package com.ece251.gongxl.transcanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

/**
 * Created by david on 2/27/15.
 */
public class Translator {
    private final static String TRANSLATE_URL = "http://openapi.baidu.com/public/2.0/bmt/translate";
    private final static String DICTIONARY_URL = "http://openapi.baidu.com/public/2.0/translate/dict/simple";
    private final static String API_KEY = "EqdscUrLrNmpdeQgj1QGLh2E";
    private final static String AUTO = "auto";
    private TextServicesManager textServicesManager;
    private SpellCheckCallback spellCheckCallback;
    private SpellCheckerSession spellCheckerSession;
    private String correctedSentence;
    private Handler handler;

    public static final int MESSAGE_TRANSLATE = 10;
    public static final int MESSAGE_LOOKUP = 11;
    public static final int MESSAGE_AUTOCORRECT = 13;


    public Translator(Context context, Handler handler) {
        this.handler = handler;
        this.textServicesManager = (TextServicesManager) context
                .getSystemService(
                        Context.TEXT_SERVICES_MANAGER_SERVICE);
        this.spellCheckCallback = new SpellCheckCallback();
        this.spellCheckerSession = textServicesManager
                .newSpellCheckerSession(null,null,
                        spellCheckCallback, true);
    }

    public void lookupDictionary(String word, boolean autoCorrection) {
        if(word == null || word.equals("")) return;
        if(word.contains(" "))
            word = word.split(" ")[0];
        new LookupThread(word, autoCorrection).start();
    }

    private class LookupThread extends Thread {
        private boolean autoCorrection;
        private String word;
        HttpClient httpClient;

        public LookupThread(String word, boolean autoCorrection) {
            this.word = word;
            this.autoCorrection = autoCorrection;
            this.httpClient = new DefaultHttpClient();
        }

        @Override
        public void run() {
            super.run();
            String corrected = null;
            if (autoCorrection) {
                corrected = spellCorrect(word);
                if(corrected.equals("")) {
                    System.out.println("nothing there");
                    return;
                }
                Message message = Message.obtain();
                message.arg1 = MESSAGE_AUTOCORRECT;
                message.obj = corrected;
                handler.sendMessage(message);
            }
            String url = DICTIONARY_URL + "?client_id=" + API_KEY
                    + "&from=" + AUTO + "&to=" + AUTO + "&q=" + URLEncoder.encode(corrected);
            HttpGet getRequest = new HttpGet(url);
            HttpResponse response = null;
            String result = null;
            InputStream json = null;
            try {
                response = httpClient.execute(getRequest);
                json = response.getEntity().getContent();
                JsonReader reader = new JsonReader(new InputStreamReader(json, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();

                    if (name.equals("data")) {
                        result = extractWordMeaning(reader);
                    } else reader.skipValue();
                }
                reader.endObject();
                json.close();
                Message message = Message.obtain();
                message.arg1 = MESSAGE_LOOKUP;
                message.obj = result;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    json.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void translate(String toTranslate, boolean autoCorrection) {
        if(toTranslate == null || toTranslate.equals("")) return;
        new TranslationThread(toTranslate, autoCorrection).start();
    }

    private class TranslationThread extends Thread {
        private boolean autoCorrection;
        private String toTranslate;
        HttpClient httpClient;

        public TranslationThread(String toTranslate, boolean autoCorrection) {
            this.toTranslate = toTranslate;
            this.autoCorrection = autoCorrection;
            httpClient = new DefaultHttpClient();
        }
        @Override
        public void run() {
            super.run();
            String corrected = null;
            if (autoCorrection) {
                corrected = spellCorrect(toTranslate);
                if(corrected.equals("")) {
                    System.out.println("nothing there");
                    return;
                }
                Message message = Message.obtain();
                message.arg1 = MESSAGE_AUTOCORRECT;
                message.obj = corrected;
                handler.sendMessage(message);
            }
            String url = TRANSLATE_URL + "?client_id=" + API_KEY
                    + "&from=" + AUTO + "&to=" + AUTO + "&q=" + URLEncoder.encode(corrected);
            HttpGet getRequest = new HttpGet(url);
            HttpResponse response = null;
            InputStream json = null;
            String result = null;
            try {
                response = httpClient.execute(getRequest);
                json = response.getEntity().getContent();
                JsonReader reader = new JsonReader(new InputStreamReader(json, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("trans_result")) {
                        result = extractTranslation(reader);
                    } else reader.skipValue();
                }
                reader.endObject();
                Message message = Message.obtain();
                message.arg1 = MESSAGE_TRANSLATE;
                message.obj = result;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    json.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String spellCorrect(String sentence) {
        String[] words = sentence.split(" ");
        TextInfo[] sentencesText = new TextInfo[words.length];
        for(int i = 0; i < words.length; i ++) {
            words[i] = filterSymbol(words[i]);
            System.out.println(words[i]);
        }
        for(int i = 0; i < words.length;i ++)
            sentencesText[i] = new TextInfo(words[i]);
        spellCheckerSession.getSentenceSuggestions(sentencesText, 1);
        while(true) {
            synchronized (this) {
                if (correctedSentence != null) break;
            }
        }
        return correctedSentence;
    }

    private static String filterSymbol(String word) {
        char[] newWord = word.toCharArray();
        for(int i = 0; i < newWord.length; i ++) {
            if(!Character.isAlphabetic(newWord[i]))
                newWord[i] = 'e';
        }
        return new String(newWord);
    }

    class SpellCheckCallback implements SpellCheckerSession.SpellCheckerSessionListener {
        @Override
        public void onGetSuggestions(SuggestionsInfo[] results) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < results.length; i ++) {
                sb.append('\n');
                for(int j = 0; j < results[i].getSuggestionsCount(); j ++) {
                    sb.append(results[i].getSuggestionAt(i) + ',');
                }
            }
            //textView.setText(sb.toString());
        }

        @Override
        public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
            StringBuilder sb = new StringBuilder();
            for(SentenceSuggestionsInfo ssi : results) {
                for(int i = 0; i < ssi.getSuggestionsCount(); i ++) {
                    sb.append(' ');
                    SuggestionsInfo si = ssi.getSuggestionsInfoAt(i);
                    if(si.getSuggestionsCount() != 0)
                        sb.append(si.getSuggestionAt(0));
//                    for(int j = 0; j < si.getSuggestionsCount(); j ++) {
//                        sb.append(si.getSuggestionAt(j) + ',');
//                    }
                }
            }
            synchronized (this) {
                correctedSentence = sb.toString().trim();
            }
            System.out.println("corrected callback " + correctedSentence);
            System.out.println(correctedSentence);
        }
    }

    private String extractDictEntry(JsonReader reader) throws IOException {
        // parts:{
        reader.beginArray();
        StringBuilder entry = new StringBuilder();
        while(reader.hasNext()) {
            // 0: {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("means")) {
                    // means: [
                    reader.beginArray();
                    while(reader.hasNext()) {
                        entry.append('\t' + reader.nextString() + "\n");
                    }
                    // means: ]
                    reader.endArray();
                } else if(name.equals("part"))
                    // part
                    entry.append(reader.nextString() + "\n");
                else reader.skipValue();
            }
            // 0: }
            reader.endObject();
        }
        // parts:}
        reader.endArray();
        return entry.toString();
    }

    private String extractWordMeaning(JsonReader reader) throws IOException {
        reader.beginObject();
        StringBuilder meaning = new StringBuilder();

        while (reader.hasNext()) {
            if (reader.nextName().equals("symbols")) {
                // symbols： [
                reader.beginArray();
                while (reader.hasNext()) {
                    // 0: {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("parts"))
                            meaning.append(extractDictEntry(reader));
                        else if (name.equals("ph_am"))
                            meaning.append(name + ": [" + reader.nextString() + "]\n");
                        else reader.skipValue();
                    }
                    // 0: }
                    reader.endObject();
                }
                // symbols ]
                reader.endArray();
            } else reader.skipValue();
        }
        reader.endObject();
        return meaning.toString();
    }




    private String extractTranslation(JsonReader reader) throws IOException {
        reader.beginArray();
        StringBuilder translated = new StringBuilder();
        while(reader.hasNext()) {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("dst"))
                    translated.append(reader.nextString());
                else reader.skipValue();
            }
            reader.endObject();
        }
        reader.endArray();
        return translated.toString();
    }

}


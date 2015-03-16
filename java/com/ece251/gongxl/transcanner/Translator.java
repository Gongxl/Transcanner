package com.ece251.gongxl.transcanner;

import android.util.JsonReader;

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
    private static HttpClient httpClient = new DefaultHttpClient();
    private final static String TRANSLATE_URL = "http://openapi.baidu.com/public/2.0/bmt/translate";
    private final static String DICTIONARY_URL = "http://openapi.baidu.com/public/2.0/translate/dict/simple";
    private final static String API_KEY = "EqdscUrLrNmpdeQgj1QGLh2E";
    private final static String AUTO = "auto";

    public boolean wordCheck(String word) {
        for(char ch : word.toCharArray()) {
            if(Character.isAlphabetic(ch))
                return false;
        }
        return true;
    }

    public static String lookupDictionary(String word) throws IOException {
        String url = DICTIONARY_URL + "?client_id=" + API_KEY
                + "&from=" + AUTO + "&to=" + AUTO + "&q=" + URLEncoder.encode(word);
        HttpGet getRequest = new HttpGet(url);
        HttpResponse response = httpClient.execute(getRequest);
        InputStream json = response.getEntity().getContent();
        JsonReader reader = new JsonReader(new InputStreamReader(json, "UTF-8"));
        reader.beginObject();
        String translated = null;
        while(reader.hasNext()) {
            String name = reader.nextName();
            if(name.equals("data"))
                translated = extractWordMeaning(reader);
            else reader.skipValue();
        }
        reader.endObject();
        json.close();
        return translated;
    }

    private static String extractDictEntry(JsonReader reader) throws IOException {
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

    private static String extractWordMeaning(JsonReader reader) throws IOException {
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


    public static  String translate(String toTranslate) throws IOException {
        String url = TRANSLATE_URL + "?client_id=" + API_KEY
                + "&from=" + AUTO + "&to=" + AUTO + "&q=" + URLEncoder.encode(toTranslate);
        HttpGet getRequest = new HttpGet(url);
        HttpResponse response = httpClient.execute(getRequest);
        InputStream json = response.getEntity().getContent();
        JsonReader reader = new JsonReader(new InputStreamReader(json, "UTF-8"));
        reader.beginObject();
        String translated = null;
        while(reader.hasNext()) {
            String name = reader.nextName();
            if(name.equals("trans_result"))
                translated = extractTranslation(reader);
            else reader.skipValue();
        }
        reader.endObject();
        json.close();
        return translated;
    }

    private static String extractTranslation(JsonReader reader) throws IOException {
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

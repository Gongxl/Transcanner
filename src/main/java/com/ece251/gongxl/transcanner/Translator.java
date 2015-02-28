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
    private final static String API_KEY = "EqdscUrLrNmpdeQgj1QGLh2E";
    private final static String AUTO = "auto";
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
                translated = extractResult(reader);
            else reader.skipValue();
        }
        reader.endObject();
        json.close();
        return translated;
    }

    private static String extractResult(JsonReader reader) throws IOException {
        reader.beginArray();
        String translated = null;
        while(reader.hasNext()) {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("dst"))
                    translated = reader.nextString();
                else reader.skipValue();
            }
            reader.endObject();
        }
        reader.endArray();
        return translated;
    }

}
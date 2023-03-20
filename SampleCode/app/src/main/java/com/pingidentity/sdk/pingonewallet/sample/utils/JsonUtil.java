package com.pingidentity.sdk.pingonewallet.sample.utils;

import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.inject.Singleton;

@Singleton
public class JsonUtil {

    private final Moshi moshi;

    public JsonUtil() {
        moshi = new Moshi.Builder().build();
    }

    public Moshi getMoshi(){
        return moshi;
    }

    public String toJson(Object o, Type t) {
        return moshi.adapter(t).toJson(o);
    }
    @SuppressWarnings("unchecked")
    public  <T> T fromJson(String json, Type t) {
        try {
            return (T) moshi.adapter(t).fromJson(json);
        } catch (IOException e) {
            return null;
        }
    }
}

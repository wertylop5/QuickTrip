package io.github.kkysen.quicktrip.misc;

import io.github.kkysen.quicktrip.apis.google.GoogleApiResponse;
import io.github.kkysen.quicktrip.reflect.Reflect;

import java.io.IOException;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * 
 * @author Khyber Sen
 */
@RequiredArgsConstructor
public class Misc {
    
    @Getter
    public static class JsonTest extends GoogleApiResponse {
        
        private final String value;
        
        public JsonTest(final String status, final String value) {
            super(status);
            this.value = value;
        }
        
        @Override
        public String toString() {
            return Reflect.toString(this);
        }
        
    }
    
    public static void main(final String[] args) throws IOException {
        final String json = "{\"status\":\"OK\",\"value\":\"hello\"}";
        final Gson gson = new Gson();
        final JsonTest test = gson.fromJson(json, JsonTest.class);
        System.out.println(test.isOk());
    }
    
}

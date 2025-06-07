package com.backend.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class QueryParser {
    public static Map<String, String> parse(String query) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new HashMap<>();
        if (query == null || query.isEmpty()) return queryPairs;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                queryPairs.put(
                    URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                );
            }
        }
        return queryPairs;
    }
}


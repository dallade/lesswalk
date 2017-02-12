package com.lesswalk;

import java.util.HashMap;

/**
 * Created by elad on 09/01/17.
 */

public class Utils {

    public static String toString(HashMap<String, String> hashMap){
        String str = "{";
        for (String key : hashMap.keySet()) {
            String val = hashMap.get(key);
            str += key + ": "+val+",";
        }
        str = str.substring(0, -1);
        str += "}";
        return str;
    }

}

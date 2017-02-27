package com.lesswalk.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

/**
 * Created by elad on 09/01/17.
 */

public class Utils {

    private static final String TAG = "Utils";

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

    public static File createDirIfNeeded(Context c, String dirPath) {
        File signaturesDir;
        if (dirPath.charAt(0) == '/'){
            signaturesDir = new File(dirPath);
        }else{
            // TODO change to the following when moving to internal storage: signaturesDir = new File(c.getFilesDir(), dirPath);
            signaturesDir = new File(c.getExternalFilesDir(null), dirPath);
        }
        if (!signaturesDir.exists())
        {
            if (signaturesDir.mkdir()) {
                Log.d(TAG, "The dir '" + signaturesDir.getAbsolutePath() + "' has been created successfully");
            }else{
                Log.e(TAG, "The dir '" + signaturesDir.getAbsolutePath() + "' has failed to get created");
                return null;
            }
        }
        return signaturesDir;
    }
}

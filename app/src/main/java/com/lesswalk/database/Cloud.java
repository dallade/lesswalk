package com.lesswalk.database;

import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by elad on 29/11/16.
 */

public abstract class Cloud
{

    public interface I_ProcessListener
    {
        void onSuccess(HashMap<String, String> result);

        void onFailure(HashMap<String, String> result);
    }

    public static class Table
    {
        public static String users      = "android_users";
        public static String signatures = "android_signatures";
    }

    public static class Field
    {
        public enum UsersField
        {
            ID, PHONE, UUID
        }

        public static ArrayMap<UsersField, String> users;

        static
        {
            users = new ArrayMap<>(UsersField.values().length);
            users.put(UsersField.ID, "id");
            users.put(UsersField.PHONE, "phone");
            users.put(UsersField.UUID, "uuid");
        }
    }

    public Cloud()
    {
    }

    public abstract void createUser(String phone, String uuid, final Cloud.I_ProcessListener listener);

    //
    public abstract JSONObject getUserJson(String phone, String countryCode);

    public abstract String getUserUuid(String phone, String countryCode);

    public abstract JSONArray findSignaturesByOwner(String uuid);

    public abstract Vector<String> findSignaturesUuidsByOwnerUuid(String uuid);

    //public abstract List<String> findSignaturesUuidsByOwnerPhone(String phone, String countryCode);
    public abstract File getSignutareFilePathByUUID(String uuid);

    //
    public abstract String downloadSignature(String uuid, AWS.OnDownloadListener onDownloadListener);

    /**
     * @param uuid
     * @param cacheDir
     * @return unzipped dir
     */
    public abstract String unzipSignatureByUUID(String uuid, File cacheDir);

    public abstract boolean unzipFileFromSignatureByUUID(String uuid, File outDir, String contentFileName);

//    public abstract String downloadAndUnzipSignature(String uuid);
}

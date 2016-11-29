package com.lesswalk.database;

import android.util.ArrayMap;

import java.util.HashMap;

/**
 * Created by elad on 29/11/16.
 */

public abstract class Cloud {

    public interface I_ProcessListener {
        void onSuccess(HashMap<String, String> result);
        void onFailure(HashMap<String, String> result);
    }

    public static class Table{
        public static String users         = "android_users";
        public static String signatures    = "android_signatures";
    }

    public static class Field{
        public enum UsersField{ID, PHONE, UUID}
        public static ArrayMap<UsersField, String> users;
        static {
            users = new ArrayMap<>(UsersField.values().length);
            users.put(UsersField.ID, "id");
            users.put(UsersField.PHONE, "phone");
            users.put(UsersField.UUID, "uuid");
        }
    }

    public Cloud() {
    }

    public abstract void createUser(String phone, String uuid, final Cloud.I_ProcessListener listener);
    public abstract void getUser(String phone, final Cloud.I_ProcessListener listener);
}

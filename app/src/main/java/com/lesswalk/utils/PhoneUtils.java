package com.lesswalk.utils;

/**
 * Created by elazarkin on 2/27/17.
 */

public class PhoneUtils
{
    public static final int PHONE_INDEX_COUNTRY = 0;
    public static final int PHONE_INDEX_MAIN    = 1;

    public static String[] splitPhoneNumber(String originalNumber)
    {
        int      COUNTRY_CODE_LENGTH = 3;
        String   LOCAL_COUNTRY_CODE  = "972";
        String[] parts               = new String[2];

        String input = originalNumber.replaceAll(" ", "");
        input = input.replaceAll("-", "");

        if (input.startsWith("0"))
        {
            parts[PHONE_INDEX_COUNTRY] = LOCAL_COUNTRY_CODE;
            parts[PHONE_INDEX_MAIN] = input;
        }
        else if (input.length() > COUNTRY_CODE_LENGTH && input.startsWith("+"))
        {
            input = input.substring(1);
            parts[PHONE_INDEX_COUNTRY] = input.substring(0, COUNTRY_CODE_LENGTH);
            parts[PHONE_INDEX_MAIN] = input.substring(parts[PHONE_INDEX_COUNTRY].length());

            if (!parts[PHONE_INDEX_MAIN].startsWith("0"))
            {
                parts[PHONE_INDEX_MAIN] = "0" + parts[PHONE_INDEX_MAIN];
            }
        }
        else return null;
        //
        return parts;
    }
}

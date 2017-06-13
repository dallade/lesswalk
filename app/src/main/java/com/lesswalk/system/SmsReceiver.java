package com.lesswalk.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by elad on 23/05/17.
 */

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_DISPLAY_SENDER = "LessWalk";//TODO be careful - this may change from the SMS service the company uses
    public static final int SMS_BODY_SIZE = 4;
    private static SmsListener sListener;
    private static String sExpectedMessage = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for (int i = 0; i < (pdus != null ? pdus.length : 0); i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);//TODO check whether the 'format' field for 'createFromPdu' is necessary
            String sender = smsMessage.getDisplayOriginatingAddress();
            if (!sender.equals(SMS_DISPLAY_SENDER)){
                return;
            }
            String messageBody = smsMessage.getMessageBody();
            if (messageBody.length() != SMS_BODY_SIZE || !sExpectedMessage.equals(messageBody)){
                if (sListener != null) {
                    sListener.onSmsMismatches(messageBody);
                }
                return;
            }
            if (sListener != null) {
                sListener.onSmsMatches(messageBody);
            }
        }
    }

    public static void setListener(String expectedMessage, SmsListener listener) {
        sExpectedMessage = expectedMessage;
        sListener = listener;
    }
}

package com.lesswalk.system;

/**
 * Created by elad on 23/05/17.
 */

interface SmsListener {

    void onSmsMatches(String messageText);

    void onSmsMismatches(String messageBody);

}

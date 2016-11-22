package com.lesswalk.database;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by elad on 22/11/16.
 */
public class ZipManager {
    private static final String TAG = "ZipManager";
    //
    private static final int BYTE_BUFFER_SIZE = 1024;

    private static void zip(String[] files, String zipFileName) {
        try {
            byte data[] = new byte[BYTE_BUFFER_SIZE];
            int numOfReadBytes;
            BufferedInputStream bis;
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipos = new ZipOutputStream(new BufferedOutputStream(fos));

            for (int i = 0; i < files.length; i++) {
                Log.v(TAG, "zip: add file: " + files[i]);
                FileInputStream fi = new FileInputStream(files[i]);
                bis = new BufferedInputStream(fi, BYTE_BUFFER_SIZE);
                ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                zipos.putNextEntry(entry);
                while ((numOfReadBytes = bis.read(data, 0, BYTE_BUFFER_SIZE)) != -1) {
                    zipos.write(data, 0, numOfReadBytes);
                }
                bis.close();
            }
            zipos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

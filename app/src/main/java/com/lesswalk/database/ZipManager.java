package com.lesswalk.database;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by elad on 22/11/16.
 */
public class ZipManager {
    private static final String TAG = "ZipManager";
    private static final int BYTE_BUFFER_SIZE = 1024;

    private static void zip(String[] files, String zipFile) {
        try {
            byte data[] = new byte[BYTE_BUFFER_SIZE];
            int numOfReadBytes;
            BufferedInputStream bis;
            FileOutputStream fos = new FileOutputStream(zipFile);
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

    public static void unzip(String zipFileName, String unzippedDir) {
        mkdirIfNeeded(unzippedDir);
        try {
            FileInputStream fin = new FileInputStream(zipFileName);
            ZipInputStream zipis = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zipis.getNextEntry()) != null) {
                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    mkdirIfNeeded(ze.getName());
                } else {
                    FileOutputStream fos = new FileOutputStream(unzippedDir + ze.getName());
                    for (int c = zipis.read(); c != -1; c = zipis.read()) {
                        fos.write(c);
                    }
                    zipis.closeEntry();
                    fos.close();
                }
            }
            zipis.close();
        } catch (Exception e) {
            Log.e(TAG, "unzip: e.message="+e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mkdirIfNeeded(String dirName) {
        //
    }

}

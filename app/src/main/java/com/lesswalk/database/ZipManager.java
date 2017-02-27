package com.lesswalk.database;

import android.content.Context;
import android.util.Log;

import com.lesswalk.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by elad on 22/11/16.
 */
public class ZipManager {
    private static final String TAG = "ZipManager";
    private static final int BYTE_BUFFER_SIZE = 1024;

    public static void zip(String[] files, String zipFile) {
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

    public static boolean unzip(Context c, String zipFileName, String unzippedDir) {
        boolean ret = false;
        try {
            int BUFFER = 2048;
            List<String> zipFiles = new ArrayList<>();
            File sourceZipFile = new File(zipFileName);
            File unzippedDestinationDirectory = new File(unzippedDir);
            if (null == (Utils.createDirIfNeeded(c, unzippedDestinationDirectory.getPath()))) return false;
            ZipFile zipFile;
            zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
            Enumeration zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(unzippedDestinationDirectory, currentEntry);
                if (currentEntry.endsWith(".zip")) {
                    zipFiles.add(destFile.getAbsolutePath());
                }

                File destinationParent = destFile.getParentFile();

                if (null == (Utils.createDirIfNeeded(c, destinationParent.getPath()))) return false;

                try {
                    if (!entry.isDirectory()) {
                        BufferedInputStream is =
                                new BufferedInputStream(zipFile.getInputStream(entry));
                        int currentByte;
                        byte data[] = new byte[BUFFER];

                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest =
                                new BufferedOutputStream(fos, BUFFER);
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            zipFile.close();

            for (String zipName : zipFiles) {
                unzip(
                        c,
                        zipName,
                        unzippedDir + File.separatorChar +
                        zipName.substring(0, zipName.lastIndexOf(".zip")
                      )
                );
            }
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            if (e.getMessage().equals("UnzippedDestinationDirectory_mkdir")){
                Log.e(TAG, e.getMessage());
            }else if (e.getMessage().equals("DestinationParent_mkdirs")) {
                Log.e(TAG, e.getMessage());
            }else{
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static boolean mkdirIfNeeded(String dirName) {
        File dir = new File(dirName);
        return dir.mkdir();
    }

//    public static void unzip(String zipFileName, String unzippedDir) {
//        mkdirIfNeeded(unzippedDir);
//        try {
//            FileInputStream fin = new FileInputStream(zipFileName);
//            ZipInputStream zipis = new ZipInputStream(fin);
//            ZipEntry ze = null;
//            while ((ze = zipis.getNextEntry()) != null) {
//                //create dir if required while unzipping
//                if (ze.isDirectory()) {
//                    mkdirIfNeeded(ze.getName());
//                } else {
//                    FileOutputStream fos = new FileOutputStream(unzippedDir + ze.getName());
//                    for (int c = zipis.read(); c != -1; c = zipis.read()) {
//                        fos.write(c);
//                    }
//                    zipis.closeEntry();
//                    fos.close();
//                }
//            }
//            zipis.close();
//        } catch (Exception e) {
//            Log.e(TAG, "unzip: e.message="+e.getMessage());
//            e.printStackTrace();
//        }
//    }


}

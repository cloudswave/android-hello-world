package com.sqisland.android.hello;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {
    private static final String TAG = "ZipExtractor";

    public static boolean extractFromResources(Context context, int resourceId, String targetDirPath) {
        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;
        
        try {
            inputStream = context.getResources().openRawResource(resourceId);
            zipInputStream = new ZipInputStream(inputStream);
            
            File targetDir = new File(targetDirPath);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    Log.e(TAG, "无法创建目标目录");
                    return false;
                }
            }
            
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(targetDir, entry.getName());
                
                if (entry.isDirectory()) {
                    if (!outputFile.mkdirs()) {
                        Log.w(TAG, "无法创建目录: " + outputFile.getAbsolutePath());
                    }
                    continue;
                }
                
                File parent = outputFile.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    Log.w(TAG, "无法创建父目录: " + parent.getAbsolutePath());
                    continue;
                }
                
                try (BufferedOutputStream outputStream = new BufferedOutputStream(
                        new FileOutputStream(outputFile))) {
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                zipInputStream.closeEntry();
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "解压失败", e);
            return false;
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "关闭流时出错", e);
            }
        }
    }
}
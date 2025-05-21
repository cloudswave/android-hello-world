package com.sqisland.android.hello;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1;
    private static final String TARGET_DIR = "MyAppData";
    private static final int ZIP_RESOURCE_ID = R.raw.data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            extractZipFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                extractZipFile();
            } else {
                Toast.makeText(this, "需要存储权限才能解压文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void extractZipFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        final File targetDir = new File(Environment.getExternalStorageDirectory(), TARGET_DIR);
        if (targetDir.exists() && targetDir.list() != null && targetDir.list().length > 0) {
            Toast.makeText(this, "文件已存在，无需重复解压", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = ZipExtractor.extractFromResources(
                        MainActivity.this,
                        ZIP_RESOURCE_ID,
                        targetDir.getAbsolutePath());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(MainActivity.this,
                                    "文件已解压到: " + targetDir.getAbsolutePath(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "文件解压失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
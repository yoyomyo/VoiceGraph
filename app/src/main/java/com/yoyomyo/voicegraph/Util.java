package com.yoyomyo.voicegraph;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Util {

    public static void requestPermission(Activity activity, String permission) {
        if (ActivityCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission},
                    10);
        }
    }

    public static List<File> getAudioFiles(File dir) {
        List<File> audioFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            Log.d("Files", "Size: " + files.length);
            for (File f : files) {
                if (f.getName().endsWith("pcm")) {
                    audioFiles.add(f);
                }
            }
        }
        return audioFiles;
    }
}

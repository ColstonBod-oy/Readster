package com.colstech.readster;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {

    /**
     * Returns locally accessible directory where our assets are extracted.
     */
    @NonNull
    public static File getLocalDir(@NonNull Context context) {
        return context.getFilesDir();
    }

    /**
     * Returns locally accessible directory path which contains the "tessdata" subdirectory
     * with *.traineddata files.
     */
    @NonNull
    public static String getTessDataPath(@NonNull Context context) {
        return getLocalDir(context).getAbsolutePath();
    }

    public static void extractAssets(@NonNull Context context) {
        AssetManager am = context.getAssets();

        File localDir = getLocalDir(context);
        if (!localDir.exists() && !localDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + localDir);
        }

        File tessDir = new File(getTessDataPath(context), "tessdata");
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + tessDir);
        }

        String[] filesToExtract = new String[]{
                "eng.traineddata",
        };
        for (String assetName : filesToExtract) {
            final File targetFile = new File(tessDir, assetName);
            if (!targetFile.exists()) {
                copyFile(am, assetName, targetFile);
            }
        }
    }

    private static void copyFile(@NonNull AssetManager am, @NonNull String assetName,
                                 @NonNull File outFile) {
        try (
                InputStream in = am.open(assetName);
                OutputStream out = new FileOutputStream(outFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

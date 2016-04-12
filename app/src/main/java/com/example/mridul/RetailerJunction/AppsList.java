package com.example.mridul.RetailerJunction;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppsList extends Application {

    private static List<AppInfoObj> appsList = new ArrayList<AppInfoObj>();

    AppsList(Context context) {

        if( appsList.size() == 0 ) {
            // load list
            String path = "/sdcard/AppsShare/apks/";

            //AssetManager assetManager = getAssets();
            File f = new File(path);
            File file[] = f.listFiles();

            //String list[] = assetManager.list(path);
            Log.e("Files", "Size: " + file.length);

            for (int i = 0; i < file.length; i++) {
                Log.e("Files", "Filename: " + file[i].getName());

                appsList.add(getAppDetail(context, path + file[i].getName()));
            }
        }
    }


    private AppInfoObj getAppDetail(Context context, String APKFilePath) {


        AppInfoObj appI = new AppInfoObj();

        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);

        // the secret are these two lines....
        pi.applicationInfo.sourceDir       = APKFilePath;
        pi.applicationInfo.publicSourceDir = APKFilePath;
        //

        Drawable d = pi.applicationInfo.loadIcon(pm);
        appI.AppName = (String)pi.applicationInfo.loadLabel(pm);
        appI.packageName = (String)pi.applicationInfo.packageName;
        appI.iconUrl = storeDrawable(d, (String)pi.applicationInfo.packageName);
        appI.apkUrl = "http://192.168.43.1:8080" + APKFilePath;
     /*  // String encodedPath = "";
        try {
            appI.apkUrl = URLEncoder.encode(appI.apkUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //appI.apkUrl = "http://192.168.43.1:8080" + encodedPath;*/


        return appI;
    }


    public String storeDrawable (Drawable drawable, String packageName) {
        Bitmap bm = drawableToBitmap(drawable);

        String extStorageDirectory = "/sdcard/AppsShare/icons/"; //dont add extra slash

        File myFile = new File(extStorageDirectory, packageName + ".PNG");

        if (!myFile.exists()) {
            myFile.getParentFile().mkdirs();
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(myFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "http://192.168.43.1:8080" + extStorageDirectory + packageName + ".PNG";
    }



    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public String getAppsListJson () {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(appsList);
    }
}

package com.leichao.retrofit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 工具类
 * Created by leichao on 2017/7/22.
 */

public class RetrofitUtil {

    /**
     * 打印log
     */
    public static void log(String str) {
        if (RetrofitConfig.getInstance().isDebug()) {
            Log.e("MULA", str);
        }
    }

    /**
     * md5加密
     *
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * 获取网络缓存地址
     */
    public static File getCacheDir(Context context) {
        File netCacheDir = null;
        String cachePath = context.getPackageName() + "/cache/network";
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED
                && MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ) {
            netCacheDir = new File(Environment.getExternalStorageDirectory(), cachePath);
        }
        if (netCacheDir == null || (!netCacheDir.exists() && !netCacheDir.mkdirs())) {
            netCacheDir = context.getCacheDir();
        }
        return netCacheDir;
    }

}

package com.leaf.downloadsample;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.leaf.downloads.DownloadManager;

/**
 * Created by Administrator on 2017/5/8.
 */

public class DownloadUtils {
    public static void download(Context mContext, String videoUrl, String iconUrl, String userAgent, String title, String fileName, String mimeType) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        String url = videoUrl;
        Uri uri = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(uri);
        req.addRequestHeader("User-Agent", userAgent);
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, fileName);
        req.setTitle(title);
        req.setIconUrl(iconUrl);
        req.setDescription("click and open");
        req.setMimeType(mimeType);
        DownloadManager.getInstance(mContext).enqueue(req);
    }
}

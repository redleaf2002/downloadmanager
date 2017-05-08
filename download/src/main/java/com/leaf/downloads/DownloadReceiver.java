/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leaf.downloads;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import static com.leaf.downloads.Constants.TAG;
import static com.leaf.downloads.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;
import static com.leaf.downloads.Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;


/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {

    /**
     * Intent extra included with {@link Constants#ACTION_CANCEL} intents,
     * indicating the IDs (as array of long) of the downloads that were
     * canceled.
     */
    public static final String EXTRA_CANCELED_DOWNLOAD_IDS =
            "com.android.providers.downloads.extra.CANCELED_DOWNLOAD_IDS";

    /**
     * Intent extra included with {@link Constants#ACTION_CANCEL} intents,
     * indicating the tag of the notification corresponding to the download(s)
     * that were canceled; this notification must be canceled.
     */
    public static final String EXTRA_CANCELED_DOWNLOAD_NOTIFICATION_TAG =
            "com.android.providers.downloads.extra.CANCELED_DOWNLOAD_NOTIFICATION_TAG";

    private static Handler sAsyncHandler;

    static {
        final HandlerThread thread = new HandlerThread("DownloadReceiver");
        thread.start();
        sAsyncHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final String action = intent.getAction();
        Log.d(TAG, "action " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            final PendingResult result = goAsync();
            sAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleBootCompleted(context);
                    result.finish();
                }
            });
        } else if (Intent.ACTION_UID_REMOVED.equals(action)) {
            final PendingResult result = goAsync();
            sAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleUidRemoved(context, intent);
                    result.finish();
                }
            });

        } else if (Constants.ACTION_OPEN.equals(action)
                || Constants.ACTION_LIST.equals(action)
                || Constants.ACTION_HIDE.equals(action)) {

            final PendingResult result = goAsync();
            if (result == null) {
                // TODO: remove this once test is refactored
                handleNotificationBroadcast(context, intent);
            } else {
                sAsyncHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleNotificationBroadcast(context, intent);
                        result.finish();
                    }
                });
            }
        } else if (Constants.ACTION_CANCEL.equals(action)) {
            long[] downloadIds = intent.getLongArrayExtra(
                    DownloadReceiver.EXTRA_CANCELED_DOWNLOAD_IDS);
            DownloadManager.getInstance(context).remove(downloadIds);
            String notifTag = intent.getStringExtra(
                    DownloadReceiver.EXTRA_CANCELED_DOWNLOAD_NOTIFICATION_TAG);
            NotificationManager notifManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notifManager.cancel(notifTag, 0);

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

            final ConnectivityManager connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = connManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                startService(context);
            }
        } else if (Constants.ACTION_RETRY.equals(action)) {
            startService(context);
        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        }

    }


    private void handleUidRemoved(Context context, Intent intent) {
        final ContentResolver resolver = context.getContentResolver();

        final int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
        final int count = resolver.delete(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, Constants.UID + "=" + uid, null);

        if (count > 0) {
            Log.d(TAG, "Deleted " + count + " downloads owned by UID " + uid);
        }
    }

    private void handleBootCompleted(Context context) {
        startService(context);
    }

    /**
     * Handle any broadcast related to a system notification.
     */
    private void handleNotificationBroadcast(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Constants.ACTION_LIST.equals(action)) {
            final long[] ids = intent.getLongArrayExtra(
                    DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            sendNotificationClickedIntent(context, ids);

        } else if (Constants.ACTION_OPEN.equals(action)) {
            final long id = ContentUris.parseId(intent.getData());
            hideNotification(context, id);

        } else if (Constants.ACTION_HIDE.equals(action)) {
            final long id = ContentUris.parseId(intent.getData());
            hideNotification(context, id);
        }
    }

    /**
     * Mark the given {@link DownloadManager#COLUMN_ID} as being acknowledged by
     * user so it's not renewed later.
     */
    private void hideNotification(Context context, long id) {
        final int status;
        final int visibility;

        final Uri uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
        final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                status = getInt(cursor, Downloads.Impl.COLUMN_STATUS);
                visibility = getInt(cursor, Downloads.Impl.COLUMN_VISIBILITY);
            } else {
                Log.w(TAG, "Missing details for download " + id);
                return;
            }
        } finally {
            cursor.close();
        }

        if (Downloads.Impl.isStatusCompleted(status) &&
                (visibility == VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        || visibility == VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)) {
            final ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_VISIBILITY,
                    Downloads.Impl.VISIBILITY_VISIBLE);
            context.getContentResolver().update(uri, values, null, null);
        }
    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     */
    private void sendNotificationClickedIntent(Context context, long[] ids) {
        Log.d(TAG, "sendNotificationClickedIntent");

        final String packageName;
        final String clazz;
        final boolean isPublicApi;

        final Uri uri = ContentUris.withAppendedId(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, ids[0]);
        final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                packageName = getString(cursor, Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE);
                clazz = getString(cursor, Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
                isPublicApi = getInt(cursor, Downloads.Impl.COLUMN_IS_PUBLIC_API) != 0;
            } else {
                Log.w(TAG, "Missing details for download " + ids[0]);
                return;
            }
        } finally {
            cursor.close();
        }

        if (TextUtils.isEmpty(packageName)) {
            Log.w(TAG, "Missing package; skipping broadcast");
            return;
        }

        Intent appIntent = null;
        if (isPublicApi) {
            appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            appIntent.setPackage(packageName);
            appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, ids);

        } else { // legacy behavior
            if (TextUtils.isEmpty(clazz)) {
                Log.w(TAG, "Missing class; skipping broadcast");
                return;
            }

            appIntent = new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            appIntent.setClassName(packageName, clazz);
            appIntent.putExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, ids);

            if (ids.length == 1) {
                appIntent.setData(uri);
            } else {
                appIntent.setData(Downloads.Impl.CONTENT_URI);
            }
        }

        context.sendBroadcast(appIntent);
    }

    private static String getString(Cursor cursor, String col) {
        return cursor.getString(cursor.getColumnIndexOrThrow(col));
    }

    private static int getInt(Cursor cursor, String col) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(col));
    }

    private void startService(Context context) {
        context.startService(new Intent(context, DownloadService.class));
    }
}

# downloadmanager

[中文Readme(Zh_cn)](https://github.com/redleaf2002/downloadmanager/blob/master/readme_zh.md)

Extend the system service DownloadManager with pauseDownload and resumeDownload. We can use this library like System "DownloadManager"

## Features
1. Support Pause and Resume to download
2. Support get downloading speed
3. Support all functions of system downloadmanager
4. Support url icon 

## Add downloadmanager to your project
Add the following code snippet in build.gralde
```java
  compile 'com.android.support.test.espresso:espresso-core:2.2.2'
```
### download_manager.aar
```java
Place download_110.aar into the libs of your project. Get the jar from the directory 'download_arr'
```
### Gradle:
```java
    compile 'com.leaf:downloadmanager:1.1.0'
```

### Maven
```java
<dependency>
  <groupId>com.leaf</groupId>
  <artifactId>downloadmanager</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

## Usage:

### 1.permission

    <uses-permission android:name="android.permission.INTERNET"/>

### 2.some common methods
```java
   DownloadManager.getInstance(mContext).pauseDownload(downloadId);
```
```java
   DownloadManager.getInstance(mContext).resumeDownload(downloadId);
```
```java
   DownloadManager.getInstance(mContext).remove(downloadId);
```
```java
   DownloadManager.getInstance(mContext).restartDownload(downloadId);
```
```java
   DownloadManager.getInstance(mContext).getDownloadSpeed(downloadId);
```
```java
   DownloadManager.getInstance(mContext).removeAll();
```
```java
  String selection = Downloads.Impl.COLUMN_STATUS + " = " + Downloads.Impl.STATUS_SUCCESS;
  String orderBy = Downloads.Impl._ID + " DESC";
  Cursor cursor = context.getContentResolver().query(Downloads.Impl.CONTENT_URI, null, selection, null, orderBy);

```

### 3.sample
like usage of system downloadmanager
```java
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

```

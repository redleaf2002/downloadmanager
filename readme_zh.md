# downloadmanager

扩展了android6.0系统的下载管理模块，增加了暂停和继续下载的功能。使用方式和原来系统的下载模块一样。

## 特点
1. 支持暂停下载和继续下载
2. 支持获取下载速度
3. 支持6.0系统的下载管理模块
4. 增加下载文件相关的icon 可以在下载的时候显示对于的图标

## 加入到项目
首先在build.gradle文件中加入
compile 'com.android.support.test.espresso:espresso-core:2.2.2'

### download_manager.aar
```java
在目录download_arr下获取aar文件 加入项目中
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
  <version>1.1.0</version>
  <type>pom</type>
</dependency>
```

## 使用:

### 1.增加网络权限

    <uses-permission android:name="android.permission.INTERNET"/>

### 2.一些常用的方法
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
   DownloadManager.getInstance(mContext).removeAll();
```
```java
   DownloadManager.getInstance(mContext).getDownloadSpeed(downloadId);
```
```java
 
  String selection = Downloads.Impl.COLUMN_STATUS + " = " + Downloads.Impl.STATUS_SUCCESS;
  String orderBy = Downloads.Impl._ID + " DESC";
  Cursor cursor = context.getContentResolver().query(Downloads.Impl.CONTENT_URI, null, selection, null, orderBy);

```

### 3.例子 
和使用系统的下载模块是一样的
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

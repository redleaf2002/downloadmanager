package com.leaf.downloadsample;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.leaf.downloads.DownloadManager;

public class MainActivity extends AppCompatActivity {
    private Button startBnt;
    private String iconUrl = "https://i.ytimg.com/vi/xRTMsguD-So/hqdefault.jpg";
    private String mineType = "MP4";
    private String videoUrl = "https://r4---sn-un57en7e.googlevideo.com/videoplayback?id=o-AD-0eZuzidz8yERERqSCHgt0K8z1JXI0mYSLsiZVBOS_&mn=sn-un57en7e&mm=31&clen=273840520&ip=211.72.246.11&ms=au&mv=m&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cexpire&mt=1494225202&signature=176A94E0E756D5B765E3D6B8BA0FF532143A729A.11D67539AAB80B332430E67CFCA888BA2330DB65&ratebypass=yes&initcwndbps=1162500&requiressl=yes&key=yt6&gir=yes&mime=video%2Fmp4&dur=5833.967&ipbits=0&expire=1494246890&lmt=1451648028124567&pl=24&itag=18&ei=ihEQWd2nFZeY4AKxvLjoAQ&source=youtube&upn=HYUPbjhZxJk";
    private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBnt = (Button) findViewById(R.id.start);
        startBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtils.download(MainActivity.this, videoUrl, iconUrl, userAgent, "mytitle", "mytitle.mp4", mineType);
            }
        });

    }


}
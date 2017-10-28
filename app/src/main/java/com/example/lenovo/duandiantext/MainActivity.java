package com.example.lenovo.duandiantext;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements MyInterfaces {
    private TextView bfz;
    private Button btnStart,btnStop;

    private String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String FILE_NAME = "xhh";
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            int a =msg.what;
            Utils.zhant = false;
            DownLoadUtil downLoadUtil = new DownLoadUtil(MainActivity.this,MainActivity.this,a);
        };

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.ones = true;
        bfz = (TextView) findViewById(R.id.main_bfz);
        btnStart = (Button) findViewById(R.id.main_start);
        btnStop = (Button) findViewById(R.id.main_stop);
        SharedPreferences mySharedPreferences = getSharedPreferences("tests",
                Activity.MODE_PRIVATE);
        bfz.setText(mySharedPreferences.getString("now_bfb", "00.00"));
        final Editor editor = mySharedPreferences.edit();
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (Utils.ones) {
                    Utils.ones = false;
                    getLength();
                }
            }
        });
        btnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!Utils.ones) {
                    Utils.ones = true;
                    Utils.zhant = true;
                }
            }
        });
    }
    @Override
    public void gengx(String a) {
        bfz.setText(a);

    }
    public void getLength(){
        ExecutorService cacheService = Executors.newCachedThreadPool();
        cacheService.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                RandomAccessFile randomFile = null;
                try {
                    URL url = new URL(Utils.MY_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestMethod("GET");
                    int length = -1;
                    if (urlConnection.getResponseCode() == 200) {
                        // 获得文件长度
                        length = urlConnection.getContentLength();
                        //创建相同大小的本地文件
                                       File dir = new File(DOWNLOAD_PATH);
                                       if (!dir.exists()) {
                                               dir.mkdir();
                                           }
                                       File file = new File(dir, FILE_NAME);
                                       randomFile = new RandomAccessFile(file, "rwd");
                                       randomFile.setLength(length);
                    }
                    if (length <= 0) {
                        return;
                    }else{
                        Message msg = Message.obtain();
                        msg.what = length;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally { // 流的回收逻辑略
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Utils.zhant = true;
    }
}

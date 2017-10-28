package com.example.lenovo.duandiantext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DownLoadUtil {
	private String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String FILE_NAME = "xhh.apk";
	private List<FileInfo> list;
	private SharedPreferences mySharedPreferences ;
	private MyInterfaces interfaces;
	private int a = 0;
	private Context context;
	RandomAccessFile randomFile = null; 
	public DownLoadUtil(Context context,MyInterfaces interfaces,int a){
		this.interfaces = interfaces;
		this.context = context;
		this.a =a ;
		//判断apk是否存在，如果存在就删除
		deleteApk();
		list = new ArrayList<FileInfo>();
		mySharedPreferences = context.getSharedPreferences("tests",
				Activity.MODE_PRIVATE);
		//获取历史记录，下载到什么位置
		addMsg();
		//开启线程下载数据
		new MyThread(list.get(0)).start();
	}
	class MyThread extends Thread{ 
		private FileInfo infos;
		public MyThread(FileInfo info){
			this.infos = info;
		}
		@Override
		public void run() {
			super.run();
			
			InputStream inputStream = null; 
				try {
					URL urls = new URL(Utils.MY_URL);
					URLConnection rulConnection=urls.openConnection();//
					HttpURLConnection httConnection = (HttpURLConnection) rulConnection;
					httConnection.setConnectTimeout(3000);
					httConnection.setChunkedStreamingMode(0);
					httConnection.setRequestMethod("GET");
					//设置下载位置
					int start;
					httConnection.setRequestProperty("Range", "bytes="+infos.getNow()+"-"+infos.getLength());

					httConnection.connect();
					int zongLength = infos.getLength();
					int nowLength = infos.getNow();
					//设置文件存储位置
					savePosition(nowLength,zongLength);
					//保存数据总长度
					savaLength(zongLength);
					if (httConnection.getResponseCode() == 206) {
						inputStream = httConnection.getInputStream();  
						byte[] buffer = new byte[512];
						int len = -1;
						long time = System.currentTimeMillis(); 
						while((len = inputStream.read(buffer))!=-1){
							randomFile.write(buffer,0,len);
							int nowLegth = mySharedPreferences.getInt("now", -1)+len;

							Editor editor = mySharedPreferences.edit();
							editor.putString("url",Utils.MY_URL );
							editor.putInt("now",nowLegth );
							editor.commit();

							Message msg = Message.obtain();
							float shu = (float)nowLegth/zongLength;
							DecimalFormat df = new DecimalFormat("0.00");//格式化小数，.后跟几个零代表几位小数
							String s = df.format(shu*100);//返回的是String类型
							//发送下载百分百
							msg.obj = s;
							handler.sendMessage(msg);
							if (Utils.zhant) {
								Editor editor4 = mySharedPreferences.edit();
								editor4.putString("now_bfb",s);
								editor4.commit();
								return;
							}
						}
						installApk();
						Utils.ones = true;
						//初始化信息
						deleteApkMsg();
					}
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			String a = (String) msg.obj;
			interfaces.gengx(a+"%");
		};
	};
	/**
	 * 下载apk
	 * */
	private void installApk() {
		File apkfile = new File(DOWNLOAD_PATH, FILE_NAME);
		if (!apkfile.exists()) {
			Toast.makeText(context, "apk不存在", Toast.LENGTH_SHORT).show();
			return;
		}else{
			
		}
		Intent i = new Intent();
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setAction(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		context.startActivity(i);
	}
	/**
	 * 判断apk是否存在，如果存在就删除
	 * */
	private void deleteApk(){
		File dir = new File(DOWNLOAD_PATH,FILE_NAME);
		if (dir.exists()){
			try {
				FileInputStream inputStream = new FileInputStream(dir);
				int size=inputStream.available();
				if (size>=a) {
					dir.delete();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	/**
	 * 获取历史记录，下载到什么位置
	 * */
	private void addMsg(){
		list.clear();
		FileInfo info;
		int length = mySharedPreferences.getInt("length", a);
		int start = mySharedPreferences.getInt("start", 0);
		int now = mySharedPreferences.getInt("now", 0);
		info = new FileInfo(Utils.MY_URL,a,start,now);
		list.add(info);
	}
	/**
	 * 记录数据的总长度
	 * */
	private void savaLength(int zongLength){
		Editor editor2 = mySharedPreferences.edit();
		editor2.putInt("length", zongLength);
		editor2.commit();
	}
	/**
	 * 设置数据存储位置
	 * */
	private void savePosition(int nowLength,int zongLength){
		File file=new File(DOWNLOAD_PATH,FILE_NAME);
		try {
			randomFile=new RandomAccessFile(file,"rwd");
			randomFile.seek(nowLength);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 清空数据
	 * */
	private void deleteApkMsg(){
		Editor editor = mySharedPreferences.edit();
		editor.putString("url","");
		editor.putInt("length",0);
		editor.putInt("start",0);
		editor.putInt("now",0);
		editor.commit();
	}
	
}

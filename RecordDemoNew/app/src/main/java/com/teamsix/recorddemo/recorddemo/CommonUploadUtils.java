package com.teamsix.recorddemo.recorddemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class CommonUploadUtils {

	private Context context;
	private boolean bAllowToUpload = true;
	private static boolean isUploading = false;
    private static boolean isDownloading = false;

    public static String getDownloadPath() {
        return downloadPath;
    }

    public static void setDownloadPath(String downloadPath) {
        CommonUploadUtils.downloadPath = downloadPath;
    }

    public static String getUploadPath() {
        return uploadPath;
    }

    public static void setUploadPath(String uploadPath) {
        CommonUploadUtils.uploadPath = uploadPath;
    }

    public static String getDomainName() {
        return domainName;
    }

    public static void setDomainName(String domainName) {
        CommonUploadUtils.domainName = domainName;
    }

    public static String getSpaceName() {
        return spaceName;
    }

    public static void setSpaceName(String spaceName) {
        CommonUploadUtils.spaceName = spaceName;
    }

    private static String downloadPath = "";
    private static String uploadPath = "";
    private static String domainName =  "http://7xkj72.com1.z0.glb.clouddn.com/";
    private static String spaceName = "recordbackup";

    private Handler handler;
    private String prefix;

	public CommonUploadUtils(Context context,Handler handler,String userName) {
		this.handler = handler;
		this.context = context;
        this.prefix = userName;

	}

	public void runUpload() {
		if(isUploading || isDownloading) {
            return;
        }
        isUploading = true;
		new Thread(new MyUploadRunnalbe()).start();
	}

    public void runDownload()
    {
        if(isUploading || isUploading) {
            return;
        }
        isDownloading = true;
        new Thread(new MyDownloadRunnable()).start();
    }

    public boolean isUploading()
    {
        return isUploading;
    }

    public boolean isDownloading()
    {
        return isDownloading;
    }

    class MyDownloadRunnable implements Runnable{

        @Override
        public void run() {
            isDownloading = true;
            Looper.prepare();
            Auth auth = Auth.create(QiNiuConfig.QINIU_AK, QiNiuConfig.QINIU_SK);
            BucketManager bucketManager = new BucketManager(auth);
            BucketManager.FileListIterator it = bucketManager.createFileListIterator(spaceName, prefix, 100, null);
            boolean bSuccess = true;
            while (it.hasNext()) {
                FileInfo[] items = it.next();
                if(items == null)
                {
                    isDownloading = false;
                    //Toast.makeText(context,"No file to restore",Toast.LENGTH_SHORT).show();
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = "No file to restore";
                    if(handler != null)
                        handler.sendMessage(msg);
                    return;
                }
                for(int i = 0; i < items.length; i++)
                {
                    System.out.println(items[i].key);
                    // generate the url of the file
                    String url2 = "";
                    url2 = domainName  + URLEncoder.encode(items[i].key.toString());

                    String urlStr = auth.privateDownloadUrl(url2, 3600 * 24);

                    OutputStream output=null;
                    try {

                        URL url=new URL(urlStr);
                        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                        /*
                         * Prepare
                         * 1.AndroidMainfest.xml add permission
                         * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                         * get permmision to write sdcard
                         * 2.getFilePath
                         * 3.Check whether file exist
                         * 4.if not exist add to the file
                         * 5.write to sd card
                         * 6.close stream
                         */

                        String pathName= downloadPath  + items[i].key.replace(prefix,""); // download filename

                        File file=new File(pathName);
                        InputStream input=conn.getInputStream();
                        if(file.exists()){
                            System.out.println("exits");
                            continue;
                        }else{
                            // assume the dir is exist
                            file.createNewFile();
                            output=new FileOutputStream(file);
                            // read data
                            byte[] buffer=new byte[1024];
                            int len = 0;
                            while((len = input.read(buffer))!=-1){
                                output.write(buffer,0,len);
                            }
                            output.flush();
                        }
                    } catch (MalformedURLException e) {
                        bSuccess = false;
                        e.printStackTrace();
                    } catch (Exception e) {
                        bSuccess = false;
                        e.printStackTrace();
                    }finally{
                        try {
                            if(output!=null)
                                output.close();

                        } catch (Exception e) {
                            bSuccess = false;
                            e.printStackTrace();
                        }
                        isDownloading = false;

                    }

                }

            }
            if(bSuccess)
            {
                System.out.println("success");
                Message msg = new Message();
                msg.what = 0;
                msg.obj = "Success to restore files";
                if(handler != null)
                    handler.sendMessage(msg);
            }
            else
            {
                System.out.println("fail");
                Message msg = new Message();
                msg.what = 0;
                msg.obj = "Fail to backup files";
                if(handler != null)
                    handler.sendMessage(msg);
            }

            isDownloading = false;
        }
    }

	class MyUploadRunnalbe implements Runnable {
		public void run() {
			isUploading = true;
            Looper.prepare();
			// get settings about the storage path

			String path = uploadPath;

			try {
				File foloder = new File(path);
				final File[] files = foloder.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						while (bAllowToUpload == false) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						bAllowToUpload = false;
						UploadManager uploadManager = new UploadManager();
						uploadManager.put(files[i], prefix + "/"+files[i].getName(), QiNiuConfig.token, new UpCompletionHandler() {
							public void complete(String k, ResponseInfo rinfo, JSONObject response) {
								bAllowToUpload = true;
                                //Toast.makeText(context,String.valueOf(response),Toast.LENGTH_SHORT).show();
							}
						}, null);
					}
				}
				isUploading = false;
				Message msg = new Message();
                msg.what = 0;
				msg.obj = "Success to backup files";
                if(handler != null)
                    handler.sendMessage(msg);
			} catch (Exception e) {
				isUploading = false;
				Message msg = new Message();
				msg.obj = "Fail to backup files";
                if(handler != null)
				    handler.sendMessage(msg);
			}
		}
	}
}

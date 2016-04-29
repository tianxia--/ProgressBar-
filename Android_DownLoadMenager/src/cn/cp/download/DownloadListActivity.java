package cn.cp.download;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


public class DownloadListActivity extends Activity {

    @ViewInject(R.id.download_list)
    private ListView downloadList;

    private DownloadManager downloadManager;
    private DownloadListAdapter downloadListAdapter;

    private Context mAppContext;
    String imagePath = "http://www.uimaker.com/uploads/allimg/120412/1_120412091841_7.jpg";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_list);
        ViewUtils.inject(this);

        mAppContext = this.getApplicationContext();
        downloadManager = DownloadService.getDownloadManager(mAppContext);
        try {
        	 String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
			downloadManager.addNewDownload("http://flv1.vodfile.m1905.com/movie/SY_fengyun2_100423.flv?key1=201604291029&key2=123c488bf2a5131a07c218ecd0f174cc", "≤‚ ‘", target, true, true, 0, imagePath,new RequestCallBack<File>() {
				
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}
			});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
       	 String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
			downloadManager.addNewDownload("http://flv1.vodfile.m1905100423.flv?key1=201604291029&key2=123c488bf2a5131a07c218ecd0f174cc", "≤‚ ‘", target, true, true, 0, imagePath,new RequestCallBack<File>() {
				
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}
			});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
       	 String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
			downloadManager.addNewDownload("http://flv1.vodfile.m1905.com/movie", "≤‚ ‘", target, true, true, 0,imagePath,new RequestCallBack<File>() {
				
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}
			});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
       	 String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
			downloadManager.addNewDownload("http://flv1.vodfile.m1905.com", "≤‚ ‘", target, true, true, 0, imagePath,new RequestCallBack<File>() {
				
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}
			});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
       	 String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
			downloadManager.addNewDownload("http://us.sinaimg.cn/001HTpwtjx06YSP79k0w05040100zcMt0k01.mp4?KID=unistore,video&ssig=ynQhSOksOQ&Expires=1461896580", "≤‚ ‘", target, true, true, 0, imagePath,new RequestCallBack<File>() {
				
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}
			});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        downloadListAdapter = new DownloadListAdapter(mAppContext, downloadManager);
        downloadManager.setAdapter(downloadListAdapter);
        downloadList.setAdapter(downloadListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        downloadListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        try {
            if (downloadListAdapter != null && downloadManager != null) {
                downloadManager.backupDownloadInfoList();
            }
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        super.onDestroy();
    }

  
}
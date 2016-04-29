package cn.cp.download;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DownloadListAdapter extends BaseAdapter {

	private final Context mContext;
	private final LayoutInflater mInflater;
	private DownloadManager mDownloadManager;
	private XUtilsImageLoader mImageLoader;
	private String mPageName;
	public DownloadListAdapter(Context context, DownloadManager downloadManager) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		this.mDownloadManager = downloadManager;
		mDownloadManager.setAdapter(this);
		mImageLoader = new XUtilsImageLoader(mContext, R.drawable.app_icon_loading, R.drawable.app_icon_load_failed);
	}

	@Override
	public int getCount() {
		if (mDownloadManager == null)
			return 0;
		return mDownloadManager.getDownloadInfoLoadingCount();
	}

	@Override
	public Object getItem(int i) {
		return mDownloadManager.getDownLoadinginfo(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	public void setPageName(String pageName){
		
		this.mPageName  =pageName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		DownloadListAdapterHolder holder = null;
		DownloadInfo downloadInfo = mDownloadManager.getDownLoadinginfo(i);

		if (view == null) {
			view = mInflater.inflate(R.layout.item_down_list, null);
			holder = new DownloadListAdapterHolder(downloadInfo, mDownloadManager, this, mContext,mPageName,mImageLoader);
			ViewUtils.inject(holder, view);//用注解实现控件跟对象的绑定，也可以直接写通过findViewById
			view.setTag(holder);
			holder.refresh();
		} else {
			holder = (DownloadListAdapterHolder) view.getTag();
			holder.update(downloadInfo);
		}
		mImageLoader.display(holder.download_pic,downloadInfo.getImageUrl());
		HttpHandler<File> handler = downloadInfo.getHandler();
		if (handler != null) {// 设置callBack，用来处理下载进度的现实刷新
			RequestCallBack callBack = handler.getRequestCallBack();
			if (callBack instanceof DownloadManager.ManagerCallBack) {
				DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
				// if (managerCallBack.getBaseCallBack() == null) {
				managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
				// }
			}
			callBack.setUserTag(new WeakReference<DownloadListAdapterHolder>(holder)); // 设置tag防止错位
		}

		return view;
	}
}

@SuppressLint("NewApi")
class DownloadListAdapterHolder {
	@ViewInject(R.id.download_label)
	TextView label;
	@ViewInject(R.id.download_state)
	TextView state;
	@ViewInject(R.id.tv_download_type)
	TextView tv_download_type;
	@ViewInject(R.id.tv_download_size)
	TextView tv_download_size;
	@ViewInject(R.id.download_pb)
	ProgressBar progressBar;
	@ViewInject(R.id.download_stop_btn)
	Button stopBtn;
	@ViewInject(R.id.download_remove_btn)
	Button removeBtn;
	@ViewInject(R.id.download_speed)
	TextView download_speed;
	@ViewInject(R.id.download_file_length)
	TextView download_file_length;
	@ViewInject(R.id.iv_downloat)
	ImageView download_pic;
	@ViewInject(R.id.tv_val_rank)
	TextView  tv_val_rank;
	private DownloadInfo downloadInfo;
	private DownloadManager mDownloadManager;
	private DownloadListAdapter mDownloadListAdapter;
	private Context mAppContext;

	private String mPageName;
	public DownloadListAdapterHolder(DownloadInfo downloadInfo, DownloadManager downloadManager, DownloadListAdapter downloadListAdapter,
			Context context,String pageName,XUtilsImageLoader imageLoader) {
		this.downloadInfo = downloadInfo;
		this.mDownloadListAdapter = downloadListAdapter;
		this.mDownloadManager = downloadManager;
		this.mAppContext = context;
		this.mPageName  =pageName;
		imageLoader.configDefaultLoadFailedImage(R.drawable.app_icon_loading);
	}
	@OnClick(R.id.download_stop_btn)
	public void stop(View view) {
		HttpHandler.State state = downloadInfo.getState();
		switch (state) {
		
		case LOADING:
			try {
				mDownloadManager.stopDownload(downloadInfo);
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
		break;
		case STARTED:
		case CANCELLED:
		case WAITING:
			
			try {
				mDownloadManager.waitAllDownload();
				mDownloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			mDownloadListAdapter.notifyDataSetChanged();
			break;
		case FAILURE:
			try {
				mDownloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			
			break;
		default:
			break;
		}
	}

	@OnClick(R.id.download_remove_btn)
	public void remove(View view) {
		try {
			mDownloadManager.removeDownload(downloadInfo);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mDownloadListAdapter.notifyDataSetChanged();
	}

	public void update(DownloadInfo downloadInfo) {
		this.downloadInfo = downloadInfo;
		refresh();
	}

	public void refresh() {

		label.setText(downloadInfo.getFileName());
		state.setText(downloadInfo.getState().toString());
		download_speed.setText(Utils.downLoadSpeed(downloadInfo.getSpeed()));
		download_file_length.setText(Float.valueOf(String.format("%.2f", ((float) downloadInfo.getProgress()) / ((float) 1048576))) + "M/"
				+ downloadInfo.getFileAllSize() + " M");
		tv_download_size.setText(downloadInfo.getFileAllSize() + " M");
		if (downloadInfo.getFileLength() > 0 && downloadInfo.getFileAllSize() > 0.0) {
			progressBar.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
		} else {
			progressBar.setProgress(0);
		}
		stopBtn.setVisibility(View.VISIBLE);
		HttpHandler.State state = downloadInfo.getState();

		
		switch (state) {
		case WAITING:
			stopBtn.setBackground(mAppContext.getResources().getDrawable(R.drawable.btn_downloaddata));
			download_speed.setText(0 + "kb/s");
			break;
		case STARTED:
			stopBtn.setBackground(mAppContext.getResources().getDrawable(R.drawable.btn_downloaddata));
			download_speed.setText(0 + "kb/s");
			break;
		case LOADING:

			stopBtn.setBackground(mAppContext.getResources().getDrawable(R.drawable.btn_download_wait));
			break;
		case CANCELLED:
			stopBtn.setBackground(mAppContext.getResources().getDrawable(R.drawable.btn_downloaddata));
			download_speed.setText(0 + "kb/s");
			break;
		case SUCCESS:
			stopBtn.setVisibility(View.INVISIBLE);
			if (mDownloadManager.getDownLoadingList() != null &&mDownloadManager.getDownLoadingList().size()>0) {
				try {
					mDownloadManager.resumeDownload(mDownloadManager.getDownLoadinginfo(0), new DownloadRequestCallBack());
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			break;
		case FAILURE:
			stopBtn.setBackground(mAppContext.getResources().getDrawable(R.drawable.btn_downloaddata));
			download_speed.setText(0 + "kb/s");

			break;
		default:
			break;
		}
//		mDownloadListAdapter.notifyDataSetChanged();
		
	}

}

class DownloadRequestCallBack extends RequestCallBack<File> {

	@SuppressWarnings("unchecked")
	private void refreshListItem() {
		if (userTag == null)
			return;
		WeakReference<DownloadListAdapterHolder> tag = (WeakReference<DownloadListAdapterHolder>) userTag;
		DownloadListAdapterHolder holder = tag.get();
		if (holder != null) {
			holder.refresh();
		}
	}

	@Override
	public void onStart() {
		refreshListItem();
	}

	@Override
	public void onLoading(long total, long current, boolean isUploading) {
		refreshListItem();
	}

	@Override
	public void onSuccess(ResponseInfo<File> responseInfo) {
		refreshListItem();
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		refreshListItem();
	}

	@Override
	public void onCancelled() {
		refreshListItem();
	}

}
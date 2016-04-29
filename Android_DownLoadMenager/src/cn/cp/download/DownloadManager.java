package cn.cp.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.HttpHandler.State;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;

/**
 * ���ع�����
 */
public class DownloadManager {

	private List<DownloadInfo> downloadInfoList; // �����б�
	private List<DownloadInfo> alreadyDownLoadList; // �Ѿ�������ɵ�
	private List<DownloadInfo> downLoadingList; // ��������
	private int maxDownloadThread = 1; // ��������߳���
	private DownloadListAdapter  mDownloadListAdapter;
	private Context mContext;
	private DbUtils db;

	/* package */DownloadManager(Context appContext) {
		ColumnConverterFactory.registerColumnConverter(HttpHandler.State.class, new HttpHandlerStateConverter());
		mContext = appContext;
		db = DbUtil.getLocalcatchDb(mContext);
		alreadyDownLoadList = new ArrayList<DownloadInfo>();
		downLoadingList = new ArrayList<DownloadInfo>();
		try {
			downloadInfoList = db.findAll(Selector.from(DownloadInfo.class)); // ��ѯ���е�����
		} catch (DbException e) {
			LogUtils.e(e.getMessage(), e);
		}
		if (downloadInfoList == null) {
			downloadInfoList = new ArrayList<DownloadInfo>();

		} else {
			for (int i = 0; i < downloadInfoList.size(); i++) {
				if (downloadInfoList.get(i).getState().equals(com.lidroid.xutils.http.HttpHandler.State.SUCCESS)) {
					alreadyDownLoadList.add(downloadInfoList.get(i));
				} else {
					downLoadingList.add(downloadInfoList.get(i));
				}
			}
		}
		setNotifyManager();
	}

	public int getDownloadInfoListCount() {
		return downloadInfoList.size();
	}

	public DownloadInfo getDownloadInfo(int index) {
		return downloadInfoList.get(index);
	}

	public int getDownloadInfoLoadingCount() {
		 DownLoadComparator comp=new DownLoadComparator();
		 Collections.sort(downLoadingList,comp);
		return downLoadingList.size();

	}

	public List<DownloadInfo> getDownLoadingList() {
		return downLoadingList;
	}

	public DownloadInfo getDownLoadinginfo(int index) {
		return downLoadingList.get(index);
	}

	public int getAlreadyDownLoadCount() {
		  DownLoadComparator comp=new DownLoadComparator();
		 Collections.sort(alreadyDownLoadList,comp);
		return alreadyDownLoadList.size();
	}

	public List<DownloadInfo> getAlreadyDownLoadList() {
		return alreadyDownLoadList;
	}

	public DownloadInfo getAlreadyDownLoadInfo(int index) {
		alreadyDownLoadList.get(index);
		return alreadyDownLoadList.get(index);
	}

	/**
	 * ��ȡ��ǰurl���ļ��Ƿ��������б����ȥ��ʱ����ȷ���Ѹ�������ӵ��������б���
	 * 
	 * @param downloadUrl
	 * @return
	 */
	public DownloadInfo getDownLoadInfo(String downloadUrl) {
		DownloadInfo downloadInfo = null;
		for (DownloadInfo doInfo : downloadInfoList) {
			if (doInfo.getDownloadUrl().equals(downloadUrl)) {
				downloadInfo = doInfo;
				return downloadInfo;
			}
		}
		return downloadInfo;
	}

	public void setAdapter(DownloadListAdapter downloadListAdapter){
		
		this.mDownloadListAdapter =downloadListAdapter;
	}
	/**
	 * �����������
	 * 
	 * @param url
	 *            ��������
	 * @param fileName
	 *            �ļ���
	 * @param target
	 *            �ļ����·��
	 * @param autoResume
	 * @param autoRename
	 * @param callback
	 *            �ص�
	 * @throws DbException
	 */
	public void addNewDownload(String url, String fileName, String target, boolean autoResume, boolean autoRename, int type,
			String imageUrl, final RequestCallBack<File> callback) throws DbException {
		if (getDownLoadInfo(url)!=null) {// �����ļ��Ѿ����ڲ��������������

			switch (getDownLoadInfo(url).getState()) {
			case SUCCESS:
				Toast.makeText(mContext, "������", Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(mContext, "������", Toast.LENGTH_SHORT).show();
				break;
			}
		} else {
			Toast.makeText(mContext, "������" ,Toast.LENGTH_SHORT).show();
			final DownloadInfo downloadInfo = new DownloadInfo();
			downloadInfo.setDownloadUrl(url);
			downloadInfo.setAutoRename(autoRename);
			downloadInfo.setAutoResume(autoResume);
			downloadInfo.setFileName(fileName);
			downloadInfo.setFileSavePath(target);
			downloadInfo.setType(type);
			downloadInfo.setImageUrl(imageUrl);
			HttpUtils http = new HttpUtils();
			http.configRequestThreadPoolSize(maxDownloadThread);
			HttpHandler<File> handler = http.download(url, target, autoResume, autoRename, new ManagerCallBack(downloadInfo, callback));
			downloadInfo.setHandler(handler);
			downloadInfo.setState(handler.getState());
			downloadInfoList.add(downloadInfo);
			downLoadingList.add(downloadInfo);
			db.saveBindingId(downloadInfo);
		}
	}






	public void resumeDownload(int index, final RequestCallBack<File> callback) throws DbException {
		final DownloadInfo downloadInfo = downloadInfoList.get(index);
		resumeDownload(downloadInfo, callback);
	}

	/**
	 * ��������
	 * 
	 * @param downloadInfo
	 *            downLoadinfo��Ϣ
	 * @param callback
	 *            �ص�
	 * @throws DbException
	 */
	public void resumeDownload(DownloadInfo downloadInfo, final RequestCallBack<File> callback) throws DbException {
		HttpUtils http = new HttpUtils();
		http.configRequestThreadPoolSize(maxDownloadThread);
		HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(), downloadInfo.getFileSavePath(),
				downloadInfo.isAutoResume(), downloadInfo.isAutoRename(), new ManagerCallBack(downloadInfo, callback));
		downloadInfo.setHandler(handler);
		downloadInfo.setState(handler.getState());
		db.saveOrUpdate(downloadInfo);
	}

	/**
	 * �Ƴ���������
	 * 
	 * @param index
	 *            Ҫ�Ƴ����±�
	 * @throws DbException
	 */
	public void removeDownload(int index) throws DbException {
		DownloadInfo downloadInfo = downLoadingList.get(index);
		removeDownload(downloadInfo);
	}

	/**
	 * �Ƴ���������
	 * 
	 * @param downloadInfo
	 *            ���ص�downloadInfo����
	 * @throws DbException
	 */
	public void removeDownload(DownloadInfo downloadInfo) throws DbException {
		HttpHandler<File> handler = downloadInfo.getHandler();
		if (handler != null && !handler.isCancelled()) {
			handler.cancel();
		}
		downloadInfo.setState(State.CANCELLED);
		downloadInfoList.remove(downloadInfo);
		downLoadingList.remove(downloadInfo);
		notificationManager.cancel(downloadInfo.getId());
		Utils.deleteFile(downloadInfo.getFileSavePath());
		db.delete(downloadInfo);
//		setNotifyCancel(downloadInfo.getFileName(),downloadInfo.getType(),downloadInfo);
	}

	/**
	 * ɾ�����Ѿ����ص�downLoadinfo �Լ��ļ�
	 * 
	 * @param downloadInfo
	 * @throws DbException
	 */
	public void removeAlreadyDownLoad(List<DownloadInfo> downloadInfos) throws DbException {
		for (int i = 0; i < downloadInfos.size(); i++) {
			HttpHandler<File> handler = downloadInfos.get(i).getHandler();
			if (handler != null && !handler.isCancelled()) {
				handler.cancel();
			}
			alreadyDownLoadList.remove(downloadInfos.get(i));
			downloadInfoList.remove(downloadInfos.get(i));
			notificationManager.cancel(downloadInfos.get(i).getId());
			Utils.deleteFile(downloadInfos.get(i).getFileSavePath());
			db.delete(downloadInfos.get(i));

		}

	}

	/**
	 * ֹͣ����
	 * 
	 * @param index
	 *            ���ص��±�
	 * @throws DbException
	 */
	public void stopDownload(int index) throws DbException {
		DownloadInfo downloadInfo = downloadInfoList.get(index);
		stopDownload(downloadInfo);
	}

	/**
	 * ֹͣ����
	 * 
	 * @param downloadInfo
	 *            ���ص�downloadInfo����
	 * @throws DbException
	 */
	public void stopDownload(DownloadInfo downloadInfo) throws DbException {
		HttpHandler<File> handler = downloadInfo.getHandler();
		if (handler != null && !handler.isCancelled()) {
			handler.cancel();
		} else {
			downloadInfo.setState(HttpHandler.State.CANCELLED);
		}
		db.saveOrUpdate(downloadInfo);
	}

	/**
	 * ֹͣ���е���������
	 * 
	 * @throws DbException
	 */
	public void stopAllDownload() throws DbException {
		for (DownloadInfo downloadInfo : downLoadingList) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null && !handler.isCancelled()) {
				handler.cancel();
			} else {
				downloadInfo.setState(HttpHandler.State.CANCELLED);
			}
		}
		db.saveOrUpdateAll(downLoadingList);
	}

	/**
	 * ֹͣ���е���������
	 * 
	 * @throws DbException
	 */
	public void waitAllDownload() throws DbException {
		for (DownloadInfo downloadInfo : downLoadingList) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null && !handler.isCancelled()) {
				handler.cancel();
			} else {
				downloadInfo.setState(HttpHandler.State.WAITING);
			}
		}
		db.saveOrUpdateAll(downLoadingList);
	}
	
	/**
	 * �ı����ݿ�������״̬
	 * 
	 * @throws DbException
	 */
	public void backupDownloadInfoList() throws DbException {
		for (DownloadInfo downloadInfo : downloadInfoList) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				switch (handler.getState()) {
				case WAITING:
					downloadInfo.setState(State.CANCELLED);
					break;
				case SUCCESS:

					break;
				case FAILURE:
					break;
				case LOADING:
					stopDownload(downloadInfo);
					downloadInfo.setState(State.CANCELLED);
					break;
				case CANCELLED:
					downloadInfo.setState(State.CANCELLED);
					break;
				case STARTED:
					downloadInfo.setState(State.CANCELLED);
					break;
				default:
					break;
				}

			}
		}
		db.saveOrUpdateAll(downloadInfoList);
	}

	public int getMaxDownloadThread() {
		return maxDownloadThread;
	}

	/**
	 * ������������߳�
	 * 
	 * @param maxDownloadThread
	 */
	public void setMaxDownloadThread(int maxDownloadThread) {
		this.maxDownloadThread = maxDownloadThread;
	}

	public class ManagerCallBack extends RequestCallBack<File> {
		private DownloadInfo downloadInfo;
		private RequestCallBack<File> baseCallBack;

		public RequestCallBack<File> getBaseCallBack() {
			return baseCallBack;
		}

		public void setBaseCallBack(RequestCallBack<File> baseCallBack) {
			this.baseCallBack = baseCallBack;
		}

		private ManagerCallBack(DownloadInfo downloadInfo, RequestCallBack<File> baseCallBack) {
			this.baseCallBack = baseCallBack;
			this.downloadInfo = downloadInfo;
		}

		@Override
		public Object getUserTag() {
			if (baseCallBack == null)
				return null;
			return baseCallBack.getUserTag();
		}

		@Override
		public void setUserTag(Object userTag) {
			if (baseCallBack == null)
				return;
			baseCallBack.setUserTag(userTag);
		}

		@Override
		public void onStart() {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				downloadInfo.setState(handler.getState());
			}
			try {
				db.saveOrUpdate(downloadInfo);
				if (downLoadingList.size() == 0) {
					downLoadingList.add(downloadInfo);
				}
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			if (baseCallBack != null) {
				baseCallBack.onStart();
			}
		}

		@Override
		public void onCancelled() {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				downloadInfo.setState(handler.getState());
			}
			try {
				db.saveOrUpdate(downloadInfo);
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			if (baseCallBack != null) {
				baseCallBack.onCancelled();
			}
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				downloadInfo.setState(handler.getState());
			}
			long nowTime = System.currentTimeMillis();
			float speed = 0;
			float speedContent = current - downloadInfo.getLatFileSize();
			float speedTimer = nowTime - downloadInfo.getLastTime();
			if (downloadInfo.getLatFileSize() > 0 && current != 0) {

				if (speedContent >= 1024) {
					speedContent = (float) ((speedContent) / (1024 + 0.0));
					speedContent = (float) (((int) (speedContent * 10) % 10 + 0.0) / 10 + (int) speedContent);
					speedTimer = (float) ((speedTimer) / (1000 + 0.0));
					speed = speedContent / speedTimer;
				}
			}

			downloadInfo.setLatFileSize(current);
			downloadInfo.setLastTime(nowTime);
			downloadInfo.setSpeed(Utils.getFloatNumber(speed));
			downloadInfo.setFileLength(total);

			downloadInfo.setFileAllSize(Float.valueOf(String.format("%.2f", ((float) total) / ((float) 1048576))));
			;
			downloadInfo.setProgress(current);
			long progress = (int) ((double) current / (double) total * 100);
			setNotifyStart(downloadInfo,(int)progress,current,total,Utils.getFloatNumber(speed));
			
			
			if (baseCallBack != null) {
				baseCallBack.onLoading(total, current, isUploading);
			}

			try {
				db.saveOrUpdate(downloadInfo);
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}

		}

		@Override
		public void onSuccess(ResponseInfo<File> responseInfo) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				if (downloadInfo.getFileAllSize() == 0.0) {
					downloadInfo.setState(State.FAILURE);
					setNotifyManager();
					setNotifyFail(downloadInfo.getFileName(), downloadInfo.getType(),downloadInfo);
				} else {
					downloadInfo.setState(handler.getState());
//					downLoadingList.remove(downloadInfo); //�h���ѽ����d�����ݣ���ӵ�������ɵļ�����ȥ��Ϊ��ʵ����������б�����������ص�����
//					alreadyDownLoadList.add(downloadInfo);
				switch (downloadInfo.getType()) {
				case 2:
					Utils.apkAdd(mContext, new File(downloadInfo.getFileSavePath()));
					break;

				default:
					break;
				}
					setNotifyManager();
					setNotifySuccess(downloadInfo.getFileName(), downloadInfo.getType(),downloadInfo);
					
					if (mDownloadListAdapter != null) {
						mDownloadListAdapter.notifyDataSetChanged();
					}
					
					/*
					 * for (int i = 0; i < mLastFileSizes.size(); i++) { mLastFileSizes.get(i).remove(downloadInfo.getDownloadUrl());
					 * mLastTimes.get(i).remove(downloadInfo.getDownloadUrl()); }
					 */
				}

			}
			try {
				db.saveOrUpdate(downloadInfo);
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			if (baseCallBack != null && downloadInfo.getFileAllSize() != 0.0) {
				baseCallBack.onSuccess(responseInfo);
			}
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				downloadInfo.setState(handler.getState());
				setNotifyManager();
				setNotifyFail(downloadInfo.getFileName(),downloadInfo.getType(),downloadInfo);
			}
			try {
				db.saveOrUpdate(downloadInfo);
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
			if (baseCallBack != null) {
				baseCallBack.onFailure(error, msg);
			}
		}
	}

	private class HttpHandlerStateConverter implements ColumnConverter<HttpHandler.State> {

		@Override
		public HttpHandler.State getFieldValue(Cursor cursor, int index) {
			return HttpHandler.State.valueOf(cursor.getInt(index));
		}

		@Override
		public HttpHandler.State getFieldValue(String fieldStringValue) {
			if (fieldStringValue == null)
				return null;
			return HttpHandler.State.valueOf(fieldStringValue);
		}

		@Override
		public Object fieldValue2ColumnValue(HttpHandler.State fieldValue) {
			return fieldValue.value();
		}

		@Override
		public ColumnDbType getColumnDbType() {
			return ColumnDbType.INTEGER;
		}
	}

	private NotificationManager notificationManager = null;
	private Notification notification = null;

	private void setNotifySuccess(String name, int type,DownloadInfo downloadInfo) {
		
		Intent intent = new Intent(mContext, DownloadListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, Notification.FLAG_AUTO_CANCEL);
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = name + "�������";
		notification.defaults = Notification.DEFAULT_SOUND;
		String success ="���سɹ�";
		
			notification.setLatestEventInfo(mContext, "����", name +success, pendingIntent);
	
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(downloadInfo.getId(), notification);
	}

	private void setNotifyStart(DownloadInfo downloadInfo,int progress,long current,long total,float speed) {
	Intent	intent = new Intent(mContext, DownloadListActivity.class);
		intent.putExtra("notify", 1);
		// ��Ҫ���õ��֪ͨ��ʱ��ʾ���ݵ���
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, Notification.FLAG_AUTO_CANCEL);
		// ����Notification����
		notification.contentIntent =pendingIntent;
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = downloadInfo.getFileName() + "������";
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.item_notify_progress);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.contentView.setTextViewText(R.id.tv_notify_press, progress+"%"); 
		notification.contentView.setTextViewText(R.id.tv_notify_speed, Utils.downLoadSpeed(downloadInfo.getSpeed())); 
		notification.contentView.setTextViewText(R.id.tv_notify_size, current / 1048576 + "M/" + total / 1048576 + "M"); 
		notification.contentView.setTextViewText(R.id.tv_notify_title, downloadInfo.getFileName()); 
		notification.contentView.setProgressBar(R.id.pb_notify_progress, 100, (int)progress, false); 
		notification.defaults =Notification.DEFAULT_LIGHTS;
		notificationManager.notify(downloadInfo.getId(), notification); 
	}

	
	private void setNotifyFail(String name, int type,DownloadInfo downloadInfo) {
		Intent intent = new Intent(mContext, DownloadListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, Notification.FLAG_AUTO_CANCEL);
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = name + "����ʧ��";
		notification.defaults = Notification.DEFAULT_SOUND;
		String fail = "ʧ��";
			notification.setLatestEventInfo(mContext, "����", name + fail, pendingIntent);

	
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(downloadInfo.getId(), notification);
	}

	private void setNotifyCancel(String name,int  type,DownloadInfo downloadInfo) {
		Intent intent = new Intent(mContext, DownloadListActivity.class);
		// ����Notification����
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, Notification.FLAG_AUTO_CANCEL);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = name + "����ȡ��";
		notification.defaults = Notification.DEFAULT_SOUND;
		
			notification.setLatestEventInfo(mContext, "����",  name + "ȡ��", pendingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(downloadInfo.getId(), notification);
	}

	private void setNotifyManager() {
		notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		// ����Notification����
		notification = new Notification();
	}
}

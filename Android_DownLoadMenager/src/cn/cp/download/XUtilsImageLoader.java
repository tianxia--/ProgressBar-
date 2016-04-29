package cn.cp.download;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;

public class XUtilsImageLoader {

	private BitmapUtils bitmapUtils;

	private Context mContext;

	public static Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	public XUtilsImageLoader(Context context, int imgDeafaultResId, int imgFailedResId) {

		this.mContext = context;
		bitmapUtils = new BitmapUtils(mContext, Constants.CACHE_IMAGE_PATH);
		bitmapUtils.configDefaultLoadingImage(imgDeafaultResId);// 默认背景图片
		bitmapUtils.configDefaultLoadFailedImage(imgFailedResId);// 加载失败图片
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);// 设置图片压缩类型
		bitmapUtils.configDefaultConnectTimeout(5000);
		bitmapUtils.configMemoryCacheEnabled(false);
	}

	public BitmapUtils configDefaltLoadingImg(int imgDeafaultResId){
		bitmapUtils.configDefaultLoadingImage(imgDeafaultResId);
		return bitmapUtils;
	}
	public BitmapUtils configDefaultLoadFailedImage(int imgFailedResId){
		bitmapUtils.configDefaultLoadFailedImage(imgFailedResId);
		return bitmapUtils;
	}
	public class CustomBitmapLoadCallBack extends

	DefaultBitmapLoadCallBack<ImageView> {

		@Override
		public void onLoading(ImageView container, String uri, BitmapDisplayConfig config, long total, long current) {

		}

		@Override
		public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
			SoftReference<Bitmap> bitmapcache = new SoftReference<Bitmap>(bitmap);
			imageCache.put(uri, bitmapcache);
			fadeInDisplay(container, bitmap, uri);
		}

		@Override
		public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
		}
	}

	private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

	private void fadeInDisplay(ImageView imageView, Bitmap bitmap, String url) {
		final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[] { TRANSPARENT_DRAWABLE,
				new BitmapDrawable(imageView.getResources(), bitmap) });
		imageView.setImageDrawable(transitionDrawable);
		transitionDrawable.startTransition(500);
	}

	public void display(ImageView container, String url) {
		if (null != imageCache.get(url)) {
			if (null == imageCache.get(url).get())
				bitmapUtils.display(container, url, new CustomBitmapLoadCallBack());
			else
				fadeInDisplay(container, imageCache.get(url).get(), url);
		} else
			bitmapUtils.display(container, url, new CustomBitmapLoadCallBack());
	}
}
package cn.cp.download;

import android.os.Environment;

public class Constants {

	public static final String CACHE_DB_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/huahua/guide/db/";

	public static final String CACHE_IMAGE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/huahua/guide/imageCache/";
}

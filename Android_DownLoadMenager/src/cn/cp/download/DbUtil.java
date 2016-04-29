package cn.cp.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DaoConfig;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

public class DbUtil {

	public static synchronized DbUtils getLocalcatchDb(Context context) {
		DaoConfig config = new DaoConfig(context);
		  config.setDbName("local.db"); //dbÃû
		  config.setDbVersion(1);  //db°æ±¾
		  config.setDbDir( Constants.CACHE_DB_PATH);
		  DbUtils db = DbUtils.create(config);

		return db;
	}
}

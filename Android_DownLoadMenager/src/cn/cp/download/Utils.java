package cn.cp.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import net.slidingmenu.tools.st.SpotDialogListener;
//import net.slidingmenu.tools.st.SpotManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class Utils {

	public static String splitVersionStr(String str, String tag) {

		String[] strs = str.split(tag);

		return strs[1];
	}

	public static String splitVersionStr(String str) {

		String[] strs = str.split("[MB]");

		return strs[0].trim();
	}

	public static String splitAppImageUrl(String str, int postion) {

		String[] strs = str.split("[.]");

		return strs[0] + "_" + postion + "." + strs[1];
	}

	public static String splitTvStr(String str) {

		String[] strs = str.split("[/]");

		return strs[2];
	}

	public static String splitNextUrl(String url) {
		String[] strs = url.split("_");
		return strs[0] + "_" + (Integer.parseInt(strs[1].substring(0, 1)) + 1) + ".mp4";
	}

	public static int splitNextPosition(String url) {
		String[] strs = url.split("_");
		return Integer.parseInt(strs[1].substring(0, 1)) + 1;
	}

	public static int splitCurrentLevel(String url) {
		if (url != null) {
			String regex = "[1-9][0-9]*\\.";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(url);
			if (matcher.find()) {
				String s = url.substring(matcher.start(), matcher.end()-1);
				return Integer.parseInt(s);
			}
		}
		return 1;
	}

	
	public static String splitNextFilePath(String filePath) {
		if (filePath.equals("") || filePath == null) {
			return "";
		} else {
			String[] strs = filePath.split("[.]");
			int level = Integer.parseInt(strs[0].substring(strs[0].length() - 1, strs[0].length())) + 1;
			return strs[0].substring(0, strs[0].length() - 1) + level + "." + strs[1];
		}

	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "";
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}

	/**
	 * 安装apk文件
	 * 
	 * @param context
	 * @param file
	 */
	public static void apkAdd(Context context, File file) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}


	public static String getNoEmptyContent(String str) {

		return   str != null && !"".equals(str)? str : "";
	}

	public static int getNoEmptyCode(String str) {

		return !"".equals(str) && str != null ? Integer.parseInt(str) : 0;
	}

	public static String stringToUTF(String str) {
		String tag = "";

		if (!"".equals(str) && str != null) {
			try {
				tag = URLEncoder.encode(str, "gbk");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return tag;
		}

		return tag;
	}

	/**
	 * 判断是否是中文
	 * 
	 * @param c
	 * @return
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是乱码的json数据
	 * 
	 * @param strName
	 * @return
	 */
	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = 0;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					count = count + 1;
				}
				chLength++;
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}



	/**
	 * 精确到俩位小数
	 * 
	 * @param f
	 * @return
	 */
	public static float getFloatNumber(float f) {
		BigDecimal bd = new BigDecimal(f);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

	/**
	 * 判断文件是否下载完成
	 * 
	 * @param context
	 * @param path
	 * @param AllSize
	 * @return
	 */
	public static boolean isDownLoaded(Context context, String path, String AllSize) {
		String integer = Formatter.formatFileSize(context, new File(path).length());
		float filesize = Float.parseFloat(splitVersionStr(integer).trim());
		float fileAllSize = Float.parseFloat(splitVersionStr(AllSize).trim());
		if ((int) filesize < (int) fileAllSize) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 电影下载是否成功
	 * 
	 * @param manager
	 * @param url
	 * @return
	 */
	public static boolean isDownLoaded(DownloadManager manager, String url) {
		boolean isDownLoading = false;
		DownloadInfo downloadInfo = manager.getDownLoadInfo(url);
		if (downloadInfo != null) {
			switch (downloadInfo.getState()) {
			case WAITING:
				break;
			case SUCCESS:
				isDownLoading = true;
				break;
			case LOADING:
				isDownLoading = false;
				break;
			case FAILURE:
				break;
			default:
				break;
			}
		}

		return isDownLoading;
	}

	/**
	 * 获取下载信息对象downLoadInfo
	 * 
	 * @param manager
	 * @param url
	 * @return
	 */
	public static DownloadInfo getDownLoadState(DownloadManager manager, String url) {
		DownloadInfo downloadInfo = manager.getDownLoadInfo(url);
		return downloadInfo;
	}

	/**
	 * 计算下载速度是mb/s 还是kb/s
	 * 
	 * @param speed
	 * @return
	 */
	public static String downLoadSpeed(float speed) {
		String downLoad;
		if (speed > 1024) {
			downLoad = Utils.getFloatNumber((speed / (float) 1024)) + "MB/S";
			return downLoad;
		} else {
			downLoad = speed + "KB/S";
			return downLoad;
		}

	}

	/**
	 * 播放本地视频
	 * 
	 * @param mContext
	 * @param path
	 */
	public static void playFileVideo(Context mContext, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String type = "video/mp4";
//		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, type);
		mContext.startActivity(intent);

	}

	/**
	 * 播放网络视频
	 * 
	 * @param mContext
	 * @param path
	 */
	public static void playNetWorkVideo(Context mContext, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String type = "video/mp4";
//		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.parse(path);
		intent.setDataAndType(uri, type);
		mContext.startActivity(intent);

	}

	/**
	 * 防止因为scrollView 解决listView 等嵌套而引起的滑动到底部
	 * 
	 * @param mScrollView
	 */
	public static void disableAutoScrollToBottom(ScrollView mScrollView) {
		mScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		mScrollView.setFocusable(true);
		mScrollView.setFocusableInTouchMode(true);
		mScrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocusFromTouch();
				return false;
			}
		});
	}
	public static void ableAutoScrollToBottom(ScrollView mScrollView) {
		mScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		mScrollView.setFocusable(false);
		mScrollView.setFocusableInTouchMode(false);
		mScrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocusFromTouch();
				return true;
			}
		});
	}

	
	/**
	 * 判断是否又足够的内存可以使用
	 * @param allMemory
	 * @param fileSize
	 * @return
	 */
	public static boolean  isfreeMemory(String allMemory,String fileSize){
		double all  = 0;
		double file = 0 ;
		
		if (allMemory.contains("MB")) {
			 all =Double.parseDouble(allMemory.replaceAll(" ", "").trim().substring(0, allMemory.length()-3));
		}else if (allMemory.contains("K")) {
			all =Double.parseDouble(allMemory.replaceAll(" ", "").trim().substring(0, allMemory.length()-1));
		}else if(allMemory.contains("M")){
			all =Double.parseDouble(allMemory.replaceAll(" ", "").trim().substring(0, allMemory.length()-1));
		}
		if (fileSize.contains("M")) {
			file =Double.parseDouble(fileSize.replaceAll(" ", "").trim().substring(0, fileSize.length()-1));
		}else if (fileSize.contains("MB")) {
			file =Double.parseDouble(fileSize.replaceAll(" ", "").trim().substring(0, fileSize.length()-2));
		}else if (fileSize.contains("K")) {
			file =Double.parseDouble(fileSize.replaceAll(" ", "").trim().substring(0, fileSize.length()-1));
		}
		
		if (all > file) {
			return true;
		}
		
		return false;
	}
	
public static boolean objIsNull (Object object){
	
	if (object != null) {
		return false;
	}
	return true;
}	

public static void delectFile (String path){
	
	File file  =new File(path);
	if (file.exists()) {
		file.delete();
	}
}

/**
 * 
 * @param theString
 * @return String
 */
public static String unicodeToUtf8(String theString) {
    char aChar;
    if(theString==null){
        return "";
    }
    int len = theString.length();
    StringBuffer outBuffer = new StringBuffer(len);
    for (int x = 0; x < len;) {
        aChar = theString.charAt(x++);
        if (aChar == '/') {
            aChar = theString.charAt(x++);
            if (aChar == 'u') {
                // Read the xxxx
                int value = 0;
                for (int i = 0; i < 4; i++) {
                    aChar = theString.charAt(x++);
                    switch (aChar) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        value = (value << 4) + aChar - '0';
                        break;
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                        value = (value << 4) + 10 + aChar - 'a';
                        break;
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                        value = (value << 4) + 10 + aChar - 'A';
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Malformed   /uxxxx   encoding.");
                    }
                }
                outBuffer.append((char) value);
            } else {
                if (aChar == 't')
                    aChar = 't';
                else if (aChar == 'r')
                    aChar = 'r';
                else if (aChar == 'n')
                    aChar = 'n';
                else if (aChar == 'f')
                    aChar = 'f';
                outBuffer.append(aChar);
            }
        } else
            outBuffer.append(aChar);
    }
    return outBuffer.toString();
}
 

public static  void full(boolean enable,Activity activity) {
    if (enable) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(lp);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    } else {
        WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setAttributes(attr);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}

/**
* 获取指定文件大小
* @param f
* @return
* @throws Exception
*/
public static long getFileSize(File file) throws Exception
{
long size = 0;
 if (file.exists()){
 FileInputStream fis = null;
 fis = new FileInputStream(file);
 size = fis.available();
 }
 else{
 Log.e("获取文件大小","文件不存在!");
 }
 return size;
}
 

/**  
 * 复制单个文件  
 * @param oldPath String  
 * @param newPath String  
 * @return boolean  
 */   
public static boolean copyFile(File oldFile, String newPath) {   
   try {   
       int bytesum = 0;   
       int byteread = 0;   
      
       if (oldFile.exists()) { //文件不存在时   
           InputStream inStream = new FileInputStream(oldFile.getAbsolutePath()); //读入原文件   
           FileOutputStream fs = new FileOutputStream(newPath);   
           byte[] buffer = new byte[1444];   
           int length;   
           while ( (byteread = inStream.read(buffer)) != -1) {   
               bytesum += byteread; //字节数 文件大小   
               System.out.println(bytesum);   
               fs.write(buffer, 0, byteread);   
           }   
           inStream.close();   
           return true;
       }   
       return false;
   }   
   catch (Exception e) {   
       System.out.println("复制单个文件操作出错");   
       e.printStackTrace();   
       return false;
   }

}   

/**  
 * 复制整个文件夹内容  
 * @param oldPath String 
 * @param newPath String
 * @return boolean  
 */   
public static void copyFolder(String oldPath, String newPath) {   

   try {   
       (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹   
       File a=new File(oldPath);   
       String[] file=a.list();   
       File temp=null;   
       for (int i = 0; i < file.length; i++) {   
           if(oldPath.endsWith(File.separator)){   
               temp=new File(oldPath+file[i]);   
           }   
           else{   
               temp=new File(oldPath+File.separator+file[i]);   
           }   

           if(temp.isFile()){   
               FileInputStream input = new FileInputStream(temp);   
               FileOutputStream output = new FileOutputStream(newPath + "/" +   
                       (temp.getName()).toString());   
               byte[] b = new byte[1024 * 5];   
               int len;   
               while ( (len = input.read(b)) != -1) {   
                   output.write(b, 0, len);   
               }   
               output.flush();   
               output.close();   
               input.close();   
           }   
           if(temp.isDirectory()){//如果是子文件夹   
               copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);   
           }   
       }   
   }   
   catch (Exception e) {   
       System.out.println("复制整个文件夹内容操作出错");   
       e.printStackTrace();   

   }   

}  


public static boolean checkPhoneNumber(String phone){
	 Pattern p = Pattern
	            .compile("^((12[0-9])|(11[0-9])|(13[0-9])|(14[0-9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9])|(19[0-9])|)\\d{11}$");
	    Matcher m = p.matcher(phone);
	    return m.matches();
	}
/**
 * 解决俩个double类型相减
 * @param v1
 * @param v2
 * @return
 */
public static double sub(double v1, double v2) {  
    BigDecimal b1 = new BigDecimal(Double.toString(v1));  
    BigDecimal b2 = new BigDecimal(Double.toString(v2));  
    return b1.subtract(b2).doubleValue();  
	}  




public static String TimeStamp2Date(String timestampString){     //"yyyy-MM-dd HH:mm:ss"
	  Long timestamp = Long.parseLong(timestampString)*1000;    
	  String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(timestamp));    
	  return date;    
	}

public static String[] getDate(String time){
	String date[] = null ;
	if (time != null && !"".equals(time)) {
		date = time.split("[-]");
		return date;
	}
	return date;		
} 


public static String splitFileToSize(String str){
	String result=  null;
	if (str.contains("M")) {
		result = str.split("M")[0].trim();
	}else if (str.contains("MB")) {
		result = str.split("MB")[0].trim();
	}else if (str.contains("G")) {
		result = Double.parseDouble(str.split("G")[0].trim()) * 1024 +"";
	}else if (str.contains("K")) {
		result = Double.parseDouble(str.split("K")[0].trim())/1024 +"";
	}
	return result;
}
/**
 * 防止快速点击
 */
private static long lastClickTime;
private static long INTERVAL_TIME = 800; 
public static boolean isFastDoubleClick() {  
    long time = System.currentTimeMillis();  
    long timeD = time - lastClickTime;  
    if ( 0 < timeD && timeD < INTERVAL_TIME) {     
        return true;     
    }     
    lastClickTime = time;     
    return false;     
}  


/**
 * 获取会员时长
 * @param startTime
 * @param endTime
 * @return
 */
public static String  getMemberDay(long startTime, long endTime){
	long sub = (endTime - startTime) / (60 * 60);
	StringBuffer result= new StringBuffer("");
	if (sub >= 24) {
		result.append(sub/24 + "天");
		if (sub%24 != 0) {
			result.append(sub%24 +"小时");
		}
			
	}else if (sub < 24) {
		result.append(sub + "小时");
	}
	return result.toString();
}

/**
 * 获取屏幕的大小
 */

public  static DisplayMetrics getScreenSize(Activity act){
	  DisplayMetrics metric = new DisplayMetrics();
	  act.getWindowManager().getDefaultDisplay().getMetrics(metric);
      int height = metric.heightPixels;  // 屏幕高度（像素
      return metric;
}
/**
 * 获取屏幕的高
 * @param act
 * @return
 */
public static int getScreenHeight(Activity act){
	DisplayMetrics displayMetrics= getScreenSize(act);
	  int height = displayMetrics.heightPixels;  // 屏幕高度（像素
	  return height;
}

/**
 * 获取屏幕的宽
 * @param act
 * @return
 */
public int getScreenWidth(Activity act){
	DisplayMetrics displayMetrics= getScreenSize(act);
	  int width = displayMetrics.widthPixels;  // 屏幕高度（像素
	  return width;
}
public static void narrowLayout(RelativeLayout relativeLayout ,Activity act,boolean isMove ,int distance){
	
	if (isMove) {
		int height = getScreenHeight(act);
		relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height - distance));
	}else
		relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	
}

/** 
 * 获取当月的 天数 
 * */  
public static int getCurrentMonthDay() {  
      
    Calendar a = Calendar.getInstance();  
    a.set(Calendar.DATE, 1);  
    a.roll(Calendar.DATE, -1);  
    int maxDate = a.get(Calendar.DATE);  
    return maxDate;  
}  
/**
 * 获取当前是第几周
 * @return
 */
public static int getCurrentWeek(){
	Date date = new Date(System.currentTimeMillis());
	int week = date.getDay();
	return week;
}
/**
 * 格式化时间
 * @param time
 * @return
 */
public static  String getTime(long time){
	
	DateFormat format  = new DateFormat();
	String str= (String) format.format("yyyy年MM月", time);
	return str;
			
}
/**
 * 获取本月开始第一天是星期几
 * @return
 */
public static int getCurrentMonthStart(){
	
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.DAY_OF_MONTH, 1);
	SimpleDateFormat format = new SimpleDateFormat("E");
	System.out.println("本月第一天是：" + format.format(calendar.getTime()) + "===="+calendar.get(Calendar.DAY_OF_WEEK)); 
	int day = calendar.get(Calendar.DAY_OF_WEEK);
	if (day == 1) 
		day = 7;
	else
		day = day - 1;
	return day;
}

/**
 * 获取当前是这个月的第几天
 * @return
 */
public static int getDayOfMonth(){
	return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
}

public static List<Integer> SpliteSignInDay(String str){
	String[] days = str.split("[,]");
	List<Integer> signInDays = new ArrayList<Integer>();
	for (int i = 0; i < days.length; i++) {
		signInDays.add(Integer.parseInt(days[i]));
	}
	return signInDays;
}
/** 
 * 把中文转成Unicode码 
 * @param str 
 * @return 
 */  
public static String chinaToUnicode(String str){  
    String result="";  
    for (int i = 0; i < str.length(); i++){  
        int chr1 = (char) str.charAt(i);  
        if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)  
            result+="\\u" + Integer.toHexString(chr1);  
        }else{  
            result+=str.charAt(i);  
        }  
    }  
    return result;  
}  

public static long getPlayTime(long l){
	
	long time = l/1000/60;
	return (long)time;
}



public static int FormatTime(long time){
	SimpleDateFormat format = new SimpleDateFormat("dd");
	String day= format.format(time);
	int d = 0;
	if (day != null) {
		d= Integer.parseInt(day);
	}
	return d;
}

public static boolean isRechargeCard(String str){
	String regEx = "\\d{12}";
	Pattern pattern = Pattern.compile(regEx);
	Matcher matcher = pattern.matcher(str);
	if (matcher.matches()) 
		return true;
	else
		return false;
}

public static int getTimeDur(long last,long curent){
	long dur = (curent - last)/1000/60;
	return (int)dur;
	
}
/** 
 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
 */  
public static int px2dip(Context context, float pxValue) {  
    final float scale = context.getResources().getDisplayMetrics().density;  
    return (int) (pxValue / scale + 0.5f);  
}  

public static String getTag_vip(String str){
	String result = "";
	String[] strs = str.split("[ ]");
	if (strs[strs.length-1]!=null) {
		result = strs[strs.length-1];
		if (result.length()==1) {
			return result;
		}else
			result = "";
			return result;
	}
	return result;
}
}

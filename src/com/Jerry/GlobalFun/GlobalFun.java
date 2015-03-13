package com.Jerry.GlobalFun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.Jerry.GPSApp.R;
import com.Jerry.GPSArea.Point;
import com.Jerry.GPSService.GPSDataService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class GlobalFun {
	/**
	 * 显示 toast 提示
	 * 
	 * @param context context
	 * @param str 显示的字符串
	 */
	public static void DisplayToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	/** 获取系统时间 */
	public static String getSystemTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		String timeString = formatter.format(currentTime);
		return timeString;
	}
	/** 获取系统日期 */
	public static String getSystemDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	/** 完全退出程序 */
	public static void exitComplete() {
		// 方法一, 需要 android.permission.RESTART_PACKAGES权限, 只能结束Activity,不能结束service
//		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
//        am.killBackgroundProcesses(getPackageName());
		// 方法二, 成功,偶尔会出现 Activity和service都结束,但后台未结束 的情况
//		android.os.Process.killProcess(android.os.Process.myPid());
		// 方法二 改进, 结果同上
		android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
		// 方法三, 成功,同上
//		System.exit(0);
		// 方法三 补充,成功,但是将本程序以外的应用也退出了
//		Intent startMain = new Intent(Intent.ACTION_MAIN);
//		startMain.addCategory(Intent.CATEGORY_HOME);
//		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //使用 Intent.FLAG_ACTIVITY_CLEAR_TOP 效果一样
//		startActivity(startMain);
//		System.exit(0);
	}
	/**
	 * 显示 确定取消对话框
	 * 
	 * @param act Activity界面
	 * @param title 标题
	 * @param Message 对话框信息
	 */
	public static void showExitDialog(final Activity act, CharSequence title, CharSequence Message) {
		// 创建退出对话框
		Dialog exitDialog = new AlertDialog.Builder(act).setTitle(title).setMessage(Message)
			.setPositiveButton(act.getText(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					act.finish();
				}
			}).setNegativeButton(act.getText(R.string.Cancel), null).create();
		exitDialog.show();
	}
	/** 由经纬度反算成高斯投影坐标 */
	public static Point BLToGauss(double longitude, double latitude) {
		int ProjNo = 0;
		int ZoneWide; // //带宽
		double longitude1, latitude1, longitude0, latitude0, X0, Y0, xval, yval;
		double a, f, e2, ee, NN, T, C, A, M;
		double iPI = Math.PI / 180;// 0.0174532925199433; //3.1415926535898/180.0;
		ZoneWide = 6;// 6度带宽
		// a = 6378245.0; f = 1.0 / 298.3; // 54年北京坐标系参数
		a = 6378140.0;
		f = 1 / 298.257; // 80年西安坐标系参数
		ProjNo = (int) (longitude / ZoneWide);
		longitude0 = ProjNo * ZoneWide + ZoneWide / 2;
		longitude0 = longitude0 * iPI;
		latitude0 = 0;
		System.out.println(latitude0);
		longitude1 = longitude * iPI; // 经度转换为弧度
		latitude1 = latitude * iPI; // 纬度转换为弧度
		e2 = 2 * f - f * f;
		ee = e2 * (1.0 - e2);
		NN = a / Math.sqrt(1.0 - e2 * Math.sin(latitude1) * Math.sin(latitude1));
		T = Math.tan(latitude1) * Math.tan(latitude1);
		C = ee * Math.cos(latitude1) * Math.cos(latitude1);
		A = (longitude1 - longitude0) * Math.cos(latitude1);
		M = a
			* ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * latitude1
				- (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2 * e2 / 1024) * Math.sin(2 * latitude1)
				+ (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * Math.sin(4 * latitude1) - (35 * e2
				* e2 * e2 / 3072)
				* Math.sin(6 * latitude1));
		xval = NN
			* (A + (1 - T + C) * A * A * A / 6 + (5 - 18 * T + T * T + 72 * C - 58 * ee) * A * A * A * A
				* A / 120);
		yval = M
			+ NN
			* Math.tan(latitude1)
			* (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24 + (61 - 58 * T + T * T + 600
				* C - 330 * ee)
				* A * A * A * A * A * A / 720);
		X0 = 1000000L * (ProjNo + 1) + 500000L;
		Y0 = 0;
		xval = xval + X0;
		yval = yval + Y0;
		Point resultPoint = new Point(xval, yval);
		return resultPoint;
	}
	/** 弧度转换 */
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	/** 计算位移 */
	public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1)
			* Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * GPSDataService.EARTH_RADIUS;
		return s;
	}
	/**
	 * 计算速度
	 * 
	 * @param distance 位移 单位 米
	 * @param time 时间 单位 秒
	 */
	public static double calcSpeed(double distance, double time) {
		double result = 0.0;
		result = distance / time;
		return result;
	}
	/**
	 * 计算面积
	 * 
	 * @param points 坐标容器
	 * @return 返回面积值
	 */
	public static double calcArea(ArrayList<Point> points) {
		int len = points.size();
		double result = 0.0;

		Point[] resultPoints = new Point[len];
		for(int i = 0; i < len; i++) {
			resultPoints[i] = BLToGauss(points.get(i).x, points.get(i).y);
		}

		for(int i = 0; i < len - 1; i++) {
			result += resultPoints[i].x * resultPoints[i + 1].y - resultPoints[i + 1].x
				* resultPoints[i].y;
		}
		result += resultPoints[len - 1].x * resultPoints[0].y - resultPoints[0].x
			* resultPoints[len - 1].y;

		return Math.abs(result) / 2;
	}

	/** 将 double 类型的数据 转换为 度分秒格式 */
	public static String doubleToDegree(double number) {
		String result = new String();
		int degree = (int) number;

		double minutesTemp = Math.abs(number - degree) * 60;
		int minutes = (int) (minutesTemp);

		double secondTemp = Math.abs(minutesTemp - minutes) * 60;
		int seconds = (int) (secondTemp);

		result = (degree + "°" + minutes + "′" + seconds + "″");
		return result;
	}

	/** 数字格式化, 如果数字太长了,转换成科学计数法 */
	public static String numberFormat() {
		String result = null;
		return result;
	}

}

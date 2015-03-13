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
	 * ��ʾ toast ��ʾ
	 * 
	 * @param context context
	 * @param str ��ʾ���ַ���
	 */
	public static void DisplayToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	/** ��ȡϵͳʱ�� */
	public static String getSystemTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		String timeString = formatter.format(currentTime);
		return timeString;
	}
	/** ��ȡϵͳ���� */
	public static String getSystemDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	/** ��ȫ�˳����� */
	public static void exitComplete() {
		// ����һ, ��Ҫ android.permission.RESTART_PACKAGESȨ��, ֻ�ܽ���Activity,���ܽ���service
//		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
//        am.killBackgroundProcesses(getPackageName());
		// ������, �ɹ�,ż������� Activity��service������,����̨δ���� �����
//		android.os.Process.killProcess(android.os.Process.myPid());
		// ������ �Ľ�, ���ͬ��
		android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
		// ������, �ɹ�,ͬ��
//		System.exit(0);
		// ������ ����,�ɹ�,���ǽ������������Ӧ��Ҳ�˳���
//		Intent startMain = new Intent(Intent.ACTION_MAIN);
//		startMain.addCategory(Intent.CATEGORY_HOME);
//		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //ʹ�� Intent.FLAG_ACTIVITY_CLEAR_TOP Ч��һ��
//		startActivity(startMain);
//		System.exit(0);
	}
	/**
	 * ��ʾ ȷ��ȡ���Ի���
	 * 
	 * @param act Activity����
	 * @param title ����
	 * @param Message �Ի�����Ϣ
	 */
	public static void showExitDialog(final Activity act, CharSequence title, CharSequence Message) {
		// �����˳��Ի���
		Dialog exitDialog = new AlertDialog.Builder(act).setTitle(title).setMessage(Message)
			.setPositiveButton(act.getText(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					act.finish();
				}
			}).setNegativeButton(act.getText(R.string.Cancel), null).create();
		exitDialog.show();
	}
	/** �ɾ�γ�ȷ���ɸ�˹ͶӰ���� */
	public static Point BLToGauss(double longitude, double latitude) {
		int ProjNo = 0;
		int ZoneWide; // //����
		double longitude1, latitude1, longitude0, latitude0, X0, Y0, xval, yval;
		double a, f, e2, ee, NN, T, C, A, M;
		double iPI = Math.PI / 180;// 0.0174532925199433; //3.1415926535898/180.0;
		ZoneWide = 6;// 6�ȴ���
		// a = 6378245.0; f = 1.0 / 298.3; // 54�걱������ϵ����
		a = 6378140.0;
		f = 1 / 298.257; // 80����������ϵ����
		ProjNo = (int) (longitude / ZoneWide);
		longitude0 = ProjNo * ZoneWide + ZoneWide / 2;
		longitude0 = longitude0 * iPI;
		latitude0 = 0;
		System.out.println(latitude0);
		longitude1 = longitude * iPI; // ����ת��Ϊ����
		latitude1 = latitude * iPI; // γ��ת��Ϊ����
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
	/** ����ת�� */
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	/** ����λ�� */
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
	 * �����ٶ�
	 * 
	 * @param distance λ�� ��λ ��
	 * @param time ʱ�� ��λ ��
	 */
	public static double calcSpeed(double distance, double time) {
		double result = 0.0;
		result = distance / time;
		return result;
	}
	/**
	 * �������
	 * 
	 * @param points ��������
	 * @return �������ֵ
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

	/** �� double ���͵����� ת��Ϊ �ȷ����ʽ */
	public static String doubleToDegree(double number) {
		String result = new String();
		int degree = (int) number;

		double minutesTemp = Math.abs(number - degree) * 60;
		int minutes = (int) (minutesTemp);

		double secondTemp = Math.abs(minutesTemp - minutes) * 60;
		int seconds = (int) (secondTemp);

		result = (degree + "��" + minutes + "��" + seconds + "��");
		return result;
	}

	/** ���ָ�ʽ��, �������̫����,ת���ɿ�ѧ������ */
	public static String numberFormat() {
		String result = null;
		return result;
	}

}

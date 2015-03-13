package com.Jerry.GPSService;

import java.util.Iterator;

import com.Jerry.GlobalFun.GlobalFun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/** 用这个类得到GPS数据 以及发送广播数据 */
public class GPSDataService extends Service {
	public static final String	GPS_COMM			= "com.GPSService.GPSDataService";
	public static final String	GPS_STATE			= "GPSDataService.GPSState";
	public static final String	GPS_LAT				= "GPSDataService.Latitude";
	public static final String	GPS_LNG				= "GPSDataService.Lngitude";
	public static final String	GPS_HORSPEED		= "GPSDataService.horSpeed";
	public static final String	GPS_SATELLITENUM	= "GPSDataService.satelliteNum";

	private LocationManager		lManager;
	public static double		EARTH_RADIUS		= 6378137;
	private boolean				gpsState			= false;
	private Double				latPre				= 0.0;								// 上一个纬度
	private Double				lngPre				= 0.0;								// 上一个经度
	private Double				latCur				= 0.0;								// 当前纬度
	private Double				lngCur				= 0.0;								// 当前经度
	private Double				distance			= 0.0;								// 位移
	private Double				horSpeed			= 0.0;								// 速度
	/** 刷新时间的值 单位秒 默认值1 */
	public static final int		defaultRefrashTime	= 1;

	private Handler				updateHandler		= new Handler();

	private SendMsgThread		sendMsgThread;
	private boolean				sendMsgThreadFlag	= false;

	/** 发送数据的线程 */
	class SendMsgThread extends Thread {
		public SendMsgThread() {
			Log.i("GPSDataService", "SendMsgThread 线程创建");
			// 线程运行标记, true为运行
			sendMsgThreadFlag = true;
		}
		@Override
		public void run() {
			while (sendMsgThreadFlag) {
				sendMsgThreadFlag = true;
				sendMsg();
				try {
					Thread.sleep(defaultRefrashTime * 1000);
				} catch (InterruptedException e) {
					Thread.currentThread().isInterrupted();
				}
			}
		}
		private void sendMsg() {
			// 使用广播方式发送数据
			Intent intentSend = new Intent(GPS_COMM);
			intentSend.putExtra(GPS_STATE, getGpsState());
			intentSend.putExtra(GPS_LAT, getLatitude());
			intentSend.putExtra(GPS_LNG, getLngitude());
			intentSend.putExtra(GPS_HORSPEED, getHorSpeed());
			intentSend.putExtra(GPS_SATELLITENUM, getSatelliteNum());
			sendBroadcast(intentSend);
		}
		public synchronized void stopThread() {
			sendMsgThreadFlag = false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		Log.i("GPSDataService", "服务 onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("GPSDataService", "服务 onStart");
		// 初始化 LocationManager
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		gpsState = getGpsState();
		if (gpsState) {
			initLocation();
		} else {
			updateNullLocation();
		}
		super.onStart(intent, startId);
	}
	/** 初始化 位置管理器 等 */
	private void initLocation() {
		// 查找到服务信息
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
		String provider = lManager.getBestProvider(criteria, true); // 获取GPS信息
		Location location = lManager.getLastKnownLocation(provider); // 通过GPS获取位置
		if (location != null) {
			updateNewLocation(location);
			updateNewLocation(location);
		} else {
			updateNullLocation();
		}
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
		lManager.requestLocationUpdates(provider, 1 * 100, 0, new locListener());

		// 初始化发送线程
		if (!sendMsgThreadFlag) {
			sendMsgThread = new SendMsgThread();
			sendMsgThread.start();
		}

		// 更新速度数据
		updateHandler.removeCallbacks(runnable);
		updateHandler.post(runnable);
	}
	/** 更新空坐标数据 */
	private void updateNullLocation() {
		latPre = latCur;
		lngPre = lngCur;
		setLatitude(0.0);
		setLongitude(0.0);
	}
	/** 更新非空坐标数据 当坐标改变的时候会更新 */
	private void updateNewLocation(Location location) {
		latPre = latCur;
		lngPre = lngCur;
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
		Log.i("updateDataSpeed", "当前坐标 :" + latCur + "\t" + lngCur);
		Log.i("updateDataSpeed", "上个坐标 :" + latPre + "\t" + lngPre);
	}
	/** 更新速度数据 持续更新 */
	private Runnable	runnable	= new Runnable() {
										@Override
										public void run() {
											updateData();
											updateHandler.postDelayed(this, defaultRefrashTime * 1000);
										}
									};
	private void updateData() {
		float result[] = new float[2];
		Location.distanceBetween(latPre, lngPre, latCur, lngCur, result);
		distance = (double) result[0];
//		distance = GlobalFun.calcDistance(latCur, lngCur, latPre, lngPre);
		Log.i("GPSDataService", "计算位移 :" + distance);
//		Log.i("updateDataSpeed", "当前坐标 :" + latCur + "\t" + lngCur);
//		Log.i("updateDataSpeed", "上个坐标 :" + latPre + "\t" + lngPre);
		setHorSpeed(GlobalFun.calcSpeed(distance, defaultRefrashTime));
		Log.i("GPSDataService", "计算速度 :" + horSpeed);
		latPre = latCur;
		lngPre = lngCur;
	}
	private class locListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			updateNewLocation(location);
		}
		/** provider 禁用时触发 */
		@Override
		public void onProviderDisabled(String provider) {
			Log.i("GPSDataService", "GPS被禁用");
			gpsState = false;
		}
		/** provider 打开时触发 */
		@Override
		public void onProviderEnabled(String provider) {
			Log.i("GPSDataService", "GPS已启用");
			gpsState = true;
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}
	@Override
	public void onDestroy() {
		updateHandler.removeCallbacks(runnable);
		if (sendMsgThreadFlag) {
			sendMsgThread.stopThread();
		}
		Log.i("GPSDataService", "服务 onDestroy");
		super.onDestroy();
	}

	/** 判断GPS设备是否开启 */
	private boolean getGpsState() {
		return lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	/** 得到GPS 连接卫星数量 */
	private int getSatelliteNum() {
		Iterator<GpsSatellite> iterator = lManager.getGpsStatus(null).getSatellites().iterator();

		int count = 0;
		while (iterator.hasNext()) {
			count++;
		}
		return count;
	}
	private void setLatitude(double latitude) {
		this.latCur = latitude;
	}
	private double getLatitude() {
		return latCur;
	}

	private void setLongitude(double longitude) {
		this.lngCur = longitude;
	}
	private double getLngitude() {
		return lngCur;
	}

	private void setHorSpeed(double horSpeed) {
		this.horSpeed = horSpeed;
	}
	private double getHorSpeed() {
		return horSpeed;
	}
}
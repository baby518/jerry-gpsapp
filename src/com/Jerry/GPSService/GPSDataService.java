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

/** �������õ�GPS���� �Լ����͹㲥���� */
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
	private Double				latPre				= 0.0;								// ��һ��γ��
	private Double				lngPre				= 0.0;								// ��һ������
	private Double				latCur				= 0.0;								// ��ǰγ��
	private Double				lngCur				= 0.0;								// ��ǰ����
	private Double				distance			= 0.0;								// λ��
	private Double				horSpeed			= 0.0;								// �ٶ�
	/** ˢ��ʱ���ֵ ��λ�� Ĭ��ֵ1 */
	public static final int		defaultRefrashTime	= 1;

	private Handler				updateHandler		= new Handler();

	private SendMsgThread		sendMsgThread;
	private boolean				sendMsgThreadFlag	= false;

	/** �������ݵ��߳� */
	class SendMsgThread extends Thread {
		public SendMsgThread() {
			Log.i("GPSDataService", "SendMsgThread �̴߳���");
			// �߳����б��, trueΪ����
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
			// ʹ�ù㲥��ʽ��������
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
		Log.i("GPSDataService", "���� onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("GPSDataService", "���� onStart");
		// ��ʼ�� LocationManager
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		gpsState = getGpsState();
		if (gpsState) {
			initLocation();
		} else {
			updateNullLocation();
		}
		super.onStart(intent, startId);
	}
	/** ��ʼ�� λ�ù����� �� */
	private void initLocation() {
		// ���ҵ�������Ϣ
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // �߾���
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // �͹���
		String provider = lManager.getBestProvider(criteria, true); // ��ȡGPS��Ϣ
		Location location = lManager.getLastKnownLocation(provider); // ͨ��GPS��ȡλ��
		if (location != null) {
			updateNewLocation(location);
			updateNewLocation(location);
		} else {
			updateNullLocation();
		}
		// ���ü��������Զ����µ���Сʱ��Ϊ���N��(1��Ϊ1*1000������д��ҪΪ�˷���)����Сλ�Ʊ仯����N��
		lManager.requestLocationUpdates(provider, 1 * 100, 0, new locListener());

		// ��ʼ�������߳�
		if (!sendMsgThreadFlag) {
			sendMsgThread = new SendMsgThread();
			sendMsgThread.start();
		}

		// �����ٶ�����
		updateHandler.removeCallbacks(runnable);
		updateHandler.post(runnable);
	}
	/** ���¿��������� */
	private void updateNullLocation() {
		latPre = latCur;
		lngPre = lngCur;
		setLatitude(0.0);
		setLongitude(0.0);
	}
	/** ���·ǿ��������� ������ı��ʱ������ */
	private void updateNewLocation(Location location) {
		latPre = latCur;
		lngPre = lngCur;
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
		Log.i("updateDataSpeed", "��ǰ���� :" + latCur + "\t" + lngCur);
		Log.i("updateDataSpeed", "�ϸ����� :" + latPre + "\t" + lngPre);
	}
	/** �����ٶ����� �������� */
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
		Log.i("GPSDataService", "����λ�� :" + distance);
//		Log.i("updateDataSpeed", "��ǰ���� :" + latCur + "\t" + lngCur);
//		Log.i("updateDataSpeed", "�ϸ����� :" + latPre + "\t" + lngPre);
		setHorSpeed(GlobalFun.calcSpeed(distance, defaultRefrashTime));
		Log.i("GPSDataService", "�����ٶ� :" + horSpeed);
		latPre = latCur;
		lngPre = lngCur;
	}
	private class locListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			updateNewLocation(location);
		}
		/** provider ����ʱ���� */
		@Override
		public void onProviderDisabled(String provider) {
			Log.i("GPSDataService", "GPS������");
			gpsState = false;
		}
		/** provider ��ʱ���� */
		@Override
		public void onProviderEnabled(String provider) {
			Log.i("GPSDataService", "GPS������");
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
		Log.i("GPSDataService", "���� onDestroy");
		super.onDestroy();
	}

	/** �ж�GPS�豸�Ƿ��� */
	private boolean getGpsState() {
		return lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	/** �õ�GPS ������������ */
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
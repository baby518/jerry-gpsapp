package com.Jerry.GPSService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/** �̳��� BroadcastReceiver,�������� GPSDataService �㲥������ */
public class GPSReceiver extends BroadcastReceiver {
	private Bundle			bundleReceived	= new Bundle();
	private static boolean	receiverState	= false;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(GPSDataService.GPS_COMM)) {
			bundleReceived = intent.getExtras();
			Log.i("GPSDataService", "�յ�GPS״̬ : " + getGPSState());
			Log.i("GPSDataService", "�յ�Lng���� : " + getLongitude());
			Log.i("GPSDataService", "�յ�Lat���� : " + getLatitude());
			Log.i("GPSDataService", "�յ�horSpeed���� : " + getHorSpeed());
			Log.i("GPSDataService", "�յ�satelliteNum���� : " + getSatelliteNum());
			receiverState = true;
		}
	}
	public boolean getGPSState() {
		return bundleReceived.getBoolean(GPSDataService.GPS_STATE, false);
	}
	public double getLatitude() {
		return bundleReceived.getDouble(GPSDataService.GPS_LAT, 0.0);
	}
	public double getLongitude() {
		return bundleReceived.getDouble(GPSDataService.GPS_LNG, 0.0);
	}
	public double getHorSpeed() {
		return bundleReceived.getDouble(GPSDataService.GPS_HORSPEED, 0.0);
	}
	public int getSatelliteNum() {
		return bundleReceived.getInt(GPSDataService.GPS_SATELLITENUM, 0);
	}
	public boolean getReceiverState() {
		return receiverState;
	}
}

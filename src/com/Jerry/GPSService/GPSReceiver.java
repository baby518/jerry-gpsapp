package com.Jerry.GPSService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/** 继承自 BroadcastReceiver,用来接收 GPSDataService 广播的数据 */
public class GPSReceiver extends BroadcastReceiver {
	private Bundle			bundleReceived	= new Bundle();
	private static boolean	receiverState	= false;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(GPSDataService.GPS_COMM)) {
			bundleReceived = intent.getExtras();
			Log.i("GPSDataService", "收到GPS状态 : " + getGPSState());
			Log.i("GPSDataService", "收到Lng数据 : " + getLongitude());
			Log.i("GPSDataService", "收到Lat数据 : " + getLatitude());
			Log.i("GPSDataService", "收到horSpeed数据 : " + getHorSpeed());
			Log.i("GPSDataService", "收到satelliteNum数据 : " + getSatelliteNum());
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

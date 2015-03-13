package com.Jerry.GPSmain;

import com.Jerry.GPSApp.R;
import com.Jerry.GPSArea.GPSAreaActivity;
import com.Jerry.GPSService.GPSDataService;
import com.Jerry.GPSSpeed.GPSSpeedActivity;
import com.Jerry.GlobalFun.GlobalFun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private ToggleButton	gpsStateToggleBtn;
	private TextView		gpsStateTextView;
	private boolean			gpsState	= false;
	private LinearLayout	speedLayBtn;
	private LinearLayout	areaLayBtn;
	private Button			aboutAppBtn;
	private Button			exitBtn;
	private Intent			serviceIntent;
	private Intent			speedIntent;
	private Intent			areaIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 初始化成员变量
		gpsStateToggleBtn = (ToggleButton) findViewById(R.id.GPSStateToggleBtn);
		gpsStateTextView = (TextView) findViewById(R.id.GPSStateText);
		aboutAppBtn = (Button) findViewById(R.id.aboutAppBtn);
		exitBtn = (Button) findViewById(R.id.exitBtn);

		speedLayBtn = (LinearLayout) findViewById(R.id.speedLayBtn);
		areaLayBtn = (LinearLayout) findViewById(R.id.areaLayBtn);
		speedLayBtn.setClickable(true);
		areaLayBtn.setClickable(true);

		speedLayBtn.setOnTouchListener(layBtnListener);
		areaLayBtn.setOnTouchListener(layBtnListener);
		speedLayBtn.setOnFocusChangeListener(focusListener);
		areaLayBtn.setOnFocusChangeListener(focusListener);

		serviceIntent = new Intent(MainActivity.this, GPSDataService.class);
		speedIntent = new Intent(MainActivity.this, GPSSpeedActivity.class);
		areaIntent = new Intent(MainActivity.this, GPSAreaActivity.class);

		/** GPS 状态按钮监听器 */
		gpsStateToggleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 方法1.调用系统设置界面来 设置GPS状态
				// Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
				// Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);//语言选择
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent); // 此为设置完成后返回到获取界面
				// 方法2.直接通过函数来设置GPS状态
				// toggleGps(!gpsState);
			}
		});
		/** 切换到速度计界面 */
		speedLayBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(speedIntent);
			}
		});
		/** 切换到面积测量界面 */
		areaLayBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(areaIntent);
			}
		});
		/** 显示关于对话框 */
		aboutAppBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String aboutAuthor = null;
				String aboutApp = null;
				String versionName = null;
				String aboutString = null;
				try {
					aboutAuthor = getText(R.string.aboutAuthor).toString() + "\n";
					aboutApp = getText(R.string.aboutApp).toString() + "\n";
					versionName = getText(R.string.version) + ":"
						+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n";
					aboutString = aboutAuthor + aboutApp + versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				Dialog aboutDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle(getText(R.string.about)).setMessage(aboutString)
					.setNegativeButton(getText(R.string.Back), null).create();
				aboutDialog.show();
			}
		});
		/** 退出按钮监听器 */
		exitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 按下退出按钮, 创建退出对话框
//				GlobalFun.showExitDialog(MainActivity.this, getText(R.string.exit),
//					getText(R.string.exitTip));
				MainActivity.this.finish();
			}
		});
	}
	/** Layout按钮的监听器 */
	private OnTouchListener			layBtnListener	= new OnTouchListener() {
														@Override
														public boolean onTouch(View v, MotionEvent event) {
															if (event.getAction() == MotionEvent.ACTION_DOWN) {
																v.setBackgroundResource(R.drawable.buttonframe);
															} else if (event.getAction() == MotionEvent.ACTION_UP) {
																v.setBackgroundColor(Color.TRANSPARENT);
															}
															return false;
														}
													};
	/** 按钮焦点改变监听器 */
	private OnFocusChangeListener	focusListener	= new OnFocusChangeListener() {
														@Override
														public void onFocusChange(View v,
															boolean hasFocus) {
															if (hasFocus) {
																v.setBackgroundResource(R.drawable.buttonframe);
															} else {
																v.setBackgroundColor(Color.TRANSPARENT);
															}
														}
													};
	@Override
	protected void onResume() {
		gpsState = getGpsState();
		if (gpsState) {
			// 开启服务
			startService(serviceIntent);

			gpsStateTextView.setText(getText(R.string.GPSStateOnText));
		} else {
			// 停止服务
			stopService(serviceIntent);

			gpsStateTextView.setText(getText(R.string.GPSStateOffText));
		}
		gpsStateToggleBtn.setChecked(gpsState);
		super.onResume();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				// 按下返回按钮, 创建退出对话框
				GlobalFun.showExitDialog(MainActivity.this, getText(R.string.exit),
					getText(R.string.exitTip));
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		// 停止服务
		stopService(serviceIntent);
		// 完全退出
		GlobalFun.exitComplete();
		super.onDestroy();
	}
	/**
	 * 得到GPS状态.
	 * 
	 * @return 如果GPS可用,则返回真.
	 */
	private boolean getGpsState() {
		// 方法1.通过Settings.Secure.isLocationProviderEnabled (ContentResolver cr, String
		// provider)
		// boolean b2 =
		// Settings.Secure.isLocationProviderEnabled(cr,LocationManager.GPS_PROVIDER);
		// 方法2.通过LocationManager.isProviderEnabled(String provider)方法得到GPS状态
		LocationManager lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean b = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return b;
	}
}

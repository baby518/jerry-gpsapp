package com.Jerry.GPSSpeed;

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.Jerry.GPSApp.R;
import com.Jerry.GPSService.GPSDataService;
import com.Jerry.GPSService.GPSReceiver;
import com.Jerry.GlobalFun.GlobalFun;

public class GPSSpeedActivity extends Activity {
	private Button			settingBtn, exitBtn;
	private TextView		refrashTimeTextView;
	/** 刷新时间的值 单位秒 */
	public static int		speedRefrashTime;
	private double			longitude, latitude;
	private TextView		lngTextView, latTextView;
	private TextView		horSpeedTextView;
	private String			speedUnitStr;
	private double			horSpeed;
	private LinearLayout	linearLayout;
	private ScrollView		scrollView;
	private boolean			gpsState	= false;
	/** 更新速度数据 */
	private Handler			updateHandler;
	/** 刷新经纬度界面 */
	private Handler			refrashHandler;
	private GPSReceiver		receiver;
	private Handler			scrollHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speedmain);
		// 成员变量初始化
		settingBtn = (Button) findViewById(R.id.speedSettingBtn);
		exitBtn = (Button) findViewById(R.id.speedBackBtn);
		refrashTimeTextView = (TextView) findViewById(R.id.refreshTimeTextView);
		lngTextView = (TextView) findViewById(R.id.speedLngTextView);
		latTextView = (TextView) findViewById(R.id.speedLatTextView);
		horSpeedTextView = (TextView) findViewById(R.id.horSpeedTextView);
		linearLayout = (LinearLayout) findViewById(R.id.Layout);
		scrollView = (ScrollView) findViewById(R.id.ScrollView);

		updateHandler = new Handler();
		scrollHandler = new Handler();
		refrashHandler = new Handler();

		// 成员变量设置初始值
		longitude = 0.0;
		latitude = 0.0;
		lngTextView.setText("\tN/A");
		latTextView.setText("\tN/A");
		horSpeed = 0.0;

		// 得到配置文件中的设置参数
		SharedPreferences setting = getSharedPreferences(SpeedSettingActivity.SETTING, MODE_PRIVATE);
		speedRefrashTime = setting.getInt(SpeedSettingActivity.SETTING_REFRESHTIME, 1);// 默认返回1
		if (speedRefrashTime > SpeedSettingActivity.refrashTimeMax
			|| speedRefrashTime < SpeedSettingActivity.refrashTimeMin) {
			speedRefrashTime = 1;
		}
		refrashTimeTextView.setText(speedRefrashTime + getString(R.string.timeUnit));

		speedUnitStr = setting.getString(SpeedSettingActivity.SETTING_SPEEDUNIT,
			getString(R.string.speedUnitMS));// 默认返回M/S
		horSpeedTextView.setText("\t" + horSpeed + "\t" + speedUnitStr);
		// 设置字体样式
		horSpeedTextView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		horSpeedTextView.setTextColor(Color.GREEN);

		if (receiver == null)
			receiver = new GPSReceiver();
		// 注册BroadcastReceiver
		registerReceiver(receiver, new IntentFilter(GPSDataService.GPS_COMM));

		/** 设置按钮 监听器 */
		settingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 创建设置对话框
				Intent intent = new Intent(GPSSpeedActivity.this, SpeedSettingActivity.class);
				GPSSpeedActivity.this.startActivityForResult(intent, 0);
			}
		});
		/** 退出按钮 监听器 */
		exitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 创建退出对话框
//				GlobalFun.showExitDialog(GPSSpeedActivity.this, getString(R.string.backToMain),
//					getString(R.string.backTip));
				GPSSpeedActivity.this.finish();
			}
		});
	}
	@Override
	protected void onResume() {
		gpsState = getGpsState();
		// 删除handler剩余线程
		updateHandler.removeCallbacks(updateRunnable);
		refrashHandler.removeCallbacks(refrashRunnable);
		if (gpsState) {
			// 开启服务
//			startService(serviceIntent);

			// 更新速度数据
			updateHandler.post(updateRunnable);
			// 刷新经纬度界面
			refrashHandler.post(refrashRunnable);
		} else {
			// 停止服务
//			stopService(serviceIntent);
		}
		super.onResume();
	}
	/** 更新经纬度界面 */
	private Runnable	refrashRunnable	= new Runnable() {
											@Override
											public void run() {
												refrash();
												refrashHandler.postDelayed(this,
													GPSDataService.defaultRefrashTime * 1000);
											}
											/** 从receiver得到经纬度 并显示 */
											private void refrash() {
												longitude = receiver.getLongitude();
												latitude = receiver.getLatitude();

												if (0 == longitude) {
													lngTextView.setText("\tN/A");
												} else {
//													lngTextView.setText(Location.convert(longitude, Location.FORMAT_SECONDS));
													lngTextView.setText(GlobalFun
														.doubleToDegree(longitude));
												}
												if (0 == latitude) {
													latTextView.setText("\tN/A");
												} else {
//													latTextView.setText(Location.convert(latitude, Location.FORMAT_SECONDS));
													latTextView.setText(GlobalFun
														.doubleToDegree(latitude));
												}
											}
										};
	private Runnable	updateRunnable	= new Runnable() {
											@Override
											public void run() {
												updateData();
												updateHandler.postDelayed(this, speedRefrashTime * 1000);
											}
										};
	// 从receiver得到速度 并显示,以及滚动视图
	private void updateData() {
		// 单位换算, M/s 到 Km/h, 3.6倍的关系
		if (speedUnitStr.equals(getString(R.string.speedUnitMS))) {
			horSpeed = receiver.getHorSpeed();
		} else if (speedUnitStr.equals(getString(R.string.speedUnitKMH))) {
			horSpeed = receiver.getHorSpeed() * 3.6;
		}
		Log.i("updateDataSpeed", Double.toString(receiver.getHorSpeed()));

		BigDecimal horSpeedText = new BigDecimal(Double.toString(horSpeed)).setScale(2,
			BigDecimal.ROUND_HALF_UP);
		horSpeedTextView.setText("\t" + horSpeedText + "\t" + speedUnitStr);

		// 在ScrollView上显示记录
		TextView textView = new TextView(GPSSpeedActivity.this);
		textView.setTextSize(17);
		textView.setText("\t" + GlobalFun.getSystemTime() + "\t\t\t\t\t\t" + horSpeedText + "\t"
			+ speedUnitStr);
		linearLayout.addView(textView);

		// ScrollView自动滚动,用handler实现,否则得到的高度不是最新
		scrollHandler.post(new Runnable() {
			@Override
			public void run() {
				int off = linearLayout.getMeasuredHeight() - scrollView.getHeight();
				if (off > 0) {
					scrollView.scrollTo(0, off);
				}
			}
		});
	}
	@Override
	protected void onDestroy() {
		// 删除handler所有命令
		updateHandler.removeCallbacks(updateRunnable);
		refrashHandler.removeCallbacks(refrashRunnable);
		// 停止服务
//		stopService(serviceIntent);
		// 注销BroadcastReceiver
		unregisterReceiver(receiver);
		// 返回到主界面
//		Intent startMain = new Intent(GPSSpeedActivity.this, mainActivity.class);
//		startMain.addCategory(Intent.CATEGORY_HOME);
//		startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 使用
//															// Intent.FLAG_ACTIVITY_NEW_TASK
//															// 或
//															// Intent.FLAG_ACTIVITY_CLEAR_TOP 完全退出
//		startActivity(startMain);

		super.onDestroy();
	}
	/** 创建菜单 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.speedmenu, menu);
		return true;
	}
	/** 菜单处理事件 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
			case R.id.about :
				String aboutAuthor = null;
				String aboutApp = null;
				String versionName = null;
				String aboutString = null;
				try {
					aboutAuthor = getString(R.string.aboutAuthor) + "\n";
					aboutApp = getString(R.string.aboutSpeedApp) + "\n";
					versionName = getString(R.string.version) + ":"
						+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n";
					aboutString = aboutAuthor + aboutApp + versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				Dialog aboutDialog = new AlertDialog.Builder(GPSSpeedActivity.this)
					.setTitle(getString(R.string.about)).setMessage(aboutString)
					.setNegativeButton(getString(R.string.Back), null).create();
				aboutDialog.show();
				break;
			default :
				break;
		}
		return true;
	}
	/** 得到GPS状态. */
	private boolean getGpsState() {
		LocationManager lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean b = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return b;
	}
	/** 从 SettingActivity得到设置 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 0 :
				Bundle extras = data.getExtras();
				if (extras != null) {
					speedUnitStr = extras.getString(SpeedSettingActivity.SETTING_SPEEDUNIT);
					speedRefrashTime = extras.getInt(SpeedSettingActivity.SETTING_REFRESHTIME);
					refrashTimeTextView.setText(speedRefrashTime + getString(R.string.timeUnit));
					horSpeedTextView.setText(horSpeed + speedUnitStr);
				}
				break;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				// 按下返回按钮, 创建退出对话框
				GlobalFun.showExitDialog(GPSSpeedActivity.this, getString(R.string.backToMain),
					getString(R.string.backTip));
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
}

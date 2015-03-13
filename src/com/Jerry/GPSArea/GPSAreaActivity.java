package com.Jerry.GPSArea;

import java.math.BigDecimal;
import com.Jerry.GPSApp.R;
import com.Jerry.GPSService.GPSDataService;
import com.Jerry.GPSService.GPSReceiver;
import com.Jerry.GlobalFun.GlobalFun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.widget.EditText;
import android.widget.TextView;

public class GPSAreaActivity extends Activity {
	/** 用于判断对话框的常量 */
	final static int			DIALOG_SAVE	= 0x100;
	final static int			DIALOG_OPEN	= 0x101;
	/** 用于传递Intent的常量 */
	final static String			ITEM_ID		= "itemID";
	final static String			NAME_INDEX	= "nameIndex";
	/** 记录的名称列表 */
	private String[]			recordNameStrings;
	/** 数据库管理类 */
	private MyDatabaseAdapter	myDatabaseAdapter;

	private Button				settingBtn, exitBtn;
	private double				longitude, latitude;
	private TextView			lngTextView, latTextView;
	private TextView			precTextView;
	private Precision			precision;
	private String				unitString;
	private Button				startBtn, stopBtn, openBtn, saveBtn;
	private double				totalArea	= 0.0;
	private TextView			totalAreaTextView;
	private AreaView			areaView;
	public static boolean		startFlag	= false;
	/** 刷新时间的值 单位秒,用来计算 */
	private static int			areaRefrashTime;
	private boolean				gpsState	= false;
	/** 刷新areaView */
	private Handler				viewHandler;
	/** 刷新经纬度界面 */
	private Handler				refrashHandler;
	private GPSReceiver			receiver;

	private int					nameIndex	= -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("Activity Lifecycle", "GPSAreaActivity onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.areamain);

		// 成员变量初始化
		lngTextView = (TextView) findViewById(R.id.areaLngTextView);
		latTextView = (TextView) findViewById(R.id.areaLatTextView);
		precTextView = (TextView) findViewById(R.id.precisionTextView);
		startBtn = (Button) findViewById(R.id.areaStartBtn);
		stopBtn = (Button) findViewById(R.id.areaStopBtn);
		totalAreaTextView = (TextView) findViewById(R.id.areaTotalAreaTextView);
		areaView = (AreaView) findViewById(R.id.areaView);
		openBtn = (Button) findViewById(R.id.openRecordBtn);
		saveBtn = (Button) findViewById(R.id.saveRecordBtn);
		settingBtn = (Button) findViewById(R.id.areaSettingBtn);
		exitBtn = (Button) findViewById(R.id.areaBackBtn);

		// 打开或创建数据库
		myDatabaseAdapter = new MyDatabaseAdapter(this);
		myDatabaseAdapter.open();

		viewHandler = new Handler();
		refrashHandler = new Handler();

		// 成员变量设置初始值
		longitude = 0.0;
		latitude = 0.0;
		lngTextView.setText("\tN/A");
		latTextView.setText("\tN/A");
		startFlag = false;

		startBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		openBtn.setEnabled(true);
		saveBtn.setEnabled(false);

		precision = Precision.NORMAL; // 默认, 精度一般

		// 得到配置文件中的设置参数
		SharedPreferences setting = getSharedPreferences(AreaSettingActivity.SETTING, MODE_PRIVATE);
		precision = new Precision(setting.getInt(AreaSettingActivity.SETTING_PRECISION,
			Precision.NORMAL.getValue()));// 默认精度一般
		areaRefrashTime = precision.getRefrashTime();
		precTextView.setText(precision.getString());
		// 读取面积单位 默认返回平方米
		unitString = setting.getString(AreaSettingActivity.SETTING_UNIT, getString(R.string.areaUnitM));

		if (receiver == null)
			receiver = new GPSReceiver();
		// 注册BroadcastReceiver
		registerReceiver(receiver, new IntentFilter(GPSDataService.GPS_COMM));

		/** 开始按钮 监听器 */
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点开始之后先初始化
				areaView.reset();

				startFlag = true;
				areaView.setStartPoint(longitude, latitude);
//				areaView.setStartPoint(108.82449, 34.12559);
				hideTotalAreaVeiw();

				startBtn.setEnabled(false);
				stopBtn.setEnabled(true);
				openBtn.setEnabled(false);
				saveBtn.setEnabled(false);
			}
			private void hideTotalAreaVeiw() {
				totalAreaTextView.setText(getText(R.string.measuring));
			}
		});
		/** 停止按钮 监听器 */
		stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/** 测试数据 */
//				areaView.addPoint(108.82489, 34.12589);
				/** 测试数据 */

				startFlag = false;
				areaView.setStopPoint(longitude, latitude);
//				areaView.setStopPoint(108.82569, 34.12489);
				showTotalAreaVeiw();

				stopBtn.setEnabled(false);
				startBtn.setEnabled(true);
				openBtn.setEnabled(true);
				saveBtn.setEnabled(true);

				// 手动刷新一次
				areaView.postInvalidate();
			}
			private void showTotalAreaVeiw() {
				double result = 0.0;
				totalArea = areaView.getArea();
				if (unitString.equals(getString(R.string.areaUnitM))) {
					result = totalArea;
				} else if (unitString.equals(getString(R.string.areaUnitHectare))) {
					result = totalArea / 10000;
				} else if (unitString.equals(getString(R.string.areaUnitKM))) {
					result = totalArea / 1000000;
				}

				totalAreaTextView.setText(getString(R.string.totalArea) + "\t"
					+ new BigDecimal(Double.toString(result)).setScale(3, BigDecimal.ROUND_HALF_UP)
					+ "\t" + unitString);
			}
		});
		/** 打开记录按钮 监听器 */
		openBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recordNameStrings = myDatabaseAdapter.readRecordName();
				// 先删除,后显示,用来刷新数据
				removeDialog(DIALOG_OPEN);
				showDialog(DIALOG_OPEN);
			}
		});
		/** 保存记录按钮 监听器 */
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeDialog(DIALOG_SAVE);
				showDialog(DIALOG_SAVE);
			}
		});
		/** 设置按钮 监听器 */
		settingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 创建设置对话框
				Intent intent = new Intent(GPSAreaActivity.this, AreaSettingActivity.class);
				GPSAreaActivity.this.startActivityForResult(intent, 0);
			}
		});
		/** 退出按钮 监听器 */
		exitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 创建退出对话框
//				GlobalFun.showExitDialog(GPSAreaActivity.this, getText(R.string.backToMain),
//					getText(R.string.backTip));
				GPSAreaActivity.this.finish();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_SAVE :
				/** 保存对话框 */
				final EditText nameEditText = new EditText(this);
				return new AlertDialog.Builder(GPSAreaActivity.this)
					.setTitle(getString(R.string.typeRecordName)).setView(nameEditText)
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String nameString = nameEditText.getText().toString();
							// 先判断输入框内是否为空,可用 TextUtils.isEmpty(nameString) 或 equals("")
							if (!(nameString.equals(""))) {
								// 先加入到名称列表中
								int itemID = myDatabaseAdapter.addDataToNameList(nameString,
									areaView.getArea());
								// 创建相应的数据库
								if (itemID >= 0) {
									String TABLE_DETAIL = "CREATE TABLE "
										+ MyDatabaseAdapter.TABLE_DETAIL + itemID + " ("
										+ MyDatabaseAdapter.TABLE_DETAIL_ID + " INTEGER PRIMARY KEY,"
										+ MyDatabaseAdapter.TABLE_DETAIL_NUM + " INTEGER,"
										+ MyDatabaseAdapter.TABLE_DETAIL_LAT + " REAL,"
										+ MyDatabaseAdapter.TABLE_DETAIL_LNG + " REAL)";
									if (myDatabaseAdapter.createTable(TABLE_DETAIL)) {
										// 创建成功后添加数据
										myDatabaseAdapter.addDataToDetail(MyDatabaseAdapter.TABLE_DETAIL
											+ itemID, areaView.getPoints());
									}
								}
							}
						}
					}).setNegativeButton(getString(R.string.Cancel), null).create();
			case DIALOG_OPEN :
				/** 打开对话框 */
				nameIndex = -1;
				return new AlertDialog.Builder(GPSAreaActivity.this)
					.setTitle(getString(R.string.openRecord))
					.setSingleChoiceItems(recordNameStrings, -1, new DialogInterface.OnClickListener() {
						@Override
						/**单选按钮的监听器*/
						public void onClick(DialogInterface dialog, int which) {
							nameIndex = which;
						}
					})
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						/**确定按钮的监听器*/
						public void onClick(DialogInterface dialog, int which) {
							// 打开指定的数据库
							if (nameIndex >= 0) {
								int itemID = myDatabaseAdapter.getIDFromTable(
									MyDatabaseAdapter.TABLE_NAME, nameIndex);
								if (itemID >= 0) {
									// 转到show界面,传递要打开的id,从show界面打开并且读取相应的数据库
									Intent intent = new Intent(GPSAreaActivity.this,
										ShowRecordActivity.class);
									intent.putExtra(ITEM_ID, itemID);
									intent.putExtra(NAME_INDEX, nameIndex);
									startActivity(intent);
								}
							}
						}
					})
					.setNeutralButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
						@Override
						/**删除按钮的监听器*/
						public void onClick(DialogInterface dialog, int which) {
							if (nameIndex >= 0) {
								int itemID = myDatabaseAdapter.getIDFromTable(
									MyDatabaseAdapter.TABLE_NAME, nameIndex);
								if (itemID >= 0) {
									// 首先删除相应的 数据库
									if (myDatabaseAdapter.deleteTable(MyDatabaseAdapter.TABLE_DETAIL
										+ itemID)) {
										// 然后删除 记录名称表 里的数据
										myDatabaseAdapter.delDataFromNameList(nameIndex);
									}
								}
							}
						}
					}).setNegativeButton(getString(R.string.Cancel), null).create();
			default :
				return null;
		}
	}
	@Override
	protected void onResume() {
		Log.i("Activity Lifecycle", "GPSAreaActivity onResume()");
		gpsState = getGpsState();
		viewHandler.removeCallbacks(viewRunnable);
		refrashHandler.removeCallbacks(refrashRunnable);
		if (gpsState) {
			// 刷新areaView界面
			viewHandler.post(viewRunnable);
			// 刷新经纬度界面
			refrashHandler.post(refrashRunnable);
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
	/** 更新areaView */
	private Runnable	viewRunnable	= new Runnable() {
											@Override
											public void run() {
												// 界面刷新函数
												if (startFlag) {
													refrashView();
												}
												viewHandler.postDelayed(this, areaRefrashTime * 1000);
											}
											/** view刷新函数 */
											private void refrashView() {
												areaView.addPoint(longitude, latitude);
												areaView.postInvalidate();
											}
										};
	/** 重写销毁函数 */
	@Override
	protected void onDestroy() {
		Log.i("Activity Lifecycle", "GPSAreaActivity onDestroy()");
		refrashHandler.removeCallbacks(refrashRunnable);
		viewHandler.removeCallbacks(viewRunnable);
		// 注销BroadcastReceiver
		unregisterReceiver(receiver);
		// 关闭数据库
		myDatabaseAdapter.close();
		super.onDestroy();
	}
	/** 从 SettingActivity得到设置 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 0 :
				Bundle extras = data.getExtras();
				if (extras != null) {
					precision = new Precision(extras.getInt(AreaSettingActivity.SETTING_PRECISION,
						Precision.NORMAL.getValue()));
					areaRefrashTime = precision.getRefrashTime();
					precTextView.setText(precision.getString());
					unitString = extras.getString(AreaSettingActivity.SETTING_UNIT);
				}
				break;
		}
	}
	/** 创建菜单 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.areamenu, menu);
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
					aboutAuthor = getText(R.string.aboutAuthor).toString() + "\n";
					aboutApp = getText(R.string.aboutAreaApp).toString() + "\n";
					versionName = getText(R.string.version) + ":"
						+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n";
					aboutString = aboutAuthor + aboutApp + versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				Dialog aboutDialog = new AlertDialog.Builder(GPSAreaActivity.this)
					.setTitle(getText(R.string.about)).setMessage(aboutString)
					.setNegativeButton(getText(R.string.Back), null).create();
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				// 按下返回按钮, 创建退出对话框
				GlobalFun.showExitDialog(GPSAreaActivity.this, getText(R.string.backToMain),
					getText(R.string.backTip));
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
}

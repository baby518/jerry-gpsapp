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
	/** �����ж϶Ի���ĳ��� */
	final static int			DIALOG_SAVE	= 0x100;
	final static int			DIALOG_OPEN	= 0x101;
	/** ���ڴ���Intent�ĳ��� */
	final static String			ITEM_ID		= "itemID";
	final static String			NAME_INDEX	= "nameIndex";
	/** ��¼�������б� */
	private String[]			recordNameStrings;
	/** ���ݿ������ */
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
	/** ˢ��ʱ���ֵ ��λ��,�������� */
	private static int			areaRefrashTime;
	private boolean				gpsState	= false;
	/** ˢ��areaView */
	private Handler				viewHandler;
	/** ˢ�¾�γ�Ƚ��� */
	private Handler				refrashHandler;
	private GPSReceiver			receiver;

	private int					nameIndex	= -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("Activity Lifecycle", "GPSAreaActivity onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.areamain);

		// ��Ա������ʼ��
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

		// �򿪻򴴽����ݿ�
		myDatabaseAdapter = new MyDatabaseAdapter(this);
		myDatabaseAdapter.open();

		viewHandler = new Handler();
		refrashHandler = new Handler();

		// ��Ա�������ó�ʼֵ
		longitude = 0.0;
		latitude = 0.0;
		lngTextView.setText("\tN/A");
		latTextView.setText("\tN/A");
		startFlag = false;

		startBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		openBtn.setEnabled(true);
		saveBtn.setEnabled(false);

		precision = Precision.NORMAL; // Ĭ��, ����һ��

		// �õ������ļ��е����ò���
		SharedPreferences setting = getSharedPreferences(AreaSettingActivity.SETTING, MODE_PRIVATE);
		precision = new Precision(setting.getInt(AreaSettingActivity.SETTING_PRECISION,
			Precision.NORMAL.getValue()));// Ĭ�Ͼ���һ��
		areaRefrashTime = precision.getRefrashTime();
		precTextView.setText(precision.getString());
		// ��ȡ�����λ Ĭ�Ϸ���ƽ����
		unitString = setting.getString(AreaSettingActivity.SETTING_UNIT, getString(R.string.areaUnitM));

		if (receiver == null)
			receiver = new GPSReceiver();
		// ע��BroadcastReceiver
		registerReceiver(receiver, new IntentFilter(GPSDataService.GPS_COMM));

		/** ��ʼ��ť ������ */
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// �㿪ʼ֮���ȳ�ʼ��
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
		/** ֹͣ��ť ������ */
		stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/** �������� */
//				areaView.addPoint(108.82489, 34.12589);
				/** �������� */

				startFlag = false;
				areaView.setStopPoint(longitude, latitude);
//				areaView.setStopPoint(108.82569, 34.12489);
				showTotalAreaVeiw();

				stopBtn.setEnabled(false);
				startBtn.setEnabled(true);
				openBtn.setEnabled(true);
				saveBtn.setEnabled(true);

				// �ֶ�ˢ��һ��
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
		/** �򿪼�¼��ť ������ */
		openBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recordNameStrings = myDatabaseAdapter.readRecordName();
				// ��ɾ��,����ʾ,����ˢ������
				removeDialog(DIALOG_OPEN);
				showDialog(DIALOG_OPEN);
			}
		});
		/** �����¼��ť ������ */
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeDialog(DIALOG_SAVE);
				showDialog(DIALOG_SAVE);
			}
		});
		/** ���ð�ť ������ */
		settingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// �������öԻ���
				Intent intent = new Intent(GPSAreaActivity.this, AreaSettingActivity.class);
				GPSAreaActivity.this.startActivityForResult(intent, 0);
			}
		});
		/** �˳���ť ������ */
		exitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// �����˳��Ի���
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
				/** ����Ի��� */
				final EditText nameEditText = new EditText(this);
				return new AlertDialog.Builder(GPSAreaActivity.this)
					.setTitle(getString(R.string.typeRecordName)).setView(nameEditText)
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String nameString = nameEditText.getText().toString();
							// ���ж���������Ƿ�Ϊ��,���� TextUtils.isEmpty(nameString) �� equals("")
							if (!(nameString.equals(""))) {
								// �ȼ��뵽�����б���
								int itemID = myDatabaseAdapter.addDataToNameList(nameString,
									areaView.getArea());
								// ������Ӧ�����ݿ�
								if (itemID >= 0) {
									String TABLE_DETAIL = "CREATE TABLE "
										+ MyDatabaseAdapter.TABLE_DETAIL + itemID + " ("
										+ MyDatabaseAdapter.TABLE_DETAIL_ID + " INTEGER PRIMARY KEY,"
										+ MyDatabaseAdapter.TABLE_DETAIL_NUM + " INTEGER,"
										+ MyDatabaseAdapter.TABLE_DETAIL_LAT + " REAL,"
										+ MyDatabaseAdapter.TABLE_DETAIL_LNG + " REAL)";
									if (myDatabaseAdapter.createTable(TABLE_DETAIL)) {
										// �����ɹ����������
										myDatabaseAdapter.addDataToDetail(MyDatabaseAdapter.TABLE_DETAIL
											+ itemID, areaView.getPoints());
									}
								}
							}
						}
					}).setNegativeButton(getString(R.string.Cancel), null).create();
			case DIALOG_OPEN :
				/** �򿪶Ի��� */
				nameIndex = -1;
				return new AlertDialog.Builder(GPSAreaActivity.this)
					.setTitle(getString(R.string.openRecord))
					.setSingleChoiceItems(recordNameStrings, -1, new DialogInterface.OnClickListener() {
						@Override
						/**��ѡ��ť�ļ�����*/
						public void onClick(DialogInterface dialog, int which) {
							nameIndex = which;
						}
					})
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						/**ȷ����ť�ļ�����*/
						public void onClick(DialogInterface dialog, int which) {
							// ��ָ�������ݿ�
							if (nameIndex >= 0) {
								int itemID = myDatabaseAdapter.getIDFromTable(
									MyDatabaseAdapter.TABLE_NAME, nameIndex);
								if (itemID >= 0) {
									// ת��show����,����Ҫ�򿪵�id,��show����򿪲��Ҷ�ȡ��Ӧ�����ݿ�
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
						/**ɾ����ť�ļ�����*/
						public void onClick(DialogInterface dialog, int which) {
							if (nameIndex >= 0) {
								int itemID = myDatabaseAdapter.getIDFromTable(
									MyDatabaseAdapter.TABLE_NAME, nameIndex);
								if (itemID >= 0) {
									// ����ɾ����Ӧ�� ���ݿ�
									if (myDatabaseAdapter.deleteTable(MyDatabaseAdapter.TABLE_DETAIL
										+ itemID)) {
										// Ȼ��ɾ�� ��¼���Ʊ� �������
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
			// ˢ��areaView����
			viewHandler.post(viewRunnable);
			// ˢ�¾�γ�Ƚ���
			refrashHandler.post(refrashRunnable);
		}
		super.onResume();
	}

	/** ���¾�γ�Ƚ��� */
	private Runnable	refrashRunnable	= new Runnable() {
											@Override
											public void run() {
												refrash();
												refrashHandler.postDelayed(this,
													GPSDataService.defaultRefrashTime * 1000);
											}
											/** ��receiver�õ���γ�� ����ʾ */
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
	/** ����areaView */
	private Runnable	viewRunnable	= new Runnable() {
											@Override
											public void run() {
												// ����ˢ�º���
												if (startFlag) {
													refrashView();
												}
												viewHandler.postDelayed(this, areaRefrashTime * 1000);
											}
											/** viewˢ�º��� */
											private void refrashView() {
												areaView.addPoint(longitude, latitude);
												areaView.postInvalidate();
											}
										};
	/** ��д���ٺ��� */
	@Override
	protected void onDestroy() {
		Log.i("Activity Lifecycle", "GPSAreaActivity onDestroy()");
		refrashHandler.removeCallbacks(refrashRunnable);
		viewHandler.removeCallbacks(viewRunnable);
		// ע��BroadcastReceiver
		unregisterReceiver(receiver);
		// �ر����ݿ�
		myDatabaseAdapter.close();
		super.onDestroy();
	}
	/** �� SettingActivity�õ����� */
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
	/** �����˵� */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.areamenu, menu);
		return true;
	}
	/** �˵������¼� */
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
	/** �õ�GPS״̬. */
	private boolean getGpsState() {
		LocationManager lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean b = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return b;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				// ���·��ذ�ť, �����˳��Ի���
				GlobalFun.showExitDialog(GPSAreaActivity.this, getText(R.string.backToMain),
					getText(R.string.backTip));
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
}

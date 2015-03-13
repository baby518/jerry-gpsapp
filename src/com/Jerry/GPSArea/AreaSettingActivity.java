package com.Jerry.GPSArea;

import com.Jerry.GPSApp.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/** 精度 的类 */
class Precision {
	private int						mValue;
	private String					precisionString;
	private int						mRefashTime;

	public static final Precision	METICULOUS				= new Precision(1);
	public static final Precision	NORMAL					= new Precision(2);
	public static final Precision	ROUGH					= new Precision(3);

	/** 精细 */
	public static final int			METICULOUS_REFRASHTIME	= 10;
	public static final String		METICULOUS_STRING		= "精细";
	/** 一般 */
	public static final int			NORMAL_REFRASHTIME		= 20;
	public static final String		NORMAL_STRING			= "一般";
	/** 粗略 */
	public static final int			ROUGH_REFRASHTIME		= 40;
	public static final String		ROUGH_STRING			= "粗略";

	Precision(int value) {
		mValue = value;
		switch (value) {
			case 1 :
				precisionString = METICULOUS_STRING;
				mRefashTime = METICULOUS_REFRASHTIME;
				break;
			case 2 :
				precisionString = NORMAL_STRING;
				mRefashTime = NORMAL_REFRASHTIME;
				break;
			case 3 :
				precisionString = ROUGH_STRING;
				mRefashTime = ROUGH_REFRASHTIME;
				break;
		}
	}
	public int getValue() {
		return mValue;
	}
	public String getString() {
		return precisionString;
	}
	public int getRefrashTime() {
		return mRefashTime;
	}
}

public class AreaSettingActivity extends Activity {
	public static final String	SETTING				= "areaSetting";
	public static final String	SETTING_PRECISION	= "areaSettingPrecision";
	public static final String	SETTING_UNIT		= "areaSettingUnit";

	/** 精度单选框组 */
	private RadioGroup			precisionRadioGroup;
	private RadioButton			rBtnMeticulous, rBtnNormal, rBtnRough;
	/** 面积单位单选框组 */
	private RadioGroup			unitRadioGroup;
	private RadioButton			rBtnM, rBtnH, rBtnKM;

	private SharedPreferences	setting;
	private Bundle				setBundle			= new Bundle();
	private Intent				mIntent;
	private Button				saveBtn, backBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.areasetting);

		// 成员变量初始化
		precisionRadioGroup = (RadioGroup) findViewById(R.id.precisionRadioGroup);
		rBtnMeticulous = (RadioButton) findViewById(R.id.meticulous);
		rBtnNormal = (RadioButton) findViewById(R.id.normal);
		rBtnRough = (RadioButton) findViewById(R.id.rough);

		unitRadioGroup = (RadioGroup) findViewById(R.id.areaUnitRadioGroup);
		rBtnM = (RadioButton) findViewById(R.id.areaUnitM);
		rBtnH = (RadioButton) findViewById(R.id.areaUnitH);
		rBtnKM = (RadioButton) findViewById(R.id.areaUnitKM);

		mIntent = getIntent();
		setting = getSharedPreferences(SETTING, MODE_PRIVATE);

		saveBtn = (Button) findViewById(R.id.areaSaveSettingBtn);
		backBtn = (Button) findViewById(R.id.areaBackSettingBtn);

		// 读取保存的配置
		readSetting();

		int t = setBundle.getInt(SETTING_PRECISION);
		// 根据读取出的"精度"设置单选框,
		if (t == Precision.METICULOUS.getValue()) {
			precisionRadioGroup.check(R.id.meticulous);
		} else if (t == Precision.NORMAL.getValue()) {
			precisionRadioGroup.check(R.id.normal);
		} else if (t == Precision.ROUGH.getValue()) {
			precisionRadioGroup.check(R.id.rough);
		}

		String unitString = setBundle.getString(SETTING_UNIT);
		// 根据读取出的"单位"设置单选框,
		if (unitString.equals(getString(R.string.areaUnitM))) {
			unitRadioGroup.check(R.id.areaUnitM);
		} else if (unitString.equals(getString(R.string.areaUnitHectare))) {
			unitRadioGroup.check(R.id.areaUnitH);
		} else if (unitString.equals(getString(R.string.areaUnitKM))) {
			unitRadioGroup.check(R.id.areaUnitKM);
		}

		// 设置"精度"单选框 监听器
		precisionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radiogroup, int i) {
				if (i == rBtnMeticulous.getId()) {
					setBundle.putInt(SETTING_PRECISION, Precision.METICULOUS.getValue());
				} else if (i == rBtnNormal.getId()) {
					setBundle.putInt(SETTING_PRECISION, Precision.NORMAL.getValue());
				} else if (i == rBtnRough.getId()) {
					setBundle.putInt(SETTING_PRECISION, Precision.ROUGH.getValue());
				}
			}
		});
		// 设置"单位"单选框 监听器
		unitRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radiogroup, int i) {
				if (i == rBtnM.getId()) {
					setBundle.putString(SETTING_UNIT, getString(R.string.areaUnitM));
				} else if (i == rBtnH.getId()) {
					setBundle.putString(SETTING_UNIT, getString(R.string.areaUnitHectare));
				} else if (i == rBtnKM.getId()) {
					setBundle.putString(SETTING_UNIT, getString(R.string.areaUnitKM));
				}
			}
		});
		/** 保存按钮 监听器 */
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mIntent.putExtras(setBundle);
				saveSetting();
				setResult(RESULT_OK, mIntent);
				AreaSettingActivity.this.finish();
			}
		});
		/** 返回按钮 监听器 */
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED, mIntent);
				AreaSettingActivity.this.finish();
			}
		});
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				setResult(RESULT_CANCELED, mIntent);
				AreaSettingActivity.this.finish();
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	/** 保存配置 */
	private void saveSetting() {
		Editor setEditor = setting.edit();
		setEditor.putInt(SETTING_PRECISION, setBundle.getInt(SETTING_PRECISION));
		setEditor.putString(SETTING_UNIT, setBundle.getString(SETTING_UNIT));
		setEditor.commit();
	}
	/** 读取配置 */
	private void readSetting() {
		// 读取精度 默认"一般"
		setBundle.putInt(SETTING_PRECISION,
			setting.getInt(SETTING_PRECISION, Precision.NORMAL.getValue()));
		// 读取单位 默认"平方米"
		setBundle
			.putString(SETTING_UNIT, setting.getString(SETTING_UNIT, getString(R.string.areaUnitM)));
	}
}

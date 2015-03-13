package com.Jerry.GPSSpeed;

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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/** ���ý��� */
public class SpeedSettingActivity extends Activity implements OnSeekBarChangeListener {
	public static final String	SETTING				= "spdSetting";
	public static final String	SETTING_SPEEDUNIT	= "spdSettingSpeedUnit";
	public static final String	SETTING_REFRESHTIME	= "spdSettingRefreshTime";

	public final static int		refrashTimeMin		= 1;						// ˢ��ʱ����Сֵ
	public final static int		refrashTimeMax		= 5;						// ˢ��ʱ�����ֵ

	private RadioGroup			radioGroup;
	private RadioButton			rBtnM, rBtnKM;
	private SeekBar				seekBar;
	private TextView			refrashTextView;
	private Bundle				setBundle			= new Bundle();
	private Intent				mIntent;
	private SharedPreferences	setting;
	private Button				saveBtn, backBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speedsetting);

		// ��Ա������ʼ��
		mIntent = getIntent();

		radioGroup = (RadioGroup) findViewById(R.id.speedUnitRadioGroup);
		rBtnM = (RadioButton) findViewById(R.id.speedUnitM);
		rBtnKM = (RadioButton) findViewById(R.id.speedUnitKM);

		seekBar = (SeekBar) findViewById(R.id.refreshTimeSeekBar);
		refrashTextView = (TextView) findViewById(R.id.setRrefreshTime);
		setting = getSharedPreferences(SETTING, MODE_PRIVATE);

		seekBar.setMax(refrashTimeMax - 1);
		seekBar.setOnSeekBarChangeListener(this);

		saveBtn = (Button) findViewById(R.id.speedSaveSettingBtn);
		backBtn = (Button) findViewById(R.id.speedBackSettingBtn);
		// ��ȡ���������
		readSetting();

		// ���ݶ�ȡ����"�ٶȵ�λ"���õ�ѡ��
		// ʹ��equals����,����ʹ�� ==
		String string = setBundle.getString(SETTING_SPEEDUNIT);
		if (string.equals(getString(R.string.speedUnitMS))) {
			radioGroup.check(R.id.speedUnitM);
		} else if (string.equals(getString(R.string.speedUnitKMH))) {
			radioGroup.check(R.id.speedUnitKM);
		} else {
			radioGroup.check(R.id.speedUnitM);
		}
		// ���ݶ�ȡ����"ˢ��ʱ��"����seekbar
		seekBar.setProgress(setBundle.getInt(SETTING_REFRESHTIME) - 1);
		// ��ʾ"ˢ��ʱ��"�ı�
		refrashTextView.setText(setBundle.getInt(SETTING_REFRESHTIME) + ""
			+ getString(R.string.timeUnit));

		// ����"�ٶȵ�λ"��ѡ�� ������
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radiogroup, int i) {
				if (i == rBtnM.getId()) {
					setBundle.putString(SETTING_SPEEDUNIT, getString(R.string.speedUnitMS));
				} else if (i == rBtnKM.getId()) {
					setBundle.putString(SETTING_SPEEDUNIT, getString(R.string.speedUnitKMH));
				}
			}
		});
		/** ���水ť ������ */
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mIntent.putExtras(setBundle);
				saveSetting();
				setResult(RESULT_OK, mIntent);
				SpeedSettingActivity.this.finish();
			}
		});
		/** ���ذ�ť ������ */
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED, mIntent);
				SpeedSettingActivity.this.finish();
			}
		});
	}
	@Override
	public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {
		refrashTextView.setText(i + 1 + getString(R.string.timeUnit));
		setBundle.putInt(SETTING_REFRESHTIME, i + 1);
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {}
	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				setResult(RESULT_CANCELED, mIntent);
				SpeedSettingActivity.this.finish();
				break;
			default :
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	/** �������� */
	private void saveSetting() {
		Editor setEditor = setting.edit();
		setEditor.putString(SETTING_SPEEDUNIT, setBundle.getString(SETTING_SPEEDUNIT));
		setEditor.putInt(SETTING_REFRESHTIME, setBundle.getInt(SETTING_REFRESHTIME));
		setEditor.commit();
	}
	/** ��ȡ���� */
	private void readSetting() {
		// Ĭ�ϵ�λ M/s
		setBundle.putString(SETTING_SPEEDUNIT,
			setting.getString(SETTING_SPEEDUNIT, getString(R.string.speedUnitMS)));
		// Ĭ�� ˢ��ʱ��1s
		setBundle.putInt(SETTING_REFRESHTIME, setting.getInt(SETTING_REFRESHTIME, 1));
	}
}

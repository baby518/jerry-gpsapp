package com.Jerry.GPSArea;

import java.util.ArrayList;
import java.util.HashMap;
import com.Jerry.GPSApp.R;
import com.Jerry.GlobalFun.GlobalFun;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class ShowRecordActivity extends TabActivity {
	private MyDatabaseAdapter	myDatabaseAdapter;
	/** ���ϸ����� ���ݹ�����Bundle */
	private Bundle				getBundle;
	private int					getID;
//	private double				totalArea;
	/** ��¼�������б� */
	private String[]			recordNameStrings;
	/** ��ǰ��¼������ */
	private String				curRecordName;
	/** �����ļ��� */
	private ArrayList<Point>	points		= new ArrayList<Point>();

	private TabHost				tabHost;

	private LinearLayout		listViewLayout;
	private ListView			listView;
	private AreaView			areaView;

	private Button				openBtn;
	private Button				backBtn;

	private TextView			totalAreaTextView;
	private int					nameIndex	= -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arearecord);

		// ��Ա������ʼ��
		openBtn = (Button) findViewById(R.id.areaRecordOpen);
		backBtn = (Button) findViewById(R.id.areaRecordBack);
		listViewLayout = (LinearLayout) findViewById(R.id.listViewLayout);
		listView = new ListView(this);
		listViewLayout.addView(listView);

		areaView = (AreaView) findViewById(R.id.recordAreaView);
		totalAreaTextView = (TextView) findViewById(R.id.recordTotalArea);

		// �õ�Intent���ݵ�ֵ
		getBundle = this.getIntent().getExtras();
		getID = getBundle.getInt(GPSAreaActivity.ITEM_ID);
		nameIndex = getBundle.getInt(GPSAreaActivity.NAME_INDEX);

		// �����ݿ�
		myDatabaseAdapter = new MyDatabaseAdapter(this);
		myDatabaseAdapter.open();

		recordNameStrings = myDatabaseAdapter.readRecordName();
		
		if (readData()) {
			// ���±���,���� ScrollView �б�,���� AreaView ����
			UpdateTitle(true);
			UpdateList(true);
			UpdateView(true);
		}
		else {
			UpdateTitle(false);
			UpdateList(false);
			UpdateView(false);
		}

		tabHost = getTabHost();
		// ��ӹ����б���ͼ
		tabHost.addTab(tabHost.newTabSpec("Tab1").setIndicator(getString(R.string.showList))
			.setContent(R.id.recordListView));

		// ����Զ���view��ͼ
		tabHost.addTab(tabHost.newTabSpec("Tab2").setIndicator(getString(R.string.showView))
			.setContent(R.id.recordAreaView));
		tabHost.setCurrentTab(0);

		/** ���ذ�ť ������ */
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ShowRecordActivity.this.finish();
			}
		});
		/** �򿪼�¼��ť ������ */
		openBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				recordNameStrings = myDatabaseAdapter.readRecordName();
				// ��ɾ��,����ʾ,����ˢ������
				removeDialog(GPSAreaActivity.DIALOG_OPEN);
				showDialog(GPSAreaActivity.DIALOG_OPEN);
			}
		});
	}
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case GPSAreaActivity.DIALOG_OPEN :
				/** �򿪶Ի��� */
				nameIndex = -1;
				return new AlertDialog.Builder(ShowRecordActivity.this)
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
									getID = itemID;
									if (readData()) {
										// ���±���,���� ScrollView �б�,���� AreaView ����
										UpdateTitle(true);
										UpdateList(true);
										UpdateView(true);
									}
									else {
										UpdateTitle(false);
										UpdateList(false);
										UpdateView(false);
									}
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
		}
		return null;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		myDatabaseAdapter.close();
	}
	/** �����ݿ��ж�ȡ��Ϣ */
	private boolean readData() {
		try {
			// ��ȡ�������
			totalAreaTextView.setText(getString(R.string.totalArea) + "\t"
				+ myDatabaseAdapter.getAreaFromTable(nameIndex) + "\t" + getString(R.string.areaUnitM));
			// ��ȡ���������
			points = myDatabaseAdapter.readDataFromDetail(MyDatabaseAdapter.TABLE_DETAIL + getID);
			return true;
		} catch (Exception e) {
			Log.i("TABLE_DETAIL",
				"��ȡ: " + MyDatabaseAdapter.TABLE_DETAIL + getID + "\tʧ��" + e.toString());
			return false;
		}
	}
	/**ˢ�±���*/
	private void UpdateTitle(boolean b) {
		if (b) {
			curRecordName = recordNameStrings[nameIndex];
		}else{
			curRecordName = recordNameStrings[nameIndex];
//			curRecordName = getString(R.string.noRecord);
		}
		setTitle(curRecordName);
	}
	/** ˢ��List */
	private void UpdateList(boolean b) {
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();;
		SimpleAdapter adapter;
		if (b) {
			// ��ʾ����
			int num = points.size();
			for(int i = 0; i < num; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(MyDatabaseAdapter.TABLE_DETAIL_NUM, Integer.toString(i + 1));
				map.put(MyDatabaseAdapter.TABLE_DETAIL_LNG, GlobalFun.doubleToDegree(points.get(i).x));
				map.put(MyDatabaseAdapter.TABLE_DETAIL_LAT, GlobalFun.doubleToDegree(points.get(i).y));
				listItem.add(map);
			}
		}
		adapter = new SimpleAdapter(this, listItem, R.layout.listitem, new String[]{
			MyDatabaseAdapter.TABLE_DETAIL_NUM, MyDatabaseAdapter.TABLE_DETAIL_LNG,
			MyDatabaseAdapter.TABLE_DETAIL_LAT}, new int[]{R.id.listViewNum, R.id.listViewLng,
			R.id.listViewLat});

		listView.setAdapter(adapter);
	}
	/** ˢ��View */
	private void UpdateView(boolean b) {
		areaView.reset();

		if (b) {
			int num = points.size();
			for(int i = 0; i < num; i++) {
				if (0 == i) {
					areaView.setStartPoint(points.get(i).x, points.get(i).y);
				} else if (i == num - 1) {
					areaView.setStopPoint(points.get(i).x, points.get(i).y);
				} else {
					areaView.addPoint(points.get(i));
				}
			}
		}
	}
}

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
	/** 从上个界面 传递过来的Bundle */
	private Bundle				getBundle;
	private int					getID;
//	private double				totalArea;
	/** 记录的名称列表 */
	private String[]			recordNameStrings;
	/** 当前记录的名称 */
	private String				curRecordName;
	/** 坐标点的集合 */
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

		// 成员变量初始化
		openBtn = (Button) findViewById(R.id.areaRecordOpen);
		backBtn = (Button) findViewById(R.id.areaRecordBack);
		listViewLayout = (LinearLayout) findViewById(R.id.listViewLayout);
		listView = new ListView(this);
		listViewLayout.addView(listView);

		areaView = (AreaView) findViewById(R.id.recordAreaView);
		totalAreaTextView = (TextView) findViewById(R.id.recordTotalArea);

		// 得到Intent传递的值
		getBundle = this.getIntent().getExtras();
		getID = getBundle.getInt(GPSAreaActivity.ITEM_ID);
		nameIndex = getBundle.getInt(GPSAreaActivity.NAME_INDEX);

		// 打开数据库
		myDatabaseAdapter = new MyDatabaseAdapter(this);
		myDatabaseAdapter.open();

		recordNameStrings = myDatabaseAdapter.readRecordName();
		
		if (readData()) {
			// 更新标题,更新 ScrollView 列表,更新 AreaView 界面
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
		// 添加滚动列表视图
		tabHost.addTab(tabHost.newTabSpec("Tab1").setIndicator(getString(R.string.showList))
			.setContent(R.id.recordListView));

		// 添加自定义view视图
		tabHost.addTab(tabHost.newTabSpec("Tab2").setIndicator(getString(R.string.showView))
			.setContent(R.id.recordAreaView));
		tabHost.setCurrentTab(0);

		/** 返回按钮 监听器 */
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ShowRecordActivity.this.finish();
			}
		});
		/** 打开记录按钮 监听器 */
		openBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				recordNameStrings = myDatabaseAdapter.readRecordName();
				// 先删除,后显示,用来刷新数据
				removeDialog(GPSAreaActivity.DIALOG_OPEN);
				showDialog(GPSAreaActivity.DIALOG_OPEN);
			}
		});
	}
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case GPSAreaActivity.DIALOG_OPEN :
				/** 打开对话框 */
				nameIndex = -1;
				return new AlertDialog.Builder(ShowRecordActivity.this)
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
									getID = itemID;
									if (readData()) {
										// 更新标题,更新 ScrollView 列表,更新 AreaView 界面
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
		}
		return null;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		myDatabaseAdapter.close();
	}
	/** 从数据库中读取信息 */
	private boolean readData() {
		try {
			// 读取面积数据
			totalAreaTextView.setText(getString(R.string.totalArea) + "\t"
				+ myDatabaseAdapter.getAreaFromTable(nameIndex) + "\t" + getString(R.string.areaUnitM));
			// 读取坐标点数据
			points = myDatabaseAdapter.readDataFromDetail(MyDatabaseAdapter.TABLE_DETAIL + getID);
			return true;
		} catch (Exception e) {
			Log.i("TABLE_DETAIL",
				"读取: " + MyDatabaseAdapter.TABLE_DETAIL + getID + "\t失败" + e.toString());
			return false;
		}
	}
	/**刷新标题*/
	private void UpdateTitle(boolean b) {
		if (b) {
			curRecordName = recordNameStrings[nameIndex];
		}else{
			curRecordName = recordNameStrings[nameIndex];
//			curRecordName = getString(R.string.noRecord);
		}
		setTitle(curRecordName);
	}
	/** 刷新List */
	private void UpdateList(boolean b) {
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();;
		SimpleAdapter adapter;
		if (b) {
			// 显示数据
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
	/** 刷新View */
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

package com.Jerry.GPSArea;

import java.util.ArrayList;

import com.Jerry.GlobalFun.GlobalFun;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** 数据库管理类 */
public class MyDatabaseAdapter {
	/** 数据库名称 */
	private final static String	DB_NAME				= "recordDatabase.db";
	/** 数据库版本 */
	private final static int	DB_VERSION			= 1;
	/** 名称列表 */
	final static String			TABLE_NAME			= "nameList";
	final static String			TABLE_NAME_ID		= "_id";
	final static String			TABLE_NAME_DATA		= "name";
	final static String			TABLE_NAME_TIME		= "time";
	final static String			TABLE_NAME_AREA		= "area";
	final static String			CREATE_TABLE_NAME	= "CREATE TABLE " + TABLE_NAME + " ("
														+ TABLE_NAME_ID + " INTEGER PRIMARY KEY,"
														+ TABLE_NAME_DATA + " TEXT," + TABLE_NAME_AREA
														+ " REAL," + TABLE_NAME_TIME + " TEXT)";
	/** 记录的详情表 */
	final static String			TABLE_DETAIL		= "detail";
	final static String			TABLE_DETAIL_ID		= "_id";
	final static String			TABLE_DETAIL_NUM	= "num";
	final static String			TABLE_DETAIL_LNG	= "longitude";
	final static String			TABLE_DETAIL_LAT	= "latitude";

	/** 本地 Context对象 */
	private Context				context;
	/** 存储记录 名称的数据库 */
	private SQLiteDatabase		recordNameDatabase	= null;
	/** 由 DatabaseHelper 继承而来 */
	private DatabaseHelper		databaseHelper		= null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			/** 创建名称表 */
			try {
				db.execSQL(CREATE_TABLE_NAME);
			} catch (Exception e) {
				Log.i("TABLE", TABLE_NAME + "\tTABLE_Exist");
			}

		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	}

	public MyDatabaseAdapter(Context context) {
		this.context = context;
	}

	/** 打开数据库 */
	public void open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		recordNameDatabase = databaseHelper.getWritableDatabase();
	}
	/** 关闭数据库 */
	public void close() {
		databaseHelper.close();
	}
	/** 向数据库中添加表 */
	boolean createTable(String tableNameAndKey) {
		try {
			// 创建表
			recordNameDatabase.execSQL(tableNameAndKey);
			Log.i("TABLE", tableNameAndKey + "\tTABLE_create");
			return true;
		} catch (Exception e) {
			// 抛出异常说明表已经存在
			Log.i("TABLE", tableNameAndKey + "\tTABLE_Exist");
			return false;
		}
	}
	/** 从数据库中删除表 */
	boolean deleteTable(String tableName) {
		try {
			// 创建表
			recordNameDatabase.execSQL("DROP TABLE " + tableName);
			Log.i("TABLE", tableName + "\tTABLE_delete");
			return true;
		} catch (Exception e) {
			// 抛出异常说明删除错误
			Log.i("TABLE", tableName + "\tTABLE_delete_error");
			return false;
		}
	}
	/**
	 * 向 记录名称表 中添加数据
	 * 
	 * @param nameString 所要添加的名称
	 * @return 返回添加数据项的_id
	 */
	int addDataToNameList(String nameString, double area) {
		// 向表中添加测试数据
		ContentValues cv = new ContentValues();
		cv.put(TABLE_NAME_DATA, nameString);
		cv.put(TABLE_NAME_AREA, area);
		cv.put(TABLE_NAME_TIME, GlobalFun.getSystemDate());
		recordNameDatabase.insert(TABLE_NAME, null, cv);
		Log.i("nameList", "Add Data");

		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		int result = -1;
		if (cur != null) {
			cur.moveToLast();
			int idColumn = cur.getColumnIndex(TABLE_NAME_ID);
			result = cur.getInt(idColumn);
		}
		return result;
	}
	/**
	 * 从 记录名称表 中删除数据
	 * 
	 * @param index 要删除的序号,从0开始
	 * @return 返回所删除数据项的_id
	 */
	void delDataFromNameList(int index) {
		// 从表中删除数据
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		if (cur.moveToPosition(index)) {
			int idColumn = cur.getColumnIndex(TABLE_NAME_ID);
			recordNameDatabase.delete(TABLE_NAME, "_id=" + cur.getInt(idColumn), null);
		}
		Log.i("nameList", "Del Data");
	}
	/** 读取 记录名称 表的所有内容 用于对话框的显示 */
	public String[] readRecordName() {
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		int recordNum = cur.getCount();
		String[] result = new String[recordNum];
		int no = 0;

		if (cur != null && recordNum >= 0) {
			Log.i("nameList", "记录数量: " + Integer.toString(recordNum));
			// 读取数据
			if (cur.moveToFirst()) {
				do {
					int idColumn = cur.getColumnIndex(TABLE_NAME_ID);
					int nameColumn = cur.getColumnIndex(TABLE_NAME_DATA);
					int timeColumn = cur.getColumnIndex(TABLE_NAME_TIME);
					result[no] = no + 1 + ":\t" + cur.getString(nameColumn) + "\t"
						+ cur.getString(timeColumn);
					no++;
					Log.i("nameList", "_id:\t" + cur.getInt(idColumn));
				} while (cur.moveToNext());
			}
		}
		return result;
	}
	/** 从记录名称 表 得到面积 */
	public double getAreaFromTable(int index) {
		double result = -1;
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		if (cur.moveToPosition(index)) {
			int areaColumn = cur.getColumnIndex(TABLE_NAME_AREA);
			result = cur.getDouble(areaColumn);
		}
		return result;
	}
	/** 从 表中 取得某项的id */
	int getIDFromTable(String tableName, int index) throws SQLException {
		int result = -1;
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + tableName, null);
		if (cur.moveToPosition(index)) {
			int idColumn = cur.getColumnIndex(TABLE_NAME_ID);
			result = cur.getInt(idColumn);
		}
		return result;
	}
	/**
	 * 向 记录详情表 中添加坐标数据
	 * 
	 * @param tableName 表名
	 * @param points 数据
	 */
	void addDataToDetail(String tableName, ArrayList<Point> points) {
		ContentValues cv = new ContentValues();
		int length = points.size();
		for(int i = 0; i < length; i++) {
			cv.put(TABLE_DETAIL_NUM, i + 1);
			cv.put(TABLE_DETAIL_LNG, points.get(i).x);
			cv.put(TABLE_DETAIL_LAT, points.get(i).y);
			recordNameDatabase.insert(tableName, null, cv);
			Log.i("TABLE_DETAIL", "添加: " + tableName + "表:\t" + Double.toString(points.get(i).x) + "\t"
				+ Double.toString(points.get(i).y));
		}
	}
	/**
	 * 从 记录详情表 中读出坐标数据
	 * 
	 * @param tableName 表名
	 */
	ArrayList<Point> readDataFromDetail(String tableName) throws SQLException {
		ArrayList<Point> result = new ArrayList<Point>();
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + tableName, null);
		int dataNum = cur.getCount();
		Log.i("TABLE_DETAIL", "读取: " + tableName + "\t表中的数据数量: " + dataNum);
		if (cur != null && dataNum >= 0) {
			if (cur.moveToFirst()) {
				do {
					int lngColumn = cur.getColumnIndex(TABLE_DETAIL_LNG);
					int latColumn = cur.getColumnIndex(TABLE_DETAIL_LAT);
					result.add(new Point(cur.getDouble(lngColumn), cur.getDouble(latColumn)));
				} while (cur.moveToNext());
			}
		}
		return result;
	}

	/** 通过Cursor查询所有数据 */
	public Cursor fetchAllDataFromDetail(String tableName) throws SQLException {
		return recordNameDatabase.rawQuery("SELECT * FROM " + tableName, null);
	}
}

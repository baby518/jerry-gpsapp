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

/** ���ݿ������ */
public class MyDatabaseAdapter {
	/** ���ݿ����� */
	private final static String	DB_NAME				= "recordDatabase.db";
	/** ���ݿ�汾 */
	private final static int	DB_VERSION			= 1;
	/** �����б� */
	final static String			TABLE_NAME			= "nameList";
	final static String			TABLE_NAME_ID		= "_id";
	final static String			TABLE_NAME_DATA		= "name";
	final static String			TABLE_NAME_TIME		= "time";
	final static String			TABLE_NAME_AREA		= "area";
	final static String			CREATE_TABLE_NAME	= "CREATE TABLE " + TABLE_NAME + " ("
														+ TABLE_NAME_ID + " INTEGER PRIMARY KEY,"
														+ TABLE_NAME_DATA + " TEXT," + TABLE_NAME_AREA
														+ " REAL," + TABLE_NAME_TIME + " TEXT)";
	/** ��¼������� */
	final static String			TABLE_DETAIL		= "detail";
	final static String			TABLE_DETAIL_ID		= "_id";
	final static String			TABLE_DETAIL_NUM	= "num";
	final static String			TABLE_DETAIL_LNG	= "longitude";
	final static String			TABLE_DETAIL_LAT	= "latitude";

	/** ���� Context���� */
	private Context				context;
	/** �洢��¼ ���Ƶ����ݿ� */
	private SQLiteDatabase		recordNameDatabase	= null;
	/** �� DatabaseHelper �̳ж��� */
	private DatabaseHelper		databaseHelper		= null;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			/** �������Ʊ� */
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

	/** �����ݿ� */
	public void open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		recordNameDatabase = databaseHelper.getWritableDatabase();
	}
	/** �ر����ݿ� */
	public void close() {
		databaseHelper.close();
	}
	/** �����ݿ�����ӱ� */
	boolean createTable(String tableNameAndKey) {
		try {
			// ������
			recordNameDatabase.execSQL(tableNameAndKey);
			Log.i("TABLE", tableNameAndKey + "\tTABLE_create");
			return true;
		} catch (Exception e) {
			// �׳��쳣˵�����Ѿ�����
			Log.i("TABLE", tableNameAndKey + "\tTABLE_Exist");
			return false;
		}
	}
	/** �����ݿ���ɾ���� */
	boolean deleteTable(String tableName) {
		try {
			// ������
			recordNameDatabase.execSQL("DROP TABLE " + tableName);
			Log.i("TABLE", tableName + "\tTABLE_delete");
			return true;
		} catch (Exception e) {
			// �׳��쳣˵��ɾ������
			Log.i("TABLE", tableName + "\tTABLE_delete_error");
			return false;
		}
	}
	/**
	 * �� ��¼���Ʊ� ���������
	 * 
	 * @param nameString ��Ҫ��ӵ�����
	 * @return ��������������_id
	 */
	int addDataToNameList(String nameString, double area) {
		// �������Ӳ�������
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
	 * �� ��¼���Ʊ� ��ɾ������
	 * 
	 * @param index Ҫɾ�������,��0��ʼ
	 * @return ������ɾ���������_id
	 */
	void delDataFromNameList(int index) {
		// �ӱ���ɾ������
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		if (cur.moveToPosition(index)) {
			int idColumn = cur.getColumnIndex(TABLE_NAME_ID);
			recordNameDatabase.delete(TABLE_NAME, "_id=" + cur.getInt(idColumn), null);
		}
		Log.i("nameList", "Del Data");
	}
	/** ��ȡ ��¼���� ����������� ���ڶԻ������ʾ */
	public String[] readRecordName() {
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		int recordNum = cur.getCount();
		String[] result = new String[recordNum];
		int no = 0;

		if (cur != null && recordNum >= 0) {
			Log.i("nameList", "��¼����: " + Integer.toString(recordNum));
			// ��ȡ����
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
	/** �Ӽ�¼���� �� �õ���� */
	public double getAreaFromTable(int index) {
		double result = -1;
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		if (cur.moveToPosition(index)) {
			int areaColumn = cur.getColumnIndex(TABLE_NAME_AREA);
			result = cur.getDouble(areaColumn);
		}
		return result;
	}
	/** �� ���� ȡ��ĳ���id */
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
	 * �� ��¼����� �������������
	 * 
	 * @param tableName ����
	 * @param points ����
	 */
	void addDataToDetail(String tableName, ArrayList<Point> points) {
		ContentValues cv = new ContentValues();
		int length = points.size();
		for(int i = 0; i < length; i++) {
			cv.put(TABLE_DETAIL_NUM, i + 1);
			cv.put(TABLE_DETAIL_LNG, points.get(i).x);
			cv.put(TABLE_DETAIL_LAT, points.get(i).y);
			recordNameDatabase.insert(tableName, null, cv);
			Log.i("TABLE_DETAIL", "���: " + tableName + "��:\t" + Double.toString(points.get(i).x) + "\t"
				+ Double.toString(points.get(i).y));
		}
	}
	/**
	 * �� ��¼����� �ж�����������
	 * 
	 * @param tableName ����
	 */
	ArrayList<Point> readDataFromDetail(String tableName) throws SQLException {
		ArrayList<Point> result = new ArrayList<Point>();
		Cursor cur = recordNameDatabase.rawQuery("SELECT * FROM " + tableName, null);
		int dataNum = cur.getCount();
		Log.i("TABLE_DETAIL", "��ȡ: " + tableName + "\t���е���������: " + dataNum);
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

	/** ͨ��Cursor��ѯ�������� */
	public Cursor fetchAllDataFromDetail(String tableName) throws SQLException {
		return recordNameDatabase.rawQuery("SELECT * FROM " + tableName, null);
	}
}

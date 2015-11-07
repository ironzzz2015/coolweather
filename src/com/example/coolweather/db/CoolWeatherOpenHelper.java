package com.example.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {
	//province建表
	public static final String 	CREAT_PROVINCE="creat table Province ("
			+"id integer primary key autoincrement,"
			+"province_name text,"
			+"province_code text)";
	//city建表
	
	public static final String 	CREAT_CITY="creat table City ("
			+"id integer primary key autoincrement,"
			+"city_name text,"
			+"city_code text"
			+"privince_id integer)";
	//country建表
	public static final String 	CREAT_COUNTRY="creat table Country ("
			+"id integer primary key autoincrement,"
			+"country_name text,"
			+"country_code text"
			+"city_id integer)";
	
	
	public CoolWeatherOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREAT_PROVINCE);
		db.execSQL(CREAT_CITY);
		db.execSQL(CREAT_COUNTRY);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}

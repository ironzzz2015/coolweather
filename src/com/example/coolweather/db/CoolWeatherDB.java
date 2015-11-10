package com.example.coolweather.db;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.model.City;
import com.example.coolweather.model.Country;
import com.example.coolweather.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	//DB name
	public static final String DB_NAME ="cool_weather";
	
	//DB version
	
	public static final int DB_version =1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
	
	//构造方法私有化
	
	private CoolWeatherDB(Context context){
		
		CoolWeatherOpenHelper dbHelper =  new CoolWeatherOpenHelper(context, DB_NAME, null, DB_version);
		db = dbHelper.getWritableDatabase();
	}
	
	public synchronized static CoolWeatherDB getInstance(Context context){
		if (coolWeatherDB==null) {
			coolWeatherDB = new CoolWeatherDB(context);
			
		}
		return coolWeatherDB;
		
	}
	
	//存储Province实例
	
	public void saveProvince(Province province){
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	//读取province
	
	public List<Province> loadProvinces(){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor=db.query("Province", null, null, null, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
				
			} while (cursor.moveToNext());
			
		}
		return list;
		
	}
	//存储city
	public void saveCity(City city){
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	//读取city
	
	public List<City> loadCities(int provinceId){
		List<City> list = new ArrayList<City>();
		Cursor cursor=db.query("City", null, "province_id = ? ", new String[]{String.valueOf(provinceId)}, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
				
			} while (cursor.moveToNext());
			
		}
		return list;
		
	}
	
	//存储country
	
	public void saveCountry(Country country){
		if (country != null) {
			ContentValues values = new ContentValues();
			
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			db.insert("Country", null, values);
			
		}
	}
	//读取country
	
	public List<Country> loadCountries(int cityId){
		List<Country> list = new  ArrayList<Country>();
		Cursor cursor =db.query("Country", null, "city_id= ?",  new String[]{String.valueOf(cityId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Country country = new Country();
				country.setCityId(cityId);
				country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
				country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				list.add(country);
			} while (cursor.moveToNext());
		}
		return list;
	}
}

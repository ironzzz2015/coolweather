package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.Country;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	
	
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTRY=2;
	
	private boolean isFromWeatherActivity;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	//province 列表
	private  List<Province> provinceList;
	//city列表
	private List<City> cityList;
	//country列表
	private List<Country> countryList;
	
	//selected province
	private Province selectedProvince;
	
	//selected city
	private City selectedCity;
	
	//selected level currently
	private  int  currentLevel;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity= getIntent().getBooleanExtra("from_weather_activity", false);
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)&& !isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			
			return;
		}
		
		setContentView(R.layout.choose_area);
		listView = (android.widget.ListView) findViewById(R.id.list_view);
			titleText= (TextView) findViewById(R.id.title_text);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
			listView.setAdapter(adapter);
			coolWeatherDB = CoolWeatherDB.getInstance(this);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
					// TODO Auto-generated method stub
					if (currentLevel == LEVEL_PROVINCE) {
						selectedProvince = provinceList.get(index);
						queryCities();
				
					}else if (currentLevel== LEVEL_CITY) {
						selectedCity = cityList.get(index);
						queryCountries();
						
					}else if (currentLevel == LEVEL_COUNTRY) {
						String countryCode = countryList.get(index).getCountryCode();
						Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
						intent.putExtra("country_code", countryCode);
						startActivity(intent);
						finish();
					}
				}
			});
			queryProvinces();
			
	}
	
	//查询全国所有的省，优先从数据库查询，如果没有查询再到服务器上去查询；
	
	
	private void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size()>0) {
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
			
		}else {
			queryFromServer(null,"province");
		}
	}

	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		
		if (cityList.size()>0) {
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
			
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	
	private void queryCountries(){
		countryList= coolWeatherDB.loadCountries(selectedCity.getId());
		if (countryList.size()>0) {
			dataList.clear();
			for(Country country:countryList){
				dataList.add(country.getCountryName());
				
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
			
		}else{
			queryFromServer(selectedCity.getCityCode(),"country");
		}
	}
	
	//根据传入的代号和类型从服务器上查询省省市县数据
	
	
	private  void queryFromServer(final String code, final String type){
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
			
			
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result =false;
				if ("province".equals(type)) {
					result =Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if ("city".equals(type)){
					result = Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if ("country".equals(type)) {
					result = Utility.handleCountryResponse(coolWeatherDB, response, selectedCity.getId());
							
				}
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							
							if ("province".equals(type)) {
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("country".equals(type)) {
								queryCountries();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUithread()方法回到主线程逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//显示进度对话框
	
	private void showProgressDialog(){
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载。。。");
			progressDialog.setCanceledOnTouchOutside(false);
			
		}
		progressDialog.show();
	
	}
	
	
	//关闭进度对话框
	
	private void closeProgressDialog(){
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	//back按键，根据当前的级别来判断，此时应该返回城市列表。省列表、还是直接退出
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel==LEVEL_COUNTRY) {
			queryCities();
		}else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		}else {
			
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_area, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

package com.example.coolweather.activity;

import com.example.coolweather.R;
import com.example.coolweather.receiver.AutoUpdateReceiver;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{

	
	
	private LinearLayout weatherInfoLayout;
	
	private TextView cityNameText;
	
	private TextView publishText;
	
	private TextView weatherDespText;
	
	private TextView temp1Text;
	
	private TextView temp2Text;
	
	private TextView currentDateText;
	
	private Button switchCity;
	
	private Button refreshWeather;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		
		//初始化控件
		
		
		
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		
		cityNameText = (TextView) findViewById(R.id.city_name);
		
		publishText = (TextView) findViewById(R.id.publish_text);
		
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		
		temp1Text= (TextView) findViewById(R.id.temp1);
		
		temp2Text= (TextView) findViewById(R.id.temp2);
		
		currentDateText = (TextView) findViewById(R.id.current_date);
		
		
		switchCity = (Button) findViewById(R.id.switch_city);
		
		switchCity.setOnClickListener(this);
		
		refreshWeather=(Button) findViewById(R.id.refresh_weather);
		
		refreshWeather.setOnClickListener(this);
		
		String countryCode = getIntent().getStringExtra("country_code");
		
		if (!TextUtils.isEmpty(countryCode)) {
			//有县级代号时就去查询
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
			
		}else {
			//没有县级代号时就直接显示本地天气
			
			showWeather();
		}
		
	}
	
	
	//查询县级代号所对应的天气代号。
	
	private void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
		queryFromServer(address,"countryCode");
		
	}
	
	//查询天气代号速对应的天气
	
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
		
		
	}
	
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array!=null && array.length==2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new  Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}
	
	//从sharedPerferences文件中读取存储的天气信息，并显示到界面上
	private void showWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		
		cityNameText.setText(prefs.getString("city_name", ""));
		
		temp1Text.setText(prefs.getString("temp1", ""));
		
		temp2Text.setText(prefs.getString("temp2", ""));
		
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		
		publishText.setText("今天"+prefs.getString("publish_time", ""+"发布"));
		
		currentDateText.setText(prefs.getString("current_data", ""));
		
		weatherInfoLayout.setVisibility(View.VISIBLE);
		
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new  Intent(this,AutoUpdateService.class);
		startService(intent);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
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


	


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
								
			}
			break;
			
		default:
			break;
		}
	}


	
}

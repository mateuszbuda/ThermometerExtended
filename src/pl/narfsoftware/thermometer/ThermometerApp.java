package pl.narfsoftware.thermometer;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class ThermometerApp extends Application implements
		OnSharedPreferenceChangeListener
{
	static final String TAG = "ThermometerApp";

	SharedPreferences preferences;

	private SensorData sensorData;

	@Override
	public void onCreate()
	{
		super.onCreate();

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		preferences.registerOnSharedPreferenceChangeListener(this);

		sensorData = new SensorData(getApplicationContext());

		Log.d(TAG, "onCreated");
	}

	public SensorData getSensorData()
	{
		if (sensorData == null)
			sensorData = new SensorData(getApplicationContext());

		return sensorData;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		preferences = sharedPreferences;
		Log.d(TAG, "onSharedPreferenceChanged for key: " + key);

		if (key.equals(getResources().getString(R.string.prefs_save_data_key)))
		{
			Intent intent = new Intent(this, SensorService.class);

			if (preferences.getBoolean(
					getResources().getString(R.string.prefs_save_data_key),
					false))
				startService(intent);

			else
				stopService(intent);
		}
	}
}

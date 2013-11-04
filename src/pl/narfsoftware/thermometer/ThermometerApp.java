package pl.narfsoftware.thermometer;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class ThermometerApp extends Application implements
		OnSharedPreferenceChangeListener {
	static final String TAG = "ThermometerApp";

	SharedPreferences preferences;

	boolean saveTemperature = false;
	boolean saveRelativeHumidity = false;
	boolean saveAbsoluteHumidity = false;
	boolean savePressure = false;
	boolean saveDewPoint = false;
	boolean saveLight = false;
	boolean saveMagneticField = false;

	private SensorData sensorData;

	@Override
	public void onCreate() {
		super.onCreate();

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		preferences.registerOnSharedPreferenceChangeListener(this);

		sensorData = new SensorData(getApplicationContext());

		Log.d(TAG, "onCreated");
	}

	public SensorData getSensorData() {
		if (sensorData == null)
			sensorData = new SensorData(getApplicationContext());

		return sensorData;
	}

	public boolean saveAnySensor() {
		return saveTemperature || saveRelativeHumidity || saveAbsoluteHumidity
				|| savePressure || saveDewPoint || saveLight
				|| saveMagneticField;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		preferences = sharedPreferences;
		Log.d(TAG, "onSharedPreferenceChanged for key: " + key);

	}
}

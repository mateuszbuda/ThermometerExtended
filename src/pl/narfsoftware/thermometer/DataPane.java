package pl.narfsoftware.thermometer;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataPane extends ActionBarActivity implements SensorEventListener
{
	static final String TAG = "DataPane";

	SharedPreferences preferences;

	// ListView sensorsDataList;
	RelativeLayout dataPaneBackground;
	TextView test;

	SensorManager sensorManager;
	Sensor[] sensors = new Sensor[sensorsCount];

	static final int sTemprature = 0;
	static final int sRelativeHumidity = 1;
	static final int sPressure = 2;
	static final int sLight = 3;
	static final int sMagneticField = 4;
	static final int sensorsCount = 5;

	boolean showTemprature;
	boolean showRelativeHumidity;
	boolean showAbsoluteHumidity;
	boolean showPressure;
	boolean showDewPoint;
	boolean showLight;
	boolean showMagneticField;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pane);

		getOverflowMenu();

		dataPaneBackground = (RelativeLayout) findViewById(R.id.dataPaneLayout);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		List<Sensor> deviceSensors = sensorManager
				.getSensorList(Sensor.TYPE_ALL);

		sensors[sTemprature] = sensorManager
				.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		sensors[sPressure] = sensorManager
				.getDefaultSensor(Sensor.TYPE_PRESSURE);
		sensors[sRelativeHumidity] = sensorManager
				.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		sensors[sLight] = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		sensors[sMagneticField] = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		Log.d(TAG, "onCreated");
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// get current preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// get resources
		Resources resources = getResources();

		// set background color
		dataPaneBackground.setBackgroundColor(Color.parseColor(preferences
				.getString(resources
						.getString(R.string.prefs_background_color_key),
						"#FFF0F8FF")));

		// get from preferences which sensors to show
		showTemprature = preferences.getBoolean(
				resources.getString(R.string.ambient_temp_key), true);
		showRelativeHumidity = preferences.getBoolean(
				resources.getString(R.string.relative_humidity_key), true);
		showAbsoluteHumidity = preferences.getBoolean(
				resources.getString(R.string.absolute_humidity_key), false);
		showPressure = preferences.getBoolean(
				resources.getString(R.string.pressure_key), true);
		showDewPoint = preferences.getBoolean(
				resources.getString(R.string.dew_point_key), false);
		showLight = preferences.getBoolean(
				resources.getString(R.string.light_key), false);
		showMagneticField = preferences.getBoolean(
				resources.getString(R.string.magnetic_field_key), false);

		// register chosen sensors
		if (showTemprature || showAbsoluteHumidity || showDewPoint)
		{
			sensorManager.registerListener(this, sensors[sTemprature],
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(TAG, "Temperature sensor registered");
		}
		if (showRelativeHumidity || showAbsoluteHumidity || showDewPoint)
		{
			sensorManager.registerListener(this, sensors[sRelativeHumidity],
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(TAG, "Relative humidity sensor registered");
		}
		if (showPressure)
		{
			sensorManager.registerListener(this, sensors[sPressure],
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(TAG, "Pressure sensor registered");
		}
		if (showLight)
		{
			sensorManager.registerListener(this, sensors[sLight],
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(TAG, "Light sensor registered");
		}
		if (showMagneticField)
		{
			sensorManager.registerListener(this, sensors[sMagneticField],
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(TAG, "Magnetic field sensor registered");
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		// get current preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// get resources
		Resources resources = getResources();

		boolean saveData = preferences.getBoolean(
				resources.getString(R.string.prefs_save_data_key), false);

		// unregister sensors, if no longer needed
		if (!saveData)
		{
			sensorManager.unregisterListener(this);
			Log.d(TAG, "Sensors unregistered");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_pane, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_history:
			startActivity(new Intent(this, HistoryActivity.class));
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getOverflowMenu()
	{
		try
		{
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e)
		{

			e.printStackTrace();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		test = (TextView) findViewById(R.id.textViewTest);

		if (event.sensor.equals(sensors[sTemprature]))
		{
			float temperature = event.values[0];
			Log.d(TAG, "Got sensor event: " + temperature);
			test.setText(temperature + "C");
		}

		if (event.sensor.equals(sensors[sMagneticField]))
		{
			float magneticFieldX = event.values[0];
			float magneticFialdY = event.values[1];
			float magneticFieldZ = event.values[2];
			float magnetciField = magneticFieldX + magneticFialdY
					+ magneticFieldZ;

			test.setText("X = " + magneticFieldX + "uT\nY = " + magneticFialdY
					+ "uT\nZ = " + magneticFieldZ + "uT\n" + "Sum = "
					+ magnetciField + "uT\nAccuracy = " + event.accuracy);
		}

	}
}

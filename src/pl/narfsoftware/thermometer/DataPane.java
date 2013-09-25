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
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataPane extends ActionBarActivity implements SensorEventListener
{
	static final String TAG = "DataPane";

	SharedPreferences preferences;

	LinearLayout dataPaneBackground;

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

	float temperature;
	float relativeHumidity;
	float absoluteHumidity;
	float pressure;
	float dewPoint;
	float light;
	float magneticField;

	static final double A = 6.112;
	static final double m = 17.62;
	static final double Tn = 243.12;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pane);

		getOverflowMenu();

		dataPaneBackground = (LinearLayout) findViewById(R.id.dataPaneLayout);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		List<Sensor> deviceSensors = sensorManager
				.getSensorList(Sensor.TYPE_ALL);

		sensors[sTemprature] = sensorManager
				.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		sensors[sRelativeHumidity] = sensorManager
				.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		sensors[sPressure] = sensorManager
				.getDefaultSensor(Sensor.TYPE_PRESSURE);
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
		if (showTemprature && event.sensor.equals(sensors[sTemprature]))
		{
			temperature = event.values[0];
			((TextView) findViewById(R.id.textViewTemperature))
					.setText("Temperature: " + String.valueOf(temperature)
							+ " C");
			Log.d(TAG, "Got temperature sensor event: " + temperature);
		}

		if (showRelativeHumidity
				&& event.sensor.equals(sensors[sRelativeHumidity]))
		{
			relativeHumidity = event.values[0];
			((TextView) findViewById(R.id.textViewRelativeHumidity))
					.setText("Relative humidity: "
							+ String.valueOf(relativeHumidity) + " %");
			Log.d(TAG, "Got relative humidity sensor event: "
					+ relativeHumidity);
		}

		if (showAbsoluteHumidity
				&& (event.sensor.equals(sensors[sTemprature]) || event.sensor
						.equals(sensors[sRelativeHumidity])))
		{
			updateAbsoluteHumidity();
		}

		if (showPressure && event.sensor.equals(sensors[sPressure]))
		{
			pressure = event.values[0];
			((TextView) findViewById(R.id.textViewPressure))
					.setText("Pressure: " + String.valueOf(pressure) + " hPa");
			Log.d(TAG, "Got pressure sensor event: " + pressure);
		}

		if (showDewPoint
				&& (event.sensor.equals(sensors[sTemprature]) || event.sensor
						.equals(sensors[sRelativeHumidity])))
		{
			updateDewPoint();
		}

		if (showLight && event.sensor.equals(sensors[sLight]))
		{
			light = event.values[0];
			((TextView) findViewById(R.id.textViewLight)).setText("Light: "
					+ String.valueOf(light) + " lx");
			Log.d(TAG, "Got light sensor event: " + light);
		}

		if (showMagneticField && event.sensor.equals(sensors[sMagneticField]))
		{
			float magneticFieldX = event.values[0];
			float magneticFieldY = event.values[1];
			float magneticFieldZ = event.values[2];
			magneticField = magneticFieldX + magneticFieldY + magneticFieldZ;
			((TextView) findViewById(R.id.textViewMagneticField))
					.setText("Magnetic field: " + String.valueOf(magneticField)
							+ " uT");
			Log.d(TAG, "Got magnetic field sensor event: " + magneticField);
		}
	}

	private void updateAbsoluteHumidity()
	{
		absoluteHumidity = (float) (216.7 * (relativeHumidity / 100.0 * A
				* Math.exp(m * temperature / (Tn + temperature)) / (273.15 + temperature)));
		((TextView) findViewById(R.id.textViewAbsoluteHumidity))
				.setText("Absolute humidity: "
						+ String.valueOf(absoluteHumidity) + " g/m3");
		Log.d(TAG, "Absolute humidity updated: " + absoluteHumidity);
	}

	private void updateDewPoint()
	{
		double h = Math.log(relativeHumidity / 100.0) + (m * temperature)
				/ (Tn + temperature);
		dewPoint = (float) (Tn * h / (m - h));
		((TextView) findViewById(R.id.textViewDewPoint)).setText("Dew point: "
				+ String.valueOf(dewPoint) + " C");
		Log.d(TAG, "Dew point updated: " + dewPoint);
	}
}

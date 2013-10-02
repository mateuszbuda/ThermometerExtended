package pl.narfsoftware.thermometer;

import java.sql.Timestamp;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

@SuppressWarnings("deprecation")
public class SensorService extends Service implements SensorEventListener
{
	static final String TAG = "SensorService";

	SharedPreferences preferences;

	SensorData sensorData;

	SensorManager sensorManager;
	Sensor[] sensors = new Sensor[SENSORS_COUNT];

	static final int S_TEMPRATURE = 0;
	static final int S_RELATIVE_HUMIDITY = 1;
	static final int S_PRESSURE = 2;
	static final int S_LIGHT = 3;
	static final int S_MAGNETIC_FIELD = 4;
	static final int SENSORS_COUNT = 5;

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

	@Override
	public void onCreate()
	{
		super.onCreate();

		sensorData = ((ThermometerApp) getApplication()).getSensorData();

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			sensors[S_TEMPRATURE] = sensorManager
					.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			sensors[S_RELATIVE_HUMIDITY] = sensorManager
					.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		} else
			sensors[S_TEMPRATURE] = sensorManager
					.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		sensors[S_PRESSURE] = sensorManager
				.getDefaultSensor(Sensor.TYPE_PRESSURE);

		sensors[S_LIGHT] = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

		sensors[S_MAGNETIC_FIELD] = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// get current preferences
		preferences = ((ThermometerApp) getApplication()).preferences;

		// get from preferences which sensors to show
		showTemprature = preferences.getBoolean(
				getResources().getString(R.string.ambient_temp_key), true);
		showRelativeHumidity = preferences.getBoolean(
				getResources().getString(R.string.relative_humidity_key), true);
		showAbsoluteHumidity = preferences
				.getBoolean(
						getResources()
								.getString(R.string.absolute_humidity_key),
						false);
		showPressure = preferences.getBoolean(
				getResources().getString(R.string.pressure_key), true);
		showDewPoint = preferences.getBoolean(
				getResources().getString(R.string.dew_point_key), false);
		showLight = preferences.getBoolean(
				getResources().getString(R.string.light_key), false);
		showMagneticField = preferences.getBoolean(
				getResources().getString(R.string.magnetic_field_key), false);

		// register chosen sensors
		if (showTemprature || showAbsoluteHumidity || showDewPoint)
		{
			sensorManager.registerListener(this, sensors[S_TEMPRATURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Temperature sensor registered");
		}
		if (showRelativeHumidity || showAbsoluteHumidity || showDewPoint)
		{
			sensorManager.registerListener(this, sensors[S_RELATIVE_HUMIDITY],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Relative humidity sensor registered");
		}
		if (showPressure)
		{
			sensorManager.registerListener(this, sensors[S_PRESSURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Pressure sensor registered");
		}
		if (showLight)
		{
			sensorManager.registerListener(this, sensors[S_LIGHT],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Light sensor registered");
		}
		if (showMagneticField)
		{
			sensorManager.registerListener(this, sensors[S_MAGNETIC_FIELD],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Magnetic field sensor registered");
		}

		Log.d(TAG, "onStarted");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		// unregister sensors, yet no longer needed
		sensorManager.unregisterListener(this);
		Log.d(TAG, "Sensors unregistered");

		Log.d(TAG, "onDestroyed");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		Date date = new Date();

		if (showTemprature && event.sensor.equals(sensors[S_TEMPRATURE]))
		{
			temperature = event.values[0];

			sensorData.insert(DbHelper.TABLE_TEMPERATUE,
					(new Timestamp(date.getTime()).getTime()), temperature);

			Log.d(TAG, "Got temperature sensor event: " + temperature);
		}

		if (showRelativeHumidity
				&& event.sensor.equals(sensors[S_RELATIVE_HUMIDITY]))
		{
			relativeHumidity = event.values[0];

			sensorData.insert(DbHelper.TABLE_RELATIVE_HUMIDITY, (new Timestamp(
					date.getTime()).getTime()), relativeHumidity);

			Log.d(TAG, "Got relative humidity sensor event: "
					+ relativeHumidity);
		}

		if (showAbsoluteHumidity
				&& (event.sensor.equals(sensors[S_TEMPRATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY])))
		{
			updateAbsoluteHumidity();
		}

		if (showPressure && event.sensor.equals(sensors[S_PRESSURE]))
		{
			pressure = event.values[0];

			sensorData.insert(DbHelper.TABLE_PRESSURE,
					(new Timestamp(date.getTime()).getTime()), pressure);

			Log.d(TAG, "Got pressure sensor event: " + pressure);
		}

		if (showDewPoint
				&& (event.sensor.equals(sensors[S_TEMPRATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY])))
		{
			updateDewPoint();
		}

		if (showLight && event.sensor.equals(sensors[S_LIGHT]))
		{
			light = event.values[0];

			sensorData.insert(DbHelper.TABLE_LIGHT,
					(new Timestamp(date.getTime()).getTime()), light);

			Log.d(TAG, "Got light sensor event: " + light);
		}

		if (showMagneticField && event.sensor.equals(sensors[S_MAGNETIC_FIELD]))
		{
			float magneticFieldX = event.values[0];
			float magneticFieldY = event.values[1];
			float magneticFieldZ = event.values[2];
			magneticField = magneticFieldX + magneticFieldY + magneticFieldZ;

			sensorData.insert(DbHelper.TABLE_MAGNETIC_FIELD, (new Timestamp(
					date.getTime()).getTime()), magneticField);

			Log.d(TAG, "Got magnetic field sensor event: " + magneticField);
		}
	}

	private void updateAbsoluteHumidity()
	{
		Date date = new Date();

		absoluteHumidity = (float) (DataPane.ABSOLUTE_HUMIDITY_CONSTANT * (relativeHumidity
				/ DataPane.HUNDRED_PERCENT
				* DataPane.A
				* Math.exp(DataPane.M * temperature
						/ (DataPane.TN + temperature)) / (DataPane.ZERO_ABSOLUTE + temperature)));

		sensorData.insert(DbHelper.TABLE_ABSOLUTE_HUMIDITY,
				(new Timestamp(date.getTime()).getTime()), absoluteHumidity);

		Log.d(TAG, "Absolute humidity updated: " + absoluteHumidity);
	}

	private void updateDewPoint()
	{
		Date date = new Date();

		double h = Math.log(relativeHumidity / DataPane.HUNDRED_PERCENT)
				+ (DataPane.M * temperature) / (DataPane.TN + temperature);
		dewPoint = (float) (DataPane.TN * h / (DataPane.M - h));

		sensorData.insert(DbHelper.TABLE_DEW_POINT,
				(new Timestamp(date.getTime()).getTime()), dewPoint);

		Log.d(TAG, "Dew point updated: " + dewPoint);
	}
}

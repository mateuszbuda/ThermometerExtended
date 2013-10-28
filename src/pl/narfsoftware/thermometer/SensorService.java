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

	ThermometerApp app;
	SharedPreferences preferences;

	SensorData sensorData;

	SensorManager sensorManager;
	Sensor[] sensors = new Sensor[SENSORS_COUNT];

	static final String INTENT_EXTRA_SENSOR = "sensor_to_unregister";

	static final int S_TEMPERATURE = 0;
	static final int S_RELATIVE_HUMIDITY = 1;
	static final int S_PRESSURE = 2;
	static final int S_LIGHT = 3;
	static final int S_MAGNETIC_FIELD = 4;
	static final int SENSORS_COUNT = 5;

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

		app = (ThermometerApp) getApplication();

		sensorData = app.getSensorData();

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			sensors[S_TEMPERATURE] = sensorManager
					.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			sensors[S_RELATIVE_HUMIDITY] = sensorManager
					.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		} else
			sensors[S_TEMPERATURE] = sensorManager
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
		// register chosen sensors
		if (app.saveTemperature || app.saveAbsoluteHumidity || app.saveDewPoint)
		{
			sensorManager.registerListener(this, sensors[S_TEMPERATURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Temperature sensor registered");
		}
		if (app.saveRelativeHumidity || app.saveAbsoluteHumidity
				|| app.saveDewPoint)
		{
			sensorManager.registerListener(this, sensors[S_RELATIVE_HUMIDITY],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Relative humidity sensor registered");
		}
		if (app.savePressure)
		{
			sensorManager.registerListener(this, sensors[S_PRESSURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Pressure sensor registered");
		}
		if (app.saveLight)
		{
			sensorManager.registerListener(this, sensors[S_LIGHT],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Light sensor registered");
		}
		if (app.saveMagneticField)
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

		// unregister sensors, yet no longer need
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

		if (app.saveTemperature && event.sensor.equals(sensors[S_TEMPERATURE]))
		{
			temperature = event.values[0];

			sensorData.insert(DbHelper.TABLE_TEMPERATUE,
					(new Timestamp(date.getTime()).getTime()), temperature);

			Log.d(TAG, "Got temperature sensor event: " + temperature);
		}

		if (app.saveRelativeHumidity
				&& event.sensor.equals(sensors[S_RELATIVE_HUMIDITY]))
		{
			relativeHumidity = event.values[0];

			sensorData.insert(DbHelper.TABLE_RELATIVE_HUMIDITY, (new Timestamp(
					date.getTime()).getTime()), relativeHumidity);

			Log.d(TAG, "Got relative humidity sensor event: "
					+ relativeHumidity);
		}

		if (app.saveAbsoluteHumidity
				&& (event.sensor.equals(sensors[S_TEMPERATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY])))
		{
			updateAbsoluteHumidity();
		}

		if (app.savePressure && event.sensor.equals(sensors[S_PRESSURE]))
		{
			pressure = event.values[0];

			sensorData.insert(DbHelper.TABLE_PRESSURE,
					(new Timestamp(date.getTime()).getTime()), pressure);

			Log.d(TAG, "Got pressure sensor event: " + pressure);
		}

		if (app.saveDewPoint
				&& (event.sensor.equals(sensors[S_TEMPERATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY])))
		{
			updateDewPoint();
		}

		if (app.saveLight && event.sensor.equals(sensors[S_LIGHT]))
		{
			light = event.values[0];

			sensorData.insert(DbHelper.TABLE_LIGHT,
					(new Timestamp(date.getTime()).getTime()), light);

			Log.d(TAG, "Got light sensor event: " + light);
		}

		if (app.saveMagneticField
				&& event.sensor.equals(sensors[S_MAGNETIC_FIELD]))
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

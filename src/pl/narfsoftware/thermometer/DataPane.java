package pl.narfsoftware.thermometer;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataPane extends ActionBarActivity implements SensorEventListener
{
	static final String TAG = "DataPane";

	static final String[] FROM = new String[]
	{ "icon", "type", "data" };
	static final int[] TO = new int[]
	{ R.id.sensorIcon, R.id.sensorType, R.id.sensorData };

	ListView sensorsDataList;
	RelativeLayout dataPaneBackground;
	TextView test;

	SensorManager sensorManager;
	Sensor[] sensors;

	static final int sTemprature = 0;
	static final int sRelativeHumidity = 1;
	static final int sPressure = 2;
	static final int sLight = 3;
	static final int sMagneticField = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pane);

		getOverflowMenu();

		sensorsDataList = (ListView) findViewById(R.layout.sensor_data_item);

		dataPaneBackground = (RelativeLayout) findViewById(R.id.dataPaneLayout);

		dataPaneBackground.setBackgroundColor(Color
				.parseColor(PreferenceManager.getDefaultSharedPreferences(this)
						.getString("background_color", "#FFF0F8FF")));

		// List<HashMap<String, Object>> data = null;

		// SimpleAdapter adapter = new SimpleAdapter(this, data,
		// R.layout.sensor_data_item, FROM, TO);

		// sensorsDataList.setAdapter(adapter);

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
		dataPaneBackground.setBackgroundColor(Color
				.parseColor(PreferenceManager.getDefaultSharedPreferences(this)
						.getString("background_color", "#00BFB9")));

		sensorManager.registerListener(this, sTemprature,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		sensorManager.unregisterListener(this);
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
		int temperature = (int) event.values[0];
		Log.d(TAG, "Got sensor event: " + temperature);
		test.setText(temperature);
	}

}

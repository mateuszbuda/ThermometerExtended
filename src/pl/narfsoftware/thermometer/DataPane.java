package pl.narfsoftware.thermometer;

import java.lang.reflect.Field;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class DataPane extends ActionBarActivity
{
	static final String TAG = "DataPane";

	static final String[] FROM = new String[]
	{ "icon", "type", "data" };
	static final int[] TO = new int[]
	{ R.id.sensorIcon, R.id.sensorType, R.id.sensorData };

	ListView sensorsDataList;
	RelativeLayout dataPaneBackground;

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
						.getString("background_color", "#00BFB9")));

		// List<HashMap<String, Object>> data = null;

		// SimpleAdapter adapter = new SimpleAdapter(this, data,
		// R.layout.sensor_data_item, FROM, TO);

		// sensorsDataList.setAdapter(adapter);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		dataPaneBackground.setBackgroundColor(Color
				.parseColor(PreferenceManager.getDefaultSharedPreferences(this)
						.getString("background_color", "#00BFB9")));
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

}

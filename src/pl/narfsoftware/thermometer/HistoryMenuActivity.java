package pl.narfsoftware.thermometer;

import java.lang.reflect.Field;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryMenuActivity extends ActionBarActivity
{
	static final String TAG = "HistoryMenuActivity";

	Context context;

	ScrollView historyBackground;

	TextView temperatueHistory;
	TextView relativeHumidityHistory;
	TextView absoluteHumidityHistory;
	TextView pressureHistory;
	TextView dewPointHistory;
	TextView lightHistory;
	TextView magneticFieldHistory;

	boolean noData = true;

	private AlertDialog eraseDataDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_menu);
		// Show the Up button in the action bar.
		setupActionBar();

		context = this;

		historyBackground = (ScrollView) findViewById(R.id.historyBackgroundLayout);

		temperatueHistory = (TextView) findViewById(R.id.historyRowTemperature);
		relativeHumidityHistory = (TextView) findViewById(R.id.historyRowRelativeHumidity);
		absoluteHumidityHistory = (TextView) findViewById(R.id.historyRowAbsoluteHumidity);
		pressureHistory = (TextView) findViewById(R.id.historyRowPressure);
		dewPointHistory = (TextView) findViewById(R.id.historyRowDewPoint);
		lightHistory = (TextView) findViewById(R.id.historyRowLight);
		magneticFieldHistory = (TextView) findViewById(R.id.historyRowMagneticField);

		// set onClick listeners
		temperatueHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(R.string.ambient_temp_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_TEMPERATUE);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_TEMPERATURE);
				startActivity(intent);
			}
		});
		relativeHumidityHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(
						HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(
								R.string.relative_humidity_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_RELATIVE_HUMIDITY);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_RELATIVE_HUMIDITY);
				startActivity(intent);
			}
		});
		absoluteHumidityHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(
						HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(
								R.string.absolute_humidity_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_ABSOLUTE_HUMIDITY);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_ABSOLUTE_HUMIDITY);
				startActivity(intent);
			}
		});
		pressureHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(R.string.pressure_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_PRESSURE);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_PRESSURE);
				startActivity(intent);
			}
		});
		dewPointHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(R.string.dew_point_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_DEW_POINT);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_DEW_POINT);
				startActivity(intent);
			}
		});
		lightHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(R.string.light_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_LIGHT);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_LIGHT);
				startActivity(intent);
			}
		});
		magneticFieldHistory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(),
						HistoryPlotActivity.class);
				intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN,
						getResources().getString(R.string.magnetic_field_title));
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
						DbHelper.TABLE_MAGNETIC_FIELD);
				intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
						HistoryPlotActivity.UNIT_MAGNETIC_FIELD);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		historyBackground
				.setBackgroundColor(Color.parseColor(PreferenceManager
						.getDefaultSharedPreferences(this).getString(
								"background_color",
								DataPane.BACKGROUND_DEFAULT_COLOR)));
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getOverflowMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;

		case R.id.action_clear_data:

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.action_clear_data)
					.setMessage(R.string.alert_dialog_erase_data_text)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									if (!PreferenceManager
											.getDefaultSharedPreferences(
													context)
											.getBoolean(
													getResources()
															.getString(
																	R.string.prefs_save_data_key),
													true))
									{
										((ThermometerApp) getApplication())
												.getSensorData().deleteAll();
										Toast.makeText(
												context,
												getResources()
														.getString(
																R.string.data_erased_success_toast),
												Toast.LENGTH_SHORT).show();
									} else
										Toast.makeText(
												context,
												getResources()
														.getString(
																R.string.data_erased_fail_toast)
														+ "\n"
														+ getResources()
																.getString(
																		R.string.data_erased_hint_toast),
												Toast.LENGTH_SHORT).show();

								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									eraseDataDialog.cancel();
								}
							});

			eraseDataDialog = builder.create();
			eraseDataDialog.setCanceledOnTouchOutside(false);
			eraseDataDialog.show();
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		}
		return super.onOptionsItemSelected(item);
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

package pl.narfsoftware.thermometer;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class HistoryPlotActivity extends Activity
{
	static final String TAG = "HistoryPlotActivity";

	boolean saveData;

	static final String INTENT_ORIGIN = "intent_origin";
	static final String INTENT_EXTRA_TABLE_NAME = "tabe_name";
	static final String INTENT_EXTRA_UNIT = "unit";

	static final String UNIT_TEMPERATURE = "[" + (char) 0x00B0 + "C]";
	static final String UNIT_RELATIVE_HUMIDITY = "[%]";
	static final String UNIT_ABSOLUTE_HUMIDITY = Html.fromHtml(
			"[g/m<sup><small>3</small></sup>]").toString();
	static final String UNIT_PRESSURE = "[hPa]";
	static final String UNIT_DEW_POINT = "[" + (char) 0x00B0 + "C]";
	static final String UNIT_LIGHT = "[lx]";
	static final String UNIT_MAGNETIC_FIELD = "[" + (char) 0x03BC + "T]";

	static final String INTENT_FROM_TEMPERATURE = "Temerature";
	static final String INTENT_FROM_RELATIVE_HUMIDITY = "Relative Humidity";
	static final String INTENT_FROM_ABSOLUTE_HUMIDITY = "Absolute Humidity";
	static final String INTENT_FROM_PRESSURE = "Pressure";
	static final String INTENT_FROM_DEW_POINT = "Dew Point";
	static final String INTENT_FROM_LIGHT = "Light";
	static final String INTENT_FROM_MAGNETIC_FIELD = "Magnetic Field";

	static final String DATE_FORMAT_TODAY = "HH:mm:ss";
	static final String DATE_FORMAT_OLDER = "d/M/yy";

	static final long DAY = 24 * 60 * 60 * 1000;

	LinearLayout backgroundLayout;
	TextView tvUnit;
	GraphView graphView;

	SensorData sensorData;
	GraphViewSeries dataSeries;

	static final float TEXT_SIZE = 10f;
	static final int HORIZONTAL_LABELS_COUNT = 4;
	static final int VERTICAL_LABELS_COUNT = 6;
	static final int VERTICAL_LABELS_WIDTH = 46;

	private final Handler handler = new Handler();
	private Runnable timer;

	static final long ONE_SECOND = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_plot);
		setupActionBar();

		sensorData = ((ThermometerApp) getApplication()).getSensorData();

		backgroundLayout = (LinearLayout) findViewById(R.id.graph);

		tvUnit = new TextView(this);

		graphView = new LineGraphView(this, "");

		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setGridColor(Color.GRAY);
		graphView.getGraphViewStyle().setTextSize(TEXT_SIZE);
		graphView.getGraphViewStyle().setNumHorizontalLabels(
				HORIZONTAL_LABELS_COUNT);
		graphView.getGraphViewStyle().setNumVerticalLabels(
				VERTICAL_LABELS_COUNT);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(
				VERTICAL_LABELS_WIDTH);

		backgroundLayout = (LinearLayout) findViewById(R.id.graph);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		saveData = ((ThermometerApp) getApplication()).preferences.getBoolean(
				getResources().getString(R.string.prefs_save_data_key), false);

		// set background color
		backgroundLayout
				.setBackgroundColor(Color.parseColor(PreferenceManager
						.getDefaultSharedPreferences(this).getString(
								"background_color",
								DataPane.BACKGROUND_DEFAULT_COLOR)));
		// set appropriate title
		this.setTitle(getIntent().getExtras().getString(INTENT_ORIGIN)
				+ " History");

		dataSeries = new GraphViewSeries(sensorData.query(getIntent()
				.getExtras().getString(INTENT_EXTRA_TABLE_NAME)));

		if (dataSeries.getValues().length <= 1)
		{
			graphView.getGraphViewStyle().setVerticalLabelsColor(
					Color.parseColor(PreferenceManager
							.getDefaultSharedPreferences(this).getString(
									"background_color",
									DataPane.BACKGROUND_DEFAULT_COLOR)));
			graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
			Toast.makeText(
					this,
					getResources().getString(R.string.no_data_info_toast)
							+ "\n"
							+ getResources().getString(
									R.string.no_data_hint_toast),
					Toast.LENGTH_LONG).show();
		} else
		{
			// set unit
			tvUnit.setText(getIntent().getExtras().getString(INTENT_EXTRA_UNIT));
			backgroundLayout.addView(tvUnit);

			graphView.setCustomLabelFormatter(new CustomLabelFormatter()
			{
				@Override
				public String formatLabel(double value, boolean isValueX)
				{
					if (isValueX)
					{
						String date;
						String time;

						long now = new Timestamp(new Date().getTime())
								.getTime();

						Date d = new Date((long) value);

						date = new SimpleDateFormat(DATE_FORMAT_OLDER)
								.format(d);

						time = new SimpleDateFormat(DATE_FORMAT_TODAY)
								.format(d);

						return ((now - ((long) dataSeries.getValues()[0].getX())) < DAY) ? time
								: date;
					}
					return null;
				}
			});

			graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
			graphView.getGraphViewStyle().setVerticalLabelsWidth(
					VERTICAL_LABELS_WIDTH);

			graphView.addSeries(dataSeries);
			graphView.setViewPort(
					dataSeries.getValues()[0].getX(),
					dataSeries.getValues()[dataSeries.getValues().length - 1]
							.getX() - dataSeries.getValues()[0].getX());
			graphView.setScalable(true);
		}

		graphView.setScrollable(true);
		// add graph view
		backgroundLayout.addView(graphView);

		timer = new Runnable()
		{
			@Override
			public void run()
			{
				if (saveData)
				{
					dataSeries.resetData(sensorData.query(getIntent()
							.getExtras().getString(INTENT_EXTRA_TABLE_NAME)));

					graphView.scrollToEnd();
				}
				handler.postDelayed(this, ONE_SECOND);
			}
		};
		handler.postDelayed(timer, ONE_SECOND);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		handler.removeCallbacks(timer);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		graphView.removeAllSeries();

		if (graphView.getParent() != null)
			backgroundLayout.removeView(graphView);

		if (tvUnit.getParent() != null)
			backgroundLayout.removeView(tvUnit);

		if (!saveData)
			sensorData.close();
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
		}
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
		}
		return super.onOptionsItemSelected(item);
	}
}

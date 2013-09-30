package pl.narfsoftware.thermometer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class HistoryPlotActivity extends Activity
{
	static final String TAG = "HistoryPlotActivity";

	static final String INTENT_ORIGIN = "intent_origin";
	static final String INTENT_EXTRA_TABLE_NAME = "tabe_name";
	static final String INTENT_FROM_TEMPERATURE = "Temerature";
	static final String INTENT_FROM_RELATIVE_HUMIDITY = "Relative Humidity";
	static final String INTENT_FROM_ABSOLUTE_HUMIDITY = "Absolute Humidity";
	static final String INTENT_FROM_PRESSURE = "Pressure";
	static final String INTENT_FROM_DEW_POINT = "Dew Point";
	static final String INTENT_FROM_LIGHT = "Light";
	static final String INTENT_FROM_MAGNETIC_FIELD = "Magnetic Field";

	LinearLayout backgroundLayout;
	GraphView graphView;

	SensorData sensorData;
	GraphViewSeries dataSeries;

	static final float TEXT_SIZE = 10f;
	static final int HORIZONTAL_LABELS_COUNT = 6;
	static final int VERTICAL_LABELS_COUNT = 6;
	static final int VERTICAL_LABELS_WIDTH = 40;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_plot);
		setupActionBar();

		sensorData = ((ThermometerApp) getApplication()).getSensorData();

		backgroundLayout = (LinearLayout) findViewById(R.id.graph);

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
		backgroundLayout
				.setBackgroundColor(Color.parseColor(PreferenceManager
						.getDefaultSharedPreferences(this).getString(
								"background_color",
								DataPane.BACKGROUND_DEFAULT_COLOR)));

		dataSeries = sensorData.query(getIntent().getExtras().getString(
				INTENT_EXTRA_TABLE_NAME));

		this.setTitle(getIntent().getExtras().getString(INTENT_ORIGIN)
				+ " History");

		if (dataSeries.getValues().length <= 0)
		{
			graphView.getGraphViewStyle().setVerticalLabelsColor(
					Color.parseColor(PreferenceManager
							.getDefaultSharedPreferences(this).getString(
									"background_color",
									DataPane.BACKGROUND_DEFAULT_COLOR)));
			graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
		} else
		{
			graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
			graphView.getGraphViewStyle().setVerticalLabelsWidth(
					VERTICAL_LABELS_WIDTH);
		}

		graphView.addSeries(dataSeries);
		backgroundLayout.addView(graphView);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		if (graphView.getParent() != null)
			backgroundLayout.removeView(graphView);

		boolean saveData = ((ThermometerApp) getApplication()).preferences
				.getBoolean(
						getResources().getString(R.string.prefs_save_data_key),
						false);

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

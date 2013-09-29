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
import com.jjoe64.graphview.GraphView.GraphViewData;
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_plot);
		setupActionBar();

		sensorData = new SensorData(this);

		backgroundLayout = (LinearLayout) findViewById(R.id.graph);

		graphView = new LineGraphView(this, "");

		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setGridColor(Color.GRAY);
		graphView.getGraphViewStyle().setTextSize(10f);
		graphView.getGraphViewStyle().setNumHorizontalLabels(6);
		graphView.getGraphViewStyle().setNumVerticalLabels(6);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(36);

		GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[]
		{ new GraphViewData(1, 2d), new GraphViewData(2, 2d),
				new GraphViewData(3, 3d), new GraphViewData(4, 2d),
				new GraphViewData(5, 3d), new GraphViewData(6, 3d) });

		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(graphView);
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
		
		graphView.addSeries(dataSeries);
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

package pl.narfsoftware.thermometer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class HistoryActivity extends Activity
{
	static final String TAG = "HistoryActivity";

	RelativeLayout historyBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		// Show the Up button in the action bar.
		setupActionBar();
		getActionBar().setDisplayHomeAsUpEnabled(true);

		historyBackground = (RelativeLayout) findViewById(R.id.historyLayout);
		historyBackground.setBackgroundColor(Color.parseColor(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"background_color", "#FFF0F8FF")));
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		historyBackground.setBackgroundColor(Color.parseColor(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"background_color", "#00BFB9")));
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
		}
		return super.onOptionsItemSelected(item);
	}

}

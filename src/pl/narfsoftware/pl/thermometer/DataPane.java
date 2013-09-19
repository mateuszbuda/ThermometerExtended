package pl.narfsoftware.pl.thermometer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DataPane extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pane);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_pane, menu);
		return true;
	}

}

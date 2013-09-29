package pl.narfsoftware.thermometer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class SensorData
{
	static final String TAG = "SensorData";
	int id;
	long timestamp;
	double value;

	private static int ID = 0;

	Context context;

	DbHelper dbHelper;
	SQLiteDatabase database;

	public SensorData(Context context)
	{
		this.context = context;
		dbHelper = new DbHelper(context);
	}

	public void insert(String table)
	{
		database = dbHelper.getWritableDatabase();

		ContentValues values = getAsContentVaues();

		database.insertWithOnConflict(table, null, values,
				SQLiteDatabase.CONFLICT_IGNORE);
	}

	public void setSensorData(long timestamp, double value)
	{
		this.id = SensorData.ID++;
		this.timestamp = timestamp;
		this.value = value;
	}

	public GraphViewSeries query(String table)
	{
		database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(table, null, null, null, null, null,
				DbHelper.C_TIMESTAMP + " asc");

		List<GraphViewData> graphViewData = new ArrayList<GraphViewData>();

		while (cursor.moveToNext())
		{
			long timestamp = cursor.getLong(cursor
					.getColumnIndex(DbHelper.C_TIMESTAMP));
			double value = cursor.getDouble(cursor
					.getColumnIndex(DbHelper.C_VALUE));
			graphViewData.add(new GraphViewData(timestamp, value));
		}

		GraphViewSeries graphViewSeries = new GraphViewSeries(
				graphViewData.toArray(new GraphViewData[0]));

		return graphViewSeries;
	}

	private ContentValues getAsContentVaues()
	{
		ContentValues values = new ContentValues();
		values.put(DbHelper.C_ID, this.id);
		values.put(DbHelper.C_TIMESTAMP, this.timestamp);
		values.put(DbHelper.C_VALUE, this.value);

		return values;
	}
}

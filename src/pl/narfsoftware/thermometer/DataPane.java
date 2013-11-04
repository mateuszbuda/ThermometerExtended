package pl.narfsoftware.thermometer;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class DataPane extends ActionBarActivity implements SensorEventListener {
	static final String TAG = "DataPane";

	Context context;
	SharedPreferences preferences;

	LinearLayout dataPaneBaseLayout;
	ScrollView backgroundLayout;

	SensorManager sensorManager;
	Sensor[] sensors = new Sensor[SENSORS_COUNT];

	static final int S_TEMPRATURE = 0;
	static final int S_RELATIVE_HUMIDITY = 1;
	static final int S_PRESSURE = 2;
	static final int S_LIGHT = 3;
	static final int S_MAGNETIC_FIELD = 4;
	static final int SENSORS_COUNT = 5;

	boolean hasTempratureSensor = false;
	boolean hasRelativeHumiditySensor = false;
	boolean hasPressureSensor = false;
	boolean hasLightSensor = false;
	boolean hasMagneticFieldSensor = false;

	boolean showTemprature;
	boolean showRelativeHumidity;
	boolean showAbsoluteHumidity;
	boolean showPressure;
	boolean showDewPoint;
	boolean showLight;
	boolean showMagneticField;

	float temperature;
	float relativeHumidity;
	float absoluteHumidity;
	float pressure;
	float dewPoint;
	float light;
	float magneticField;

	TextView tvTemprature;
	TextView tvRelativeHumidity;
	TextView tvAbsoluteHumidity;
	TextView tvPressure;
	TextView tvDewPoint;
	TextView tvLight;
	TextView tvMagneticField;

	LinearLayout sensorDataRow;
	ImageView sensorIcon;
	LinearLayout sensorDataRowText;
	TextView sensorHeader;
	View dividingLine;
	LinearLayout dateAndTimeRow;

	TextView time;
	TextView date;
	String timeFormat = DEFAULT_TIME_FORMAT;
	String dateFormat = DEFAULT_DATE_FORMAT;
	static final String DEFAULT_TIME_FORMAT = "kk:mm a";
	static final String DEFAULT_DATE_FORMAT = "EEEE, dd MMMM";

	String temperatureUnit;
	static final int CELSIUS = 0;
	static final int FAHRENHEIT = 1;
	static final int KELVIN = 2;

	static final double A = 6.112;
	static final double M = 17.62;
	static final double TN = 243.12;
	static final double ZERO_ABSOLUTE = 273.15;
	static final double HUNDRED_PERCENT = 100.0;
	static final double FAHRENHEIT_FACTOR = 5 / 9;
	static final double FAHRENHEIT_CONSTANT = 32;
	static final double ABSOLUTE_HUMIDITY_CONSTANT = 216.7;

	static final int DATA_ROW_PADDING_LEFT = 0;
	static final int DATA_ROW_PADDING_TOP = 8;
	static final int DATA_ROW_PADDING_RIGHT = 0;
	static final int DATA_ROW_PADDING_BOTTOM = 10;

	static final int DATE_TIME_ROW_PADDING_LEFT = 0;
	static final int DATE_TIME_ROW_PADDING_TOP = 0;
	static final int DATE_TIME_ROW_PADDING_RIGHT = 0;
	static final int DATE_TIME_ROW_PADDING_BOTTOM = 8;

	static final int DIV_LINE_HEIGHT = 1;
	static final String DIV_LINE_HEX_COLOR = "#44232323";
	static final String BACKGROUND_DEFAULT_COLOR = "#FFF0F8FF";

	BroadcastReceiver minuteChangeReceiver;

	private AlertDialog eraseDataDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pane);

		context = this;
		getOverflowMenu();

		dataPaneBaseLayout = (LinearLayout) findViewById(R.id.dataPaneLayout);
		backgroundLayout = (ScrollView) findViewById(R.id.backgroundLayout);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		getSensors();

		checkSensorsAvailability();

		Log.d(TAG, "onCreated");
	}

	@Override
	public void onStart() {
		super.onStart();

		minuteChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
				if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
					Calendar calendar = Calendar.getInstance(Locale
							.getDefault());
					calendar.setTimeInMillis(new Date().getTime());

					date.setText(DateFormat.format(dateFormat, calendar));
					time.setText(DateFormat.format(timeFormat, calendar));
				}
			}
		};

		this.registerReceiver(minuteChangeReceiver, new IntentFilter(
				Intent.ACTION_TIME_TICK));
	}

	@Override
	protected void onResume() {
		super.onResume();

		customize();

		setShowSensors();

		initTextViews();

		sensorManager.unregisterListener(this);
		Log.d(TAG, "Sensors unregistered");

		registerChosenSensors();

		dataPaneBaseLayout.removeAllViews();

		removeTextViewsParent();

		addDateAndTimeRow();

		addChosenSensorsTextView();

	}

	@Override
	public void onPause() {
		super.onPause();

		// unregister sensors, yet no longer needed
		sensorManager.unregisterListener(this);
		Log.d(TAG, "Sensors unregistered");
	}

	@Override
	public void onStop() {
		super.onStop();

		if (minuteChangeReceiver != null)
			unregisterReceiver(minuteChangeReceiver);

		if (date.getParent() != null)
			((LinearLayout) date.getParent()).removeView(date);
		if (time.getParent() != null)
			((LinearLayout) time.getParent()).removeView(time);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		sensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_pane, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.action_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;

		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;

		case R.id.action_clear_data:
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.action_clear_data)
					.setMessage(R.string.alert_dialog_erase_data_text)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									((ThermometerApp) getApplication())
											.getSensorData().deleteAll();
									Toast.makeText(
											context,
											getResources()
													.getString(
															R.string.data_erased_success_toast),
											Toast.LENGTH_SHORT).show();
								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									eraseDataDialog.cancel();
								}
							});

			eraseDataDialog = builder.create();
			eraseDataDialog.setCanceledOnTouchOutside(false);
			eraseDataDialog.show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Create classes - each one to listen to its own sensor change
		if ((showTemprature || showDewPoint || showAbsoluteHumidity)
				&& event.sensor.equals(sensors[S_TEMPRATURE])) {
			temperature = event.values[0];

			if (temperatureUnit.equals(getResources().getStringArray(
					R.array.prefs_temp_unit_vals)[CELSIUS]))
				tvTemprature.setText(String.format("%.0f", temperature) + " "
						+ (char) 0x00B0 + "C");
			else if (temperatureUnit.equals(getResources().getStringArray(
					R.array.prefs_temp_unit_vals)[FAHRENHEIT]))
				tvTemprature.setText(String.format("%.0f",
						temperature * 9 / 5 + 32) + " " + (char) 0x00B0 + "F");
			else
				tvTemprature.setText(String.format("%.0f", temperature + 273)
						+ " K");

			Log.d(TAG, "Got temperature sensor event: " + temperature);
		}

		if ((showRelativeHumidity || showDewPoint || showAbsoluteHumidity)
				&& event.sensor.equals(sensors[S_RELATIVE_HUMIDITY])) {
			relativeHumidity = event.values[0];

			tvRelativeHumidity.setText(String.format("%.0f", relativeHumidity)
					+ " %");

			Log.d(TAG, "Got relative humidity sensor event: "
					+ relativeHumidity);
		}

		if (showAbsoluteHumidity
				&& (event.sensor.equals(sensors[S_TEMPRATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY]))) {
			updateAbsoluteHumidity();
		}

		if (showPressure && event.sensor.equals(sensors[S_PRESSURE])) {
			pressure = event.values[0];

			tvPressure.setText(String.format("%.0f", pressure) + " hPa");

			Log.d(TAG, "Got pressure sensor event: " + pressure);
		}

		if (showDewPoint
				&& (event.sensor.equals(sensors[S_TEMPRATURE]) || event.sensor
						.equals(sensors[S_RELATIVE_HUMIDITY]))) {
			updateDewPoint();
		}

		if (showLight && event.sensor.equals(sensors[S_LIGHT])) {
			light = event.values[0];

			tvLight.setText(String.format("%.0f", light) + " lx");

			Log.d(TAG, "Got light sensor event: " + light);
		}

		if (showMagneticField && event.sensor.equals(sensors[S_MAGNETIC_FIELD])) {
			float magneticFieldX = event.values[0];
			float magneticFieldY = event.values[1];
			float magneticFieldZ = event.values[2];
			magneticField = magneticFieldX + magneticFieldY + magneticFieldZ;

			tvMagneticField.setText(String.format("%.0f", magneticField) + " "
					+ (char) 0x03BC + "T");

			Log.d(TAG, "Got magnetic field sensor event: " + magneticField);
		}
	}

	private void updateAbsoluteHumidity() {
		absoluteHumidity = (float) (ABSOLUTE_HUMIDITY_CONSTANT * (relativeHumidity
				/ HUNDRED_PERCENT
				* A
				* Math.exp(M * temperature / (TN + temperature)) / (ZERO_ABSOLUTE + temperature)));

		tvAbsoluteHumidity.setText(Html.fromHtml(String.format("%.0f",
				absoluteHumidity) + " g/m<sup><small>3</small></sup>"));

		Log.d(TAG, "Absolute humidity updated: " + absoluteHumidity);
	}

	private void updateDewPoint() {
		double h = Math.log(relativeHumidity / HUNDRED_PERCENT)
				+ (M * temperature) / (TN + temperature);
		dewPoint = (float) (TN * h / (M - h));

		if (temperatureUnit.equals(getResources().getStringArray(
				R.array.prefs_temp_unit_vals)[CELSIUS]))
			tvDewPoint.setText(String.format("%.0f", dewPoint) + " "
					+ (char) 0x00B0 + "C");
		else if (temperatureUnit.equals(getResources().getStringArray(
				R.array.prefs_temp_unit_vals)[FAHRENHEIT]))
			tvDewPoint.setText(String.format("%.0f", dewPoint
					* FAHRENHEIT_FACTOR + FAHRENHEIT_CONSTANT)
					+ " " + (char) 0x00B0 + "F");
		else
			tvDewPoint.setText(String.format("%.0f", dewPoint + ZERO_ABSOLUTE)
					+ " K");

		Log.d(TAG, "Dew point updated: " + dewPoint);
	}

	private void getSensors() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			sensors[S_TEMPRATURE] = sensorManager
					.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		else
			sensors[S_TEMPRATURE] = sensorManager
					.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			sensors[S_RELATIVE_HUMIDITY] = sensorManager
					.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

		sensors[S_PRESSURE] = sensorManager
				.getDefaultSensor(Sensor.TYPE_PRESSURE);

		sensors[S_LIGHT] = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

		sensors[S_MAGNETIC_FIELD] = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	private void checkSensorsAvailability() {
		try {
			hasTempratureSensor = sensorManager.registerListener(this,
					sensors[S_TEMPRATURE], SensorManager.SENSOR_DELAY_UI);
		} catch (Exception e) {
			hasTempratureSensor = false;
		}
		try {
			hasRelativeHumiditySensor = sensorManager
					.registerListener(this, sensors[S_RELATIVE_HUMIDITY],
							SensorManager.SENSOR_DELAY_UI);
		} catch (Exception e) {
			hasRelativeHumiditySensor = false;
		}
		try {
			hasPressureSensor = sensorManager.registerListener(this,
					sensors[S_PRESSURE], SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Pressure sensor registered");
		} catch (Exception e) {
			hasPressureSensor = false;
		}
		try {
			hasLightSensor = sensorManager.registerListener(this,
					sensors[S_LIGHT], SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Light sensor registered");
		} catch (Exception e) {
			hasLightSensor = false;
		}
		try {
			hasMagneticFieldSensor = sensorManager.registerListener(this,
					sensors[S_MAGNETIC_FIELD], SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Magnetic field sensor registered");
		} catch (Exception e) {
			hasMagneticFieldSensor = false;
		}

		sensorManager.unregisterListener(this);
	}

	private void initTextViews() {
		// initialize TextViews
		initializeTextViewSensor(tvTemprature = new TextView(this),
				hasTempratureSensor);
		initializeTextViewSensor(tvRelativeHumidity = new TextView(this),
				hasRelativeHumiditySensor);
		initializeTextViewSensor(tvAbsoluteHumidity = new TextView(this),
				hasTempratureSensor && hasRelativeHumiditySensor);
		initializeTextViewSensor(tvPressure = new TextView(this),
				hasPressureSensor);
		initializeTextViewSensor(tvDewPoint = new TextView(this),
				hasTempratureSensor && hasRelativeHumiditySensor);
		initializeTextViewSensor(tvLight = new TextView(this), hasLightSensor);
		initializeTextViewSensor(tvMagneticField = new TextView(this),
				hasMagneticFieldSensor);

		// date and time initialization
		date = new TextView(this);
		time = new TextView(this);
	}

	private void initializeTextViewSensor(TextView tvSensor, boolean hasSensor) {
		tvSensor.setGravity(Gravity.CENTER);
		tvSensor.setTextAppearance(this, android.R.style.TextAppearance_Large);
		if (!hasSensor)
			tvSensor.setText(getResources().getString(
					R.string.sensor_unavailable));
		else
			tvSensor.setText(getResources().getString(R.string.sensor_no_data));
	}

	private void customize() {
		// get current preferences
		preferences = ((ThermometerApp) getApplication()).preferences;

		// set background color
		backgroundLayout.setBackgroundColor(Color.parseColor(preferences
				.getString(
						getResources().getString(
								R.string.prefs_background_color_key),
						BACKGROUND_DEFAULT_COLOR)));

		// set temperature unit
		temperatureUnit = preferences.getString(
				getResources().getString(R.string.prefs_temp_unit_key),
				getResources().getStringArray(R.array.prefs_temp_unit_vals)[0]);

		// set date and time format
		dateFormat = preferences.getString(
				getResources().getString(R.string.prefs_date_format_key),
				DEFAULT_DATE_FORMAT);
		timeFormat = preferences.getString(
				getResources().getString(R.string.prefs_time_format_key),
				DEFAULT_TIME_FORMAT);
	}

	private void setShowSensors() {
		// get current preferences
		preferences = ((ThermometerApp) getApplication()).preferences;

		// get from preferences which sensors to show
		showTemprature = preferences.getBoolean(
				getResources().getString(R.string.ambient_temp_key), true);

		showRelativeHumidity = preferences.getBoolean(
				getResources().getString(R.string.relative_humidity_key), true);
		showAbsoluteHumidity = preferences
				.getBoolean(
						getResources()
								.getString(R.string.absolute_humidity_key),
						false);
		showPressure = preferences.getBoolean(
				getResources().getString(R.string.pressure_key), true);
		showDewPoint = preferences.getBoolean(
				getResources().getString(R.string.dew_point_key), false);
		showLight = preferences.getBoolean(
				getResources().getString(R.string.light_key), false);
		showMagneticField = preferences.getBoolean(
				getResources().getString(R.string.magnetic_field_key), false);
	}

	private void registerChosenSensors() {
		// TODO Register sensors on their own listener
		if (hasTempratureSensor
				&& (showTemprature || showAbsoluteHumidity || showDewPoint)) {
			sensorManager.registerListener(this, sensors[S_TEMPRATURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Temperature sensor registered");
		}
		if (hasRelativeHumiditySensor
				&& (showRelativeHumidity || showAbsoluteHumidity || showDewPoint)) {
			sensorManager.registerListener(this, sensors[S_RELATIVE_HUMIDITY],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Relative humidity sensor registered");
		}
		if (hasPressureSensor && showPressure) {
			sensorManager.registerListener(this, sensors[S_PRESSURE],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Pressure sensor registered");
		}
		if (hasLightSensor && showLight) {
			sensorManager.registerListener(this, sensors[S_LIGHT],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Light sensor registered");
		}
		if (showMagneticField) {
			sensorManager.registerListener(this, sensors[S_MAGNETIC_FIELD],
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "Magnetic field sensor registered");
		}
	}

	private void removeTextViewsParent() {
		// remove TextViews parents
		if (tvTemprature.getParent() != null)
			((LinearLayout) tvTemprature.getParent()).removeView(tvTemprature);
		if (tvRelativeHumidity.getParent() != null)
			((LinearLayout) tvRelativeHumidity.getParent())
					.removeView(tvRelativeHumidity);
		if (tvAbsoluteHumidity.getParent() != null)
			((LinearLayout) tvAbsoluteHumidity.getParent())
					.removeView(tvAbsoluteHumidity);
		if (tvPressure.getParent() != null)
			((LinearLayout) tvPressure.getParent()).removeView(tvPressure);
		if (tvDewPoint.getParent() != null)
			((LinearLayout) tvDewPoint.getParent()).removeView(tvDewPoint);
		if (tvLight.getParent() != null)
			((LinearLayout) tvLight.getParent()).removeView(tvLight);
		if (tvMagneticField.getParent() != null)
			((LinearLayout) tvMagneticField.getParent())
					.removeView(tvMagneticField);
	}

	private void addChosenSensorsTextView() {
		if (showTemprature)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveTemperature ? R.drawable.temprature
							: R.drawable.temprature_disabled,
					R.string.ambient_temp_title, tvTemprature,
					temperatureIconOnClickListener,
					temperatueHistoryOnClickListener);

		if (showRelativeHumidity)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveRelativeHumidity ? R.drawable.relative_humidity
							: R.drawable.relative_humidity_disabled,
					R.string.relative_humidity_title, tvRelativeHumidity,
					relativeHumidityIconOnClickListener,
					relativeHumidityHistoryOnClickListener);

		if (showAbsoluteHumidity)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveAbsoluteHumidity ? R.drawable.absolute_humidity
							: R.drawable.absolute_humidity_disabled,
					R.string.absolute_humidity_title, tvAbsoluteHumidity,
					absoluteHumidityIconOnClickListener,
					absoluteHumidityHistoryOnClickListener);

		if (showPressure)
			addSensorDataRow(
					((ThermometerApp) getApplication()).savePressure ? R.drawable.pressure
							: R.drawable.pressure_disabled,
					R.string.pressure_title, tvPressure,
					pressureIconOnClickListener, pressureHistoryOnClickListener);

		if (showDewPoint)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveDewPoint ? R.drawable.dew_point
							: R.drawable.dew_point_disabled,
					R.string.dew_point_title, tvDewPoint,
					dewPointIconOnClickListener, dewPointHistoryOnClickListener);

		if (showLight)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveLight ? R.drawable.light
							: R.drawable.light_disabled, R.string.light_title,
					tvLight, lightIconOnClickListener,
					lightHistoryOnClickListener);

		if (showMagneticField)
			addSensorDataRow(
					((ThermometerApp) getApplication()).saveMagneticField ? R.drawable.magnetic_field
							: R.drawable.magnetic_field_disabled,
					R.string.magnetic_field_title, tvMagneticField,
					magneticFieldIconOnClickListener,
					magneticFieldHistoryOnClickListener);

	}

	private void addSensorDataRow(int iconResId, int titleResId,
			TextView tvSensor, OnClickListener iconOnClickListener,
			OnClickListener textOnClickListener) {
		sensorDataRow = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		sensorDataRow.setLayoutParams(params);
		sensorDataRow.setOrientation(LinearLayout.HORIZONTAL);
		sensorDataRow.setPadding(DATA_ROW_PADDING_LEFT, DATA_ROW_PADDING_TOP,
				DATA_ROW_PADDING_RIGHT, DATA_ROW_PADDING_BOTTOM);
		sensorDataRow.setGravity(Gravity.CENTER);

		sensorIcon = new ImageView(this);
		sensorIcon.setImageResource(iconResId);
		sensorIcon.setOnClickListener(iconOnClickListener);

		sensorDataRowText = new LinearLayout(this);
		sensorDataRowText.setOrientation(LinearLayout.VERTICAL);
		sensorDataRowText.setLayoutParams(params);
		sensorDataRowText.setOnClickListener(textOnClickListener);

		sensorHeader = new TextView(this);
		sensorHeader.setText(titleResId);
		sensorHeader.setGravity(Gravity.CENTER);
		sensorHeader.setTextAppearance(this,
				android.R.style.TextAppearance_Large);

		sensorDataRow.addView(sensorIcon);
		sensorDataRowText.addView(sensorHeader);
		sensorDataRowText.addView(tvSensor);
		sensorDataRow.addView(sensorDataRowText);

		dataPaneBaseLayout.addView(sensorDataRow);

		dividingLine = new View(this);
		dividingLine.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.FILL_PARENT, DIV_LINE_HEIGHT));
		dividingLine.setBackgroundColor(Color.parseColor(DIV_LINE_HEX_COLOR));

		dataPaneBaseLayout.addView(dividingLine);
	}

	private void addDateAndTimeRow() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(new Date().getTime());

		date.setText(DateFormat.format(dateFormat, calendar));
		time.setText(DateFormat.format(timeFormat, calendar));

		dateAndTimeRow = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		dateAndTimeRow.setLayoutParams(params);
		dateAndTimeRow.setOrientation(LinearLayout.VERTICAL);
		dateAndTimeRow.setPadding(DATE_TIME_ROW_PADDING_LEFT,
				DATE_TIME_ROW_PADDING_TOP, DATE_TIME_ROW_PADDING_RIGHT,
				DATE_TIME_ROW_PADDING_BOTTOM);
		dateAndTimeRow.setGravity(Gravity.CENTER);

		if (!dateFormat.equals(""))
			dateAndTimeRow.addView(date);
		if (!timeFormat.equals(""))
			dateAndTimeRow.addView(time);

		dataPaneBaseLayout.addView(dateAndTimeRow);

		dividingLine = new View(this);
		dividingLine.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.FILL_PARENT, DIV_LINE_HEIGHT));
		dividingLine.setBackgroundColor(Color.parseColor(DIV_LINE_HEX_COLOR));

		dataPaneBaseLayout.addView(dividingLine);

		Log.d(TAG, "Date and time row added");
	}

	private boolean sensorOnClickListener(ImageView sensorIcon,
			boolean saveSensorBeforeClicked, int disabledIconResId,
			int iconResId, int toastOffResId, int toastOnResId) {
		sensorIcon.setImageResource(saveSensorBeforeClicked ? disabledIconResId
				: iconResId);
		Toast.makeText(getApplicationContext(),
				saveSensorBeforeClicked ? toastOffResId : toastOnResId,
				Toast.LENGTH_SHORT).show();
		return !saveSensorBeforeClicked;
	}

	private OnClickListener temperatureIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveTemperature = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveTemperature,
					R.drawable.temprature_disabled, R.drawable.temprature,
					R.string.toast_off_temperature_save_data,
					R.string.toast_on_temperature_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener relativeHumidityIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveRelativeHumidity = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveRelativeHumidity,
					R.drawable.relative_humidity_disabled,
					R.drawable.relative_humidity,
					R.string.toast_off_relative_humidity_save_data,
					R.string.toast_on_relative_humidity_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener absoluteHumidityIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveAbsoluteHumidity = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveAbsoluteHumidity,
					R.drawable.absolute_humidity_disabled,
					R.drawable.absolute_humidity,
					R.string.toast_off_absolute_humidity_save_data,
					R.string.toast_on_absolute_humidity_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener pressureIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).savePressure = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).savePressure,
					R.drawable.pressure_disabled, R.drawable.pressure,
					R.string.toast_off_pressure_save_data,
					R.string.toast_on_pressure_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener dewPointIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveDewPoint = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveDewPoint,
					R.drawable.dew_point_disabled, R.drawable.dew_point,
					R.string.toast_off_dew_point_save_data,
					R.string.toast_on_dew_point_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener lightIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveLight = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveLight,
					R.drawable.light_disabled, R.drawable.light,
					R.string.toast_off_light_save_data,
					R.string.toast_on_light_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener magneticFieldIconOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopService(new Intent(getBaseContext(), SensorService.class));
			((ThermometerApp) getApplication()).saveMagneticField = sensorOnClickListener(
					(ImageView) v,
					((ThermometerApp) getApplication()).saveMagneticField,
					R.drawable.magnetic_field_disabled,
					R.drawable.magnetic_field,
					R.string.toast_off_magnetic_field_save_data,
					R.string.toast_on_magnetic_field_save_data);
			if (((ThermometerApp) getApplication()).saveAnySensor())
				startService(new Intent(getBaseContext(), SensorService.class));
		}
	};

	private OnClickListener temperatueHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.ambient_temp_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_TEMPERATUE);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_TEMPERATURE);
			startActivity(intent);
		}
	};

	private OnClickListener relativeHumidityHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.relative_humidity_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_RELATIVE_HUMIDITY);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_RELATIVE_HUMIDITY);
			startActivity(intent);
		}
	};

	private OnClickListener absoluteHumidityHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.absolute_humidity_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_ABSOLUTE_HUMIDITY);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_ABSOLUTE_HUMIDITY);
			startActivity(intent);
		}
	};

	private OnClickListener pressureHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.pressure_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_PRESSURE);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_PRESSURE);
			startActivity(intent);
		}
	};

	private OnClickListener dewPointHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.dew_point_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_DEW_POINT);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_DEW_POINT);
			startActivity(intent);
		}
	};

	private OnClickListener lightHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.light_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_LIGHT);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_LIGHT);
			startActivity(intent);
		}
	};

	private OnClickListener magneticFieldHistoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(),
					HistoryPlotActivity.class);
			intent.putExtra(HistoryPlotActivity.INTENT_ORIGIN, getResources()
					.getString(R.string.magnetic_field_title));
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_TABLE_NAME,
					DbHelper.TABLE_MAGNETIC_FIELD);
			intent.putExtra(HistoryPlotActivity.INTENT_EXTRA_UNIT,
					HistoryPlotActivity.UNIT_MAGNETIC_FIELD);
			startActivity(intent);
		}
	};
}

package Ir.co.tfs.farazaman.layout;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.GnssStatusCompat;
import androidx.core.location.LocationManagerCompat;

import Ir.co.tfs.farazaman.OSMTracker;
import Ir.co.tfs.farazaman.R;
import Ir.co.tfs.farazaman.activity.DisplayTrackMap;
import Ir.co.tfs.farazaman.activity.TrackLogger;

import java.text.DecimalFormat;

/**
 * Layout for the GPS Status image and misc action buttons.
 * 
 * @author Nicolas Guillaumin
 * 
 */
public class GpsStatusRecordDisplay extends LinearLayout implements LocationListener {

	private final static String TAG = GpsStatusRecordDisplay.class.getSimpleName();

	final private int REQUEST_CODE_GPS_PERMISSIONS = 1;

	/**
	 * Formatter for accuracy display.
	 */
	private final static DecimalFormat ACCURACY_FORMAT = new DecimalFormat("0");

	/**
	 * Keeps matching between satellite indicator bars to draw, and numbers
	 * of satellites for each bars;
	 */
	private final static int[] SAT_INDICATOR_TRESHOLD = {2, 3, 4, 6, 8};

	/**
	 * Containing activity
	 */
	private DisplayTrackMap activity;

	/**
	 * Reference to LocationManager
	 */
	private LocationManager lmgr;

	/**
	 * the timestamp of the last GPS fix we used
	 */
	private long lastGPSTimestampStatus = 0;

	/**
	 * the timestamp of the last GPS fix we used for location updates
	 */
	private long lastGPSTimestampLocation = 0;

	/**
	 * the interval (in ms) to log GPS fixes defined in the preferences
	 */
	private final long gpsLoggingInterval;

	/**
	 * Is GPS active ?
	 */
	private boolean gpsActive = false;

	/**
	 * Satellites count
	 */
	private int satCount = 0;

	/**
	 * Satellites used in fix count
	 */
	private int fixCount = 0;


	public GpsStatusRecordDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		GpsStatusViewFactory.populate(this);

		//read the logging interval from preferences
		gpsLoggingInterval = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(
				OSMTracker.Preferences.KEY_GPS_LOGGING_INTERVAL, OSMTracker.Preferences.VAL_GPS_LOGGING_INTERVAL)) * 1000;

		if (context instanceof DisplayTrackMap) {
			activity = (DisplayTrackMap) context;
			lmgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
		
		// Initialize waiting message (0 satellites so far)
		TextView tvAccuracy = (TextView) findViewById(R.id.gpsstatus_record_tvAccuracy);
		tvAccuracy.setText(getResources().getString(R.string.various_waiting_gps_fix)
				.replace("{0}", "0")
				.replace("{1}", "0"));

	}

	public void requestLocationUpdates(boolean request) {
		if (request) {
			if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
				LocationManagerCompat.registerGnssStatusCallback(
						lmgr, ContextCompat.getMainExecutor(getContext()), mStatusCallback);
			} else {
				ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						REQUEST_CODE_GPS_PERMISSIONS);
			}
		} else {
			lmgr.removeUpdates(this);
			LocationManagerCompat.unregisterGnssStatusCallback(lmgr, mStatusCallback);
		}
	}

	private final GnssStatusCompat.Callback mStatusCallback = new GnssStatusCompat.Callback() {
		@Override
		public void onSatelliteStatusChanged(GnssStatusCompat status) {
			satCount = status.getSatelliteCount();
			fixCount = 0;

			for (int i = 0; i < satCount; i++) {
				if (status.usedInFix(i)) {
					fixCount++;
				}
			}

			if (fixCount == 0) {
				TextView tvAccuracy = findViewById(R.id.gpsstatus_record_tvAccuracy);
				tvAccuracy.setText(getResources().getString(R.string.various_waiting_gps_fix)
						.replace("{0}", Long.toString(fixCount))
						.replace("{1}", Long.toString(satCount)));

				setGpsQualityColor(R.color.stop);
			}

			Log.v(TAG, "Found " + satCount + " satellites. " + fixCount + " used in fix.");
		}
	};

	@Override
	public void onLocationChanged(Location location) {
		// first of all we check if the time from the last used fix to the current fix is greater than the logging interval
		if ((lastGPSTimestampLocation + gpsLoggingInterval) < System.currentTimeMillis()) {
			lastGPSTimestampLocation = System.currentTimeMillis(); // save the time of this fix
			Log.v(TAG, "Location received " + location);
			if (!gpsActive) {
				gpsActive = true;
				// GPS activated, activate UI
//				activity.onGpsEnabled();
				manageRecordingIndicator(true);
			}


			TextView tvAccuracy = findViewById(R.id.gpsstatus_record_tvAccuracy);
			if (location.hasAccuracy()) {
				Log.d(TAG, "location accuracy: "+ ACCURACY_FORMAT.format(location.getAccuracy()));
				tvAccuracy.setText(getResources().getString(R.string.various_accuracy_with_sats)
						.replace("{0}", ACCURACY_FORMAT.format(location.getAccuracy()))
						.replace("{1}", getResources().getString(R.string.various_unit_meters))
						.replace("{2}", Long.toString(fixCount))
						.replace("{3}", Long.toString(satCount)));

				manageSatelliteStatusIndicator((int) location.getAccuracy());

			} else {
				Log.d(TAG, "location without accuracy");
				tvAccuracy.setText("");
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Location provider " + provider + " disabled");
		gpsActive = false;
		setGpsQualityColor(R.color.icon_gray);
		((TextView) findViewById(R.id.gpsstatus_record_tvAccuracy)).setText("");
//		activity.onGpsDisabled();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Location provider " + provider + " enabled");
		setGpsQualityColor(R.color.stop);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Update provider status image according to status
		Log.d(TAG, "Location provider " + provider + " status changed to: " + status);
		ImageView imgSatIndicator = findViewById(R.id.gpsstatus_record_imgSatIndicator);
		TextView tvAccuracy = findViewById(R.id.gpsstatus_record_tvAccuracy);

		switch (status) {
			// Don't do anything for status AVAILABLE, as this event occurs frequently,
			// changing the graphics cause flickering .
			case LocationProvider.OUT_OF_SERVICE:
				setGpsQualityColor(R.color.icon_gray);
				tvAccuracy.setText("");
				gpsActive = false;
//				activity.onGpsDisabled();
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				setGpsQualityColor(R.color.stop);
				tvAccuracy.setText("");
				gpsActive = false;
				break;
		}

	}
	
	/**
	 * Manages the state of the recording indicator, depending if we're tracking or not.
	 * @param isTracking true if the indicator must show that we're tracking, otherwise false
	 */
	public void manageRecordingIndicator(boolean isTracking) {
		ImageView recordStatus = findViewById(R.id.gpsstatus_record_animRec);
		recordStatus.setImageResource(R.drawable.ic_route_recording);
		recordStatus.setColorFilter(ContextCompat.getColor(getContext(), isTracking ? R.color.brand_green : R.color.icon_gray));
	}

	/**
	 * Manages the state of the satellites status
	 * @param accuracy in meters, the smaller the number the better the accuracy.
	 */
	private void manageSatelliteStatusIndicator(int accuracy){
		ImageView imgSatIndicator = findViewById(R.id.gpsstatus_record_imgSatIndicator);

		imgSatIndicator.setImageResource(R.drawable.ic_gps_fix);
		setGpsQualityColor(accuracy <= 10 ? R.color.brand_green : accuracy <= 25 ? R.color.stop : R.color.app_error);
	}

	private void setGpsQualityColor(int colorResource) {
		ImageView indicator = findViewById(R.id.gpsstatus_record_imgSatIndicator);
		indicator.setImageResource(R.drawable.ic_gps_fix);
		indicator.setColorFilter(ContextCompat.getColor(getContext(), colorResource));
	}

}

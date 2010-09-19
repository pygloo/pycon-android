package org.pyconfr.schedules;

import java.util.Date;

import org.pyconfr.R;
import org.pyconfr.broadcast.FavoritesBroadcast;
import org.pyconfr.db.DBAdapter;
import org.pyconfr.listeners.ParserEventListener;
import org.pyconfr.util.StringUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements ParserEventListener,
		OnClickListener {
	public static final String LOG_TAG = Main.class.getName();

	public static final int STARTFETCHING = -1;
	public static final int DONEFETCHING = 0;
	public static final int TAGEVENT = 1;
	public static final int DONELOADINGDB = 2;
	public static final int ROOMIMGSTART = 3;
	public static final int ROOMIMGDONE = 4;
	public static final int LOAD_BG_START = 5;
	public static final int LOAD_BG_END = 6;

	protected static final int DIALOG_ABOUT = 0;
	protected static final int DIALOG_UPDATE = 1;

	private static final int ABOUT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST + 1;
	private static final int SETTINGS_ID = Menu.FIRST + 2;

	public static final String PREFS = "org.pyconfr";
	// fosdem schedule: http://fosdem.org/schedule/xml
	// pyconfr schedule: http://www.bewype.org/pyconfr-android/index.xml
	public static final String XML_URL = "http://www.bewype.org/pyconfr-android/index.xml";
	// fosdem map: http://fosdem.org/2010/map/room/
	// pyconfr map: http://www.bewype.org/pyconfr-android/map/room/
	public static final String ROOM_IMG_URL_BASE = "http://www.bewype.org/pyconfr-android/map/room/";

	public int counter = 0;
	protected TextView tvProgress = null, tvDbVer = null;
	protected Button btnDay1, btnDay2, btnSearch, btnFavorites;
	protected Intent service;

	private BroadcastReceiver favoritesChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.v(getClass().getName(),"Action: "+intent.getIntExtra(FavoritesBroadcast.EXTRA_TYPE, -1));
			if (intent.getIntExtra(FavoritesBroadcast.EXTRA_TYPE,-1)
							!=FavoritesBroadcast.EXTRA_TYPE_INSERT && intent
							.getIntExtra(FavoritesBroadcast.EXTRA_TYPE,-1)!=FavoritesBroadcast.EXTRA_TYPE_DELETE)
				return;
			long count = intent
					.getLongExtra(FavoritesBroadcast.EXTRA_COUNT, -1);
			Log
					.v(getClass().getName(), "FavoritesBroadcast received! "
							+ count);
			if (count == 0 || count == -1)
				btnFavorites.setEnabled(false);
			else
				btnFavorites.setEnabled(true);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		final String queryAction = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			EventListActivity.doSearchWithIntent(this, intent);
			finish();
		}
		if (Intent.ACTION_VIEW.equals(queryAction)) {
			Intent i = new Intent(this, DisplayEvent.class);
			i.putExtra(DisplayEvent.ID, Integer
					.parseInt(intent.getDataString()));
			startActivity(i);
			finish();
		}
		
		Intent initialLoadIntent = new Intent(FavoritesBroadcast.ACTION_FAVORITES_INITIAL_LOAD);
		sendBroadcast(initialLoadIntent);

		setContentView(R.layout.main);

		btnDay1 = (Button) findViewById(R.id.btn_day_1);
		btnDay1.setOnClickListener(this);
		btnDay2 = (Button) findViewById(R.id.btn_day_2);
		btnDay2.setOnClickListener(this);
		btnSearch = (Button) findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);
		btnFavorites = (Button) findViewById(R.id.btn_favorites);
		btnFavorites.setOnClickListener(this);

		tvProgress = (TextView) findViewById(R.id.progress);
		tvDbVer = (TextView) findViewById(R.id.db_ver);

		// FIXME on first startup
		// - propose user to update database
	}

	@Override
	protected void onResume() {
		super.onResume();
		tvDbVer.setText(getString(R.string.db_ver) + " "
				+ StringUtil.dateTimeToString(getDBLastUpdated()));

		DBAdapter dbAdapter = new DBAdapter(this);
		long count = 0;
		try {
			dbAdapter.open();
			btnFavorites.setEnabled(dbAdapter.getBookmarkCount() > 0);
			count = dbAdapter.getEventCount();
			btnDay1.setEnabled(count > 0);
			btnDay2.setEnabled(count > 0);
		} finally {
			dbAdapter.close();
		}

		if (count < 1) {
			showDialog(DIALOG_UPDATE);
		}

		// FIXME on first startup
		// - propose user to update database
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// menu.add(0, SETTINGS_ID, 2,
		// R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, UPDATE_ID, 2, R.string.menu_update).setIcon(
				R.drawable.menu_refresh);
		menu.add(0, ABOUT_ID, 2, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, SETTINGS_ID, 2, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		return true;
	}

	/**
	 * @return
	 */
	private Dialog createAboutDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view = getLayoutInflater().inflate(R.layout.about, null,
				false);
		builder.setTitle(getString(R.string.app_name));
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(view);
		builder.setPositiveButton(getString(android.R.string.ok), null);
		builder.setCancelable(true);
		return builder.create();
	}

	/**
	 * @return
	 */
	private Dialog createUpdateDialog() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.updater_title));

		final boolean[] selection = { true, false }; // second bool for room maps -> not used for pyconfr
		builder.setMultiChoiceItems(R.array.updater_dialog_choices, selection,
				new OnMultiChoiceClickListener() {

					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						selection[which] = isChecked;
					}
				});

		builder.setPositiveButton(getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						// if none selected, skip
						if (!(selection[0] || selection[1]))
							return;

						final Thread t = new Thread(new BackgroundUpdater(
								handler, Main.this, getApplicationContext(),
								selection[0], selection[1]));
						t.start();
					}

				});

		builder.setNegativeButton(getString(android.R.string.cancel), null);
		builder.setCancelable(true);

		return builder.create();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ABOUT:
			return createAboutDialog();
		case DIALOG_UPDATE:
			return createUpdateDialog();
		default:
			return null;
		}
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_day_1:
			showTracksForDay(1);
			break;
		case R.id.btn_day_2:
			showTracksForDay(2);
			break;
		case R.id.btn_search:
			// nothing to do as btn is not active
			break;
		case R.id.btn_favorites:
			showFavorites();
			break;
		default:
			Log.e(LOG_TAG,
					"Received a button click, but I don't know from where.");
			break;
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case UPDATE_ID:
			showDialog(DIALOG_UPDATE);
			return true;
		case ABOUT_ID:
			showDialog(DIALOG_ABOUT);
			break;
		case SETTINGS_ID:
			showSettings();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void toast(String message) {
		final Context context = getApplicationContext();
		final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.show();
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg == null)
				return;
			switch (msg.what) {
			case TAGEVENT:
				Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
				tvProgress.setText("Fetched " + counter + " events.");
				break;
			case STARTFETCHING:
				tvProgress.setText("Downloading...");
				break;
			case DBAdapter.MSG_EVENT_STORED:
				tvProgress.setText("Stored " + msg.arg1 + " events.");
				break;
			case DONEFETCHING:
				tvProgress.setText("Done fetching, loading into DB");
				setDBLastUpdated();
				break;
			case DONELOADINGDB:
				final String doneDb = "Done loading into DB";
				tvProgress.setText(doneDb);
				toast(doneDb);
				tvDbVer.setText(getString(R.string.db_ver) + " "
						+ StringUtil.dateTimeToString(getDBLastUpdated()));
				DBAdapter db = new DBAdapter(Main.this);
				db.open();
				try {
					long count = db.getEventCount();
					btnDay1.setEnabled(count > 0);
					btnDay2.setEnabled(count > 0);
				} finally {
					db.close();
				}
				break;
			case ROOMIMGSTART:
				tvProgress.setText("Downloading room images...");
				break;
			case ROOMIMGDONE:
				final String doneRooms = "Room Images downloaded";
				tvProgress.setText(doneRooms);
				toast(doneRooms);
				break;
			/*case LOAD_BG_START:
				Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay(); 
				Main.this.setRequestedOrientation(display.getOrientation());
				break;
			case LOAD_BG_END:
				Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;*/
			}
		}
	};

	public void onTagEvent(String tag, int type) {
		if (tag.equals("event") && type == ParserEventListener.TAG_OPEN) {
			counter++;
			final Message msg = Message.obtain();
			msg.what = TAGEVENT;
			handler.sendMessage(msg);
		}
	}

	public void showTracksForDay(int day) {
		Log.d(LOG_TAG, "showTracksForDay(" + day + ");");
		Intent i = new Intent(this, TrackListActivity.class);
		i.putExtra(TrackListActivity.DAY_INDEX, day);
		startActivity(i);
	}

	public void showFavorites() {
		Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(EventListActivity.FAVORITES, true);
		startActivity(i);
	}

	/**
	 * Set NOW as the time that the Schedule database has been imported.
	 */
	private void setDBLastUpdated() {
		SharedPreferences.Editor editor = getSharedPreferences(Main.PREFS, 0)
				.edit();
		long timestamp = System.currentTimeMillis() / 1000;
		editor.putLong("db_last_updated", timestamp);
		editor.commit(); // Don't forget to commit your edits!!!
	}

	/**
	 * Fetch the Date when the Schedule database has been imported
	 * 
	 * @return Date of the last Database update
	 */
	private Date getDBLastUpdated() {
		SharedPreferences settings = getSharedPreferences(Main.PREFS, 0);
		long timestamp = settings.getLong("db_last_updated", 0);
		if (timestamp == 0)
			return null;
		return new Date(timestamp * 1000);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void showSettings() {
		Intent i = new Intent(this, Preferences.class);
		startActivity(i);
	}
}
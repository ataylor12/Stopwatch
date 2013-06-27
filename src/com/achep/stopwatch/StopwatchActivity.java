package com.achep.stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.achep.stopwatch.view.PagerAdapter;
import com.achep.stopwatch.view.ViewPager;
import com.achep.stopwatch.view.ViewPager.OnPageChangeListener;
import com.achep.stopwatch.widget.NumberPickerLite;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class StopwatchActivity extends Activity implements
		OnSharedPreferenceChangeListener {
	private static final int MSG_START_TIMER = 0;
	private static final int MSG_CLEAR_TIMER = 1;
	private static final int MSG_UPDATE_TIMER = 2;
	private static final int MSG_PAUSE_TIMER = 3;

	private static final int REFRESH_RATE_STOPWATCH = 14;
	private static final int REFRESH_RATE_TIMER = 1000;

	private static final int NOTIFY_ID = 2;
	private static final int ICON = R.drawable.ic_statusbar_stopwatch;

	private static final String ERROR_DATA_LOADING = "Error data loading!";

	private static final String KEY_STOPWATCH_DATA = "mStopwatchData";
	private static final String KEY_STOPWATCH_PAUSE = "mPauseTime";
	private static final String KEY_STOPWATCH_START = "mStartTime";
	private static final String KEY_STOPWATCH_FULL_TIME = "mStopwatchFullTime";
	private static final String KEY_STOPWATCH_LAPS = "mLapLength";
	private static final String KEY_STOPWATCH_PRIMARY_LINE = "mPLine=";
	private static final String KEY_STOPWATCH_SECONDARY_LINE = "mSLine=";

	private static final String KEY_TIMER_PAUSE = "mTimerPauseTime";

	private Vector<String> mPrimaryLine, mSecondaryLine, mTimerPrimaryLine,
			mTimerSecondaryLine;

	private boolean mTimerRepeatBool = false;
	private TextView mStopwatchMinSec, mStopwatchHour, mStopwatchDay,
			mStopwatchMs, mTimerMinSec, mTimerHour, mTimerDay;
	private LinearLayout mTimerRepeatBtn;
	private Button mStopwatchStartBtn, mStopwatchResetBtn, mTimerStartBtn,
			mAddSwitch, mTimerResetBtn, mAddSaveBtn;
	private ListView mStopwatchListView, mTimerListView;
	private Activity mContext;
	private View AddTimer;
	private ViewPager viewPager;
	private NumberPickerLite mAddHour, mAddMinute, mAddSecond, mAddHour2,
			mAddMinute2, mAddSecond2;

	private long mStopwatchLapTime, mStopwatchFullTime, mStopwatchStartTime,
			mStopwatchPauseTime, mTimerPauseTime, mTimerStartTime,
			mTimerNextTime = 0;

	private long[][] mTimersList;
	private int mTimersListLength;
	
	// Add timer
	private CheckBox mRepeatState;
	
	// Data
	private SharedPreferences.Editor mDataTimer, mDataStopwatch;

	// Settings
	private boolean mStopwatchMode, mNotifyEnabled;

	// Notifications
	private NotificationManager mNotificationManager;
	private CharSequence mNotifyText, mNotifyText2;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_START_TIMER:
				mStopwatchStartTime = System.currentTimeMillis()
						- mStopwatchPauseTime;
				mStopwatchPauseTime = 0;
				sendStopwatchMsg(MSG_UPDATE_TIMER);
				mStopwatchStartBtn.setText(getString(R.string.stopwatch_pause));
				mStopwatchResetBtn.setText(getString(R.string.stopwatch_lap));
				break;

			case MSG_UPDATE_TIMER:
				mStopwatchLapTime = TimeStopwatch();
				updateStopwatchText(mStopwatchMode ? mStopwatchLapTime
						: mStopwatchLapTime + mStopwatchFullTime);
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
						REFRESH_RATE_STOPWATCH);
				break;

			case MSG_CLEAR_TIMER:
				mStopwatchStartBtn.setText(getString(R.string.stopwatch_start));
				mStopwatchResetBtn.setText(getString(R.string.stopwatch_reset));
				updateStopwatchText(0);
				mStopwatchPauseTime = 0;
				break;

			case MSG_PAUSE_TIMER:
				mStopwatchPauseTime = TimeStopwatch();
				mStopwatchStartTime = 0;
				mStopwatchStartBtn.setText(getString(R.string.stopwatch_start));
				mStopwatchResetBtn.setText(getString(R.string.stopwatch_reset));
				break;

			default:
				break;
			}
		}
	};

	Handler mTimerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_START_TIMER:
				mTimerStartTime = System.currentTimeMillis() - mTimerPauseTime;
				mTimerPauseTime = 0;
				saveDataTimerTime(false);
				startService(new Intent(mContext, ServiceTimer.class));
				updateTimersList();
				mTimerNextTime = next();
				sendTimerMsg(MSG_UPDATE_TIMER);
				mTimerStartBtn.setText(getString(R.string.stopwatch_pause));
				break;

			case MSG_UPDATE_TIMER:
				final long a = mTimerNextTime + mTimerStartTime
						- System.currentTimeMillis();
				if (a > 0) {
					String[] mStrTime = getTimeString(a);
					mTimerMinSec.setText(mStrTime[1]);
					mTimerDay.setText(mStrTime[3]);
					mTimerHour.setText(mStrTime[2]);
					mTimerHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
							REFRESH_RATE_TIMER);
				} else {
					mTimerNextTime = next();
					sendTimerMsg(mTimerNextTime != Long.MAX_VALUE ? MSG_UPDATE_TIMER
							: MSG_CLEAR_TIMER);
				}
				break;

			case MSG_PAUSE_TIMER:
				mContext.stopService(new Intent(mContext, ServiceTimer.class));
				mTimerStartBtn.setText(getString(R.string.stopwatch_start));
				mTimerPauseTime = System.currentTimeMillis() - mTimerStartTime;
				mTimerStartTime = 0;
				saveDataTimerTime(true);
				break;

			case MSG_CLEAR_TIMER:
				mContext.stopService(new Intent(mContext, ServiceTimer.class));
				mTimerStartBtn.setText(getString(R.string.stopwatch_start));
				updateTimerText(0);
				mTimerPauseTime = 0;
				mTimerStartTime = 0;
				saveDataTimerTime(true);
				break;

			default:
				break;
			}
		}
	};

	private PendingIntent mStopwatchNotificationIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mDataTimer = PreferenceManager.getDefaultSharedPreferences(mContext)
				.edit();
		mDataStopwatch = getSharedPreferences(KEY_STOPWATCH_DATA, MODE_PRIVATE)
				.edit();

		// Set current language
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String value = preferences.getString(Utils.KEY_SETTINGS_LOCALE,
				"system");
		Locale locale = value.equals("system") ? Locale.getDefault()
				: new Locale(value);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getResources().updateConfiguration(config,
				getResources().getDisplayMetrics());
		
		// Initialize some often using strings
		time_hour1 = getString(R.string.time_hour1);
		time_hour2 = getString(R.string.time_hour2);
		time_hour3 = getString(R.string.time_hour3);
		time_day1 = getString(R.string.time_day1);
		time_day2 = getString(R.string.time_day2);
		time_day3 = getString(R.string.time_day3);

		// Stopwatch's notifications
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyText = getText(R.string.notification_stopwatch1);
		mNotifyText2 = getText(R.string.notification_stopwatch2) + ": ";
		Intent notificationIntent = new Intent(mContext,
				StopwatchActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mStopwatchNotificationIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		// Load preferences
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(
				PreferenceManager.getDefaultSharedPreferences(this), "null");

		LayoutInflater inflater = LayoutInflater.from(this);
		List<View> pages = new ArrayList<View>();
		View Stopwatch = inflater.inflate(R.layout.main_stopwatch, null);
		View Timer = inflater.inflate(R.layout.main_timer, null);
		AddTimer = inflater.inflate(R.layout.main_add, null);
		mStopwatchListView = (ListView) Stopwatch.findViewById(R.id.stopwatch_list);
		mStopwatchStartBtn = (Button) Stopwatch.findViewById(R.id.toggle_stopwatch);
		mStopwatchResetBtn = (Button) Stopwatch.findViewById(R.id.clear_stopwatch);
		mStopwatchMinSec = (TextView) Stopwatch.findViewById(R.id.stopwatch_mm_ss);
		mStopwatchDay = (TextView) Stopwatch.findViewById(R.id.stopwatch_dd);
		mStopwatchHour = (TextView) Stopwatch.findViewById(R.id.stopwatch_hh);
		mStopwatchMs = (TextView) Stopwatch.findViewById(R.id.stopwatch_ms);
		mTimerListView = (ListView) Timer.findViewById(R.id.timer_list);
		mTimerStartBtn = (Button) Timer.findViewById(R.id.start_timer);
		mTimerResetBtn = (Button) Timer.findViewById(R.id.clear_timer);
		mTimerMinSec = (TextView) Timer.findViewById(R.id.timer_mm_ss);
		mTimerDay = (TextView) Timer.findViewById(R.id.timer_dd);
		mTimerHour = (TextView) Timer.findViewById(R.id.timer_hh);
		mAddSwitch = (Button) Timer.findViewById(R.id.timer_add);
		mAddHour = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_hh);
		mAddMinute = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_mm);
		mAddSecond = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_ss);
		mAddSaveBtn = (Button) AddTimer.findViewById(R.id.save);
		mTimerRepeatBtn = (LinearLayout) AddTimer.findViewById(R.id.add_timer_repeat);
		mAddHour2 = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_hh2);
		mAddMinute2 = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_mm2);
		mAddSecond2 = (NumberPickerLite) AddTimer.findViewById(R.id.add_timer_ss2);
		mRepeatState = (CheckBox) AddTimer.findViewById(R.id.add_timer_repeat_state);

		mStopwatchStartBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendStopwatchMsg(checkStopwatchState() ? MSG_PAUSE_TIMER
						: MSG_START_TIMER);
			}
		});
		mStopwatchResetBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!checkStopwatchState()) {

					// Stop visual stopwatch
					sendStopwatchMsg(MSG_CLEAR_TIMER);

					// Clear stopwatch's list
					setStopwatchListAdapter(true);

					// Clear stopwatch's data
					mDataStopwatch.clear();
				} else {

					// Start visual stopwatch
					sendStopwatchMsg(MSG_START_TIMER);

					// Get strings
					String primaryLine = new String(
							getTimeStrAdapter(mStopwatchLapTime));
					String secondaryLine = new String(
							getTimeStrAdapter(mStopwatchFullTime += mStopwatchLapTime));

					// Add new lap
					mPrimaryLine.add(primaryLine);
					mSecondaryLine.add(secondaryLine);

					// Save data
					int lengthOfArray = mPrimaryLine.size();
					mDataStopwatch.putLong(KEY_STOPWATCH_FULL_TIME,
							mStopwatchFullTime).commit();
					mDataStopwatch.putInt(KEY_STOPWATCH_LAPS, lengthOfArray)
							.commit();
					mDataStopwatch.putString(
							KEY_STOPWATCH_PRIMARY_LINE + (lengthOfArray - 1),
							primaryLine).commit();
					mDataStopwatch.putString(
							KEY_STOPWATCH_SECONDARY_LINE + (lengthOfArray - 1),
							secondaryLine).commit();

					// Set list adapter
					setStopwatchListAdapter(false);
				}
			}
		});
		mTimerStartBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mTimerPrimaryLine.isEmpty())
					sendTimerMsg(!checkTimerState() ? MSG_START_TIMER
							: MSG_PAUSE_TIMER);
			}
		});
		mAddSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTimerStartTime == 0 && mTimerPauseTime == 0)
					viewPager.setCurrentItem(2);
			}
		});
		mTimerResetBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendTimerMsg(MSG_CLEAR_TIMER);
				mContext.stopService(new Intent(mContext, ServiceTimer.class));
			}
		});
		mTimerResetBtn.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sendTimerMsg(MSG_CLEAR_TIMER);
				mContext.stopService(new Intent(mContext, ServiceTimer.class));
				setTimerAdapter(true);
				mDataTimer.clear();
				return false;
			}
		});
		mAddSaveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((mAddHour.getValue() + mAddMinute.getValue()
						+ mAddSecond.getValue() != 0)
						& mTimerStartTime == 0 && mTimerPauseTime == 0) {

					// Get string values
					String primaryLine = new String(mAddHour.getStrValue()
							+ ":" + mAddMinute.getStrValue() + ":"
							+ mAddSecond.getStrValue());
					String secondaryLine = new String(
							mTimerRepeatBool ? (mAddHour2.getStrValue() + ":"
									+ mAddMinute2.getStrValue() + ":" + mAddSecond2
									.getStrValue()) : "");

					// Get lenght of array of timers
					int lengthOfArray = mTimerPrimaryLine.size();

					// Save timer's data
					mDataTimer.putString(
							Utils.KEY_TIMER_PRIMARY_LINE + lengthOfArray,
							primaryLine).commit();
					mDataTimer.putString(
							Utils.KEY_TIMER_SECONDARY_LINE + lengthOfArray,
							secondaryLine).commit();
					mDataTimer
							.putInt(Utils.KEY_TIMER_TIMERS, lengthOfArray + 1)
							.commit();

					// Get long time from string
					long time = Utils.getTime(primaryLine);

					// Put new timer to correct place
					if (lengthOfArray > 0)
						for (int i = lengthOfArray; i > -1; i--) {
							if (i > 0) {
								if (time <= Utils.getTime(mTimerPrimaryLine
										.elementAt(i - 1))) {
									mTimerPrimaryLine.add(i, primaryLine);
									mTimerSecondaryLine.add(i, secondaryLine);
									break;
								}
							} else {
								mTimerPrimaryLine.add(0, primaryLine);
								mTimerSecondaryLine.add(0, secondaryLine);
							}

						}
					else {
						mTimerPrimaryLine.add(primaryLine);
						mTimerSecondaryLine.add(secondaryLine);
					}

					// Update list adapter
					setTimerAdapter(false);

					// Reset "add timer" page information
					mAddSecond.setValue(0);
					mAddMinute.setValue(0);
					mAddHour.setValue(0);
					mAddSecond2.setValue(0);
					mAddMinute2.setValue(0);
					mAddHour2.setValue(0);
					setAddRepeat(mTimerRepeatBool = false);

					// set timer page as current page
					viewPager.setCurrentItem(1);					
					
				}
			}
		});
		mTimerRepeatBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAddRepeat(mTimerRepeatBool = !mTimerRepeatBool);
			}
		});

		mAddHour.setMaxValue(999);
		mAddHour2.setMaxValue(999);

		pages.add(Stopwatch);
		pages.add(Timer);
		pages.add(AddTimer);
		viewPager = new ViewPager(this);
		viewPager.setAdapter(new ViewPagerAdapter(pages));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 0) {
					LaunchStopwatchByData();
					mTimerHandler.removeMessages(MSG_UPDATE_TIMER);
				} else {
					mHandler.removeMessages(MSG_UPDATE_TIMER);
					if (arg0 == 1) {
						if (checkTimerState())
							sendTimerMsg(MSG_UPDATE_TIMER);
					} else
						mTimerHandler.removeMessages(MSG_UPDATE_TIMER);
				}
			}

		});

		loadSavedData();
		setContentView(viewPager);
	}

	private void setStopwatchListAdapter(boolean reset) {
		if (!reset) {
			String a[] = {};
			mStopwatchListView.setAdapter(new StopwatchAdapter(mContext,
					mPrimaryLine.toArray(a), mSecondaryLine.toArray(a), false));
		} else {
			mStopwatchListView.setAdapter(null);
			mPrimaryLine.clear();
			mSecondaryLine.clear();
			mStopwatchFullTime = 0;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Pause handlers
		mTimerHandler.removeMessages(MSG_UPDATE_TIMER);
		mHandler.removeMessages(MSG_UPDATE_TIMER);

		// Create notification
		updateStopwatchNotify();

		// Save stopwatch's data
		mDataStopwatch.putLong(KEY_STOPWATCH_START, mStopwatchStartTime)
				.commit();
		mDataStopwatch.putLong(KEY_STOPWATCH_PAUSE, mStopwatchPauseTime)
				.commit();

		// Save current screen position
		SharedPreferences mDataScreen = getSharedPreferences("mSystemData",
				MODE_PRIVATE);
		mDataScreen.edit().putInt("mCurrentWindow", viewPager.getCurrentItem())
				.commit();
		
        // Disable wake lock
		cpuLockUp.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNotificationManager.cancel(NOTIFY_ID);
		LaunchStopwatchByData();
		LaunchTimerByData();
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		cpuLockUp = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				WAKELOCK_TAG);
		cpuLockUp.acquire();
	}

	private PowerManager.WakeLock cpuLockUp;
	private static final String WAKELOCK_TAG = "StopwatchActivity";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			startActivity(new Intent(this, SettingsAbout.class));
			return true;
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadSavedData() {
		mPrimaryLine = new Vector<String>();
		mSecondaryLine = new Vector<String>();
		mTimerPrimaryLine = new Vector<String>();
		mTimerSecondaryLine = new Vector<String>();
		SharedPreferences mSavedData = getSharedPreferences("mStopwatchData",
				MODE_PRIVATE);
		if (mSavedData != null) {
			viewPager.setCurrentItem(mSavedData.getInt("mCurrentWindow", 0),
					false);
			mStopwatchStartTime = mSavedData.getLong("mStartTime", 0);
			mStopwatchPauseTime = mSavedData.getLong("mPauseTime", 0);
			mStopwatchFullTime = mSavedData.getLong("mStopwatchFullTime", 0);
			int length = mSavedData.getInt("mLapLength", 0);
			if (length > 0) {
				for (int i = 0; i < length; i++) {
					mPrimaryLine.add(new String(mSavedData.getString("mPLine="
							+ i, ERROR_DATA_LOADING)));
					mSecondaryLine.add(new String(mSavedData.getString(
							"mSLine=" + i, ERROR_DATA_LOADING)));
				}
				setStopwatchListAdapter(false);
			}
		}
		mSavedData = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (mSavedData != null) {
			mTimerStartTime = mSavedData.getLong("mTimerSystemTime", 0);
			mTimerPauseTime = mSavedData.getLong("mTimerPauseTime", 0);
			int length = mSavedData.getInt("mTimerListLength", 0);
			if (length > 0) {
				for (int i = 0; i < length; i++) {
					mTimerPrimaryLine.add(new String(mSavedData.getString(
							"mTimerListMain=" + i, ERROR_DATA_LOADING)));
					mTimerSecondaryLine.add(new String(mSavedData.getString(
							"mTimerListRepeat=" + i, ERROR_DATA_LOADING)));
				}
				setTimerAdapter(false);
				updateTimersList();
			}
		}
		mSavedData = getSharedPreferences("mSystemData", MODE_PRIVATE);
		if (mSavedData != null) {
			viewPager.setCurrentItem(mSavedData.getInt("mCurrentWindow", 0),
					false);
		}

	}

	private void LaunchStopwatchByData() {
		if (mStopwatchStartTime != 0 && mStopwatchPauseTime == 0) {
			mStopwatchStartBtn.setText(getString(R.string.stopwatch_pause));
			mStopwatchResetBtn.setText(getString(R.string.stopwatch_lap));
			sendStopwatchMsg(MSG_UPDATE_TIMER);
		} else {
			mStopwatchStartBtn.setText(getString(R.string.stopwatch_start));
			mStopwatchResetBtn.setText(getString(R.string.stopwatch_reset));
			if (mStopwatchPauseTime == 0)
				updateStopwatchText(0);
			else {
				mStopwatchStartTime = System.currentTimeMillis()
						- mStopwatchPauseTime;
				updateStopwatchText(TimeStopwatch());
			}

		}
	}

	private void LaunchTimerByData() {
		if (mTimerStartTime != 0 && mTimerPauseTime == 0) {
			startService(new Intent(mContext, ServiceTimer.class));
			sendTimerMsg(MSG_UPDATE_TIMER);
			mTimerStartBtn.setText(getString(R.string.stopwatch_pause));
		} else {
			mTimerStartBtn.setText(getString(R.string.stopwatch_start));
			if (mTimerPauseTime == 0)
				updateTimerText(0);
			else {
				mTimerStartTime = System.currentTimeMillis() - mTimerPauseTime;
				updateTimerText(next() - mTimerPauseTime);
			}
		}
	}

	private void setTimerAdapter(boolean reset) {
		if (!reset) {
			String a[] = {};
			mTimerListView.setAdapter(new StopwatchAdapter(mContext,
					mTimerPrimaryLine.toArray(a), mTimerSecondaryLine
							.toArray(a), true));
		} else {
			mTimerPrimaryLine.clear();
			mTimerSecondaryLine.clear();
			mTimerListView.setAdapter(null);
		}
	}

	private void setAddRepeat(boolean repeat) {
		mRepeatState.setChecked(repeat);
	}

	private void updateTimersList() {
		String a[] = {};
		String b[] = {};
		a = mTimerPrimaryLine.toArray(a);
		b = mTimerSecondaryLine.toArray(b);
		mTimersListLength = a.length;
		mTimersList = new long[mTimersListLength][2];
		for (int i = 0; i < mTimersListLength; i++) {
			mTimersList[i][0] = Utils.getTime(a[i]) * 1000;
			mTimersList[i][1] = b[i].equals("") ? -1
					: Utils.getTime(b[i]) * 1000;
		}
	}

	private long next() {
		long min = Long.MAX_VALUE;
		long time = System.currentTimeMillis() - mTimerStartTime;
		for (int i = 0; i < mTimersListLength; i++) {
			if (mTimersList[i][1] == -1 || mTimersList[i][0] > time) {
				if (mTimersList[i][0] < min && mTimersList[i][0] > time)
					min = mTimersList[i][0];
			} else {
				final long x = mTimersList[i][1] == 0 ? (Math.round(time
						/ mTimersList[i][0] + 0.5) * mTimersList[i][0])
						: (Math.round((time - mTimersList[i][0])
								/ (mTimersList[i][1] + mTimersList[i][0]) + 0.5)
								* (mTimersList[i][1] + mTimersList[i][0]) + mTimersList[i][0]);
				if (x < min && x > time)
					min = x;
			}
		}
		return min;
	}

	private boolean checkTimerState() {
		return mTimerStartTime == 0 || mTimerPauseTime != 0 ? false : true;
	}

	private boolean checkStopwatchState() {
		return mStopwatchStartTime != 0 && mStopwatchPauseTime == 0 ? true
				: false;
	}

	private long TimeStopwatch() {
		return System.currentTimeMillis() - mStopwatchStartTime;
	}

	private void sendStopwatchMsg(int msg) {
		if (mHandler.hasMessages(MSG_UPDATE_TIMER))
			mHandler.removeMessages(MSG_UPDATE_TIMER);
		mHandler.sendEmptyMessage(msg);
	}

	private void sendTimerMsg(int msg) {
		if (mTimerHandler.hasMessages(MSG_UPDATE_TIMER))
			mTimerHandler.removeMessages(MSG_UPDATE_TIMER);
		mTimerHandler.sendEmptyMessage(msg);
	}

	private void updateStopwatchText(long time) {
		if (time != 0) {
			String[] mStrTime = getTimeString(time);
			mStopwatchMs.setText(mStrTime[0]);
			mStopwatchMinSec.setText(mStrTime[1]);
			mStopwatchDay.setText(mStrTime[3]);
			mStopwatchHour.setText(mStrTime[2]);
		} else {
			mStopwatchDay.setText("0 " + getString(R.string.time_day3));
			mStopwatchHour.setText("0 " + getString(R.string.time_hour3));
			mStopwatchMinSec.setText("00:00");
			mStopwatchMs.setText("000");
		}
	}

	private void updateTimerText(long time) {
		if (time != 0) {
			String[] mStrTime = getTimeString(time);
			mTimerMinSec.setText(mStrTime[1]);
			mTimerDay.setText(mStrTime[3]);
			mTimerHour.setText(mStrTime[2]);
		} else {
			mTimerDay.setText("0 " + getString(R.string.time_day3));
			mTimerHour.setText("0 " + getString(R.string.time_hour3));
			mTimerMinSec.setText("00:00");
		}
	}

	// Get settings values
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		mStopwatchMode = sharedPreferences.getBoolean(Utils.KEY_STOPWATCH_MODE,
				true);
		mNotifyEnabled = sharedPreferences.getBoolean(
				Utils.KEY_STOPWATCH_NOTIFICATIONS_ENABLED, false);
		if (!mNotifyEnabled)
			mNotificationManager.cancel(NOTIFY_ID);
	}

	// Update stopwatch's notification
	private void updateStopwatchNotify() {
		if (mNotifyEnabled && checkStopwatchState()) {
			Notification notification = new Notification(ICON, mNotifyText, 0);
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			notification.setLatestEventInfo(mContext, mNotifyText, mNotifyText2
					+ Integer.toString(mPrimaryLine.size()),
					mStopwatchNotificationIntent);
			mNotificationManager.notify(NOTIFY_ID, notification);
		} else
			mNotificationManager.cancel(NOTIFY_ID);
	}

	// Save timer time
	private void saveDataTimerTime(boolean paused) {
		mDataTimer.putLong(Utils.KEY_TIMER_START, mTimerStartTime).commit();
		mDataTimer.putLong(KEY_TIMER_PAUSE, mTimerPauseTime).commit();
		mDataTimer.putBoolean(Utils.KEY_TIMER_UNRUNNING, paused).commit();
	}

	public class StopwatchAdapter extends ArrayAdapter<String> {
		private final CharSequence str_timer_list_unrepeat = getText(R.string.timer_list_unrepeat);
		private final Activity context;
		private final String[] line1;
		private final String[] line2;
		private final int length;
		private final boolean num123;

		public StopwatchAdapter(Activity context, String[] line1,
				String[] line2, boolean num) {
			super(context, R.layout.item, line1);
			this.context = context;
			this.line1 = line1;
			this.line2 = line2;
			this.num123 = num;
			this.length = line1.length;
		}

		class ViewHolder {
			public TextView numView;
			public TextView line1View;
			public TextView line2View;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.item, null, true);
				holder = new ViewHolder();
				holder.numView = (TextView) rowView.findViewById(R.id.number);
				holder.line1View = (TextView) rowView.findViewById(R.id.line1);
				holder.line2View = (TextView) rowView.findViewById(R.id.line2);
				rowView.setTag(holder);
			} else
				holder = (ViewHolder) rowView.getTag();

			holder.numView.setText(num123 ? Integer.toString(position + 1)
					: Integer.toString(this.length - position));
			holder.line1View.setText(this.line1[this.length - position - 1]);
			holder.line2View.setText(!this.line2[this.length - position - 1]
					.equals("") ? this.line2[this.length - position - 1]
					: this.str_timer_list_unrepeat);

			return rowView;
		}
	}

	public class ViewPagerAdapter extends PagerAdapter {

		List<View> pages = null;

		public ViewPagerAdapter(List<View> pages) {
			this.pages = pages;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			View v = pages.get(position);
			((ViewPager) collection).addView(v, 0);
			return v;
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	private String getTimeStr(long time) {
		long s = (time - time % 1000) / 1000;
		long m = (s - s % 60) / 60;
		long h = (m - m % 60) / 60;
		return Utils.addZero(h) + ":" + Utils.addZero(m % 60) + ":"
				+ Utils.addZero(s % 60);
	}

	private String addZeroLong(long x) {
		return String.format("%03d", x % 1000);
	}

	private String getTimeStrAdapter(long x) {
		return getTimeStr(x) + ":" + addZeroLong(x);
	}

	private int getTrueLabel(long x) {
		if (x > 9 & x < 20)
			return 3;
		else {
			x = x % 10;
			return x == 1 ? 1 : x > 1 && x < 5 ? 2 : 3;
		}
	}

	private String time_hour1;
	private String time_hour2;
	private String time_hour3;
	private String time_day1;
	private String time_day2;
	private String time_day3;


	private String getHourStr(long x) {
		int a = getTrueLabel(x);
		return x + " "
				+ ((a == 1) ? time_hour1 : (a == 2 ? time_hour2 : time_hour3));
	}

	private String[] getTimeString(long x) {
		String time[] = new String[4];
		time[0] = String.format("%03d", x % 1000);
		x = (x - x % 1000) / 1000;
		time[1] = Utils.addZero(((x - x % 60) / 60) % 60) + ":"
				+ Utils.addZero(x % 60);
		x = (x - x % 60) / 60;
		x = (x - x % 60) / 60;
		time[2] = getHourStr(x % 24);
		x = (x - x % 24) / 24;
		int a = getTrueLabel(x);
		time[3] = x + " "
				+ ((a == 1) ? time_day1 : (a == 2 ? time_day2 : time_day3));
		return time;
	}
}
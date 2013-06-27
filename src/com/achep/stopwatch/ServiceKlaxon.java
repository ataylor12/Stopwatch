package com.achep.stopwatch;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.util.Log;

public class ServiceKlaxon extends Service {

	private static final String TAG = "ServiceKlaxon";
	private static final long[] sVibratePattern = new long[] { 250, 300 };
	private AssetFileDescriptor afd;
	private MediaPlayer player;
	private Vibrator mVibrator;

	private boolean playSound;
	private boolean doVibro;

	@Override
	public void onStart(Intent intent, int startId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		playSound = prefs.getBoolean(Utils.KEY_TIMER_SOUND_ENABLED, true);
		doVibro = prefs.getBoolean(Utils.KEY_TIMER_VIBRO_ENABLED, true);
		if (playSound) {
			if (player == null)
				try {
					play();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			else
				Log.i(TAG, "Skipped sound. Player is already running");
		} else if (doVibro) {
			if (mVibrator == null) {
				mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				mVibrator.vibrate(sVibratePattern, 0);
				Handler mHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						mVibrator.cancel();
						mVibrator = null;
					}
				};
				mHandler.sendEmptyMessageDelayed(0, 2500);
			} else
				Log.i(TAG, "Skipped vibro. Vibro is already running");
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void play() throws java.io.IOException, IllegalArgumentException,
			IllegalStateException {

		afd = getAssets().openFd("tone.mp3");
		player = new MediaPlayer();
		if (player != null & afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			player.prepare();
			player.start();
			player.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					player.release();
					player = null;
					if (doVibro)
						mVibrator.cancel();
				}

			});
			if (doVibro) {
				mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				mVibrator.vibrate(sVibratePattern, 0);
			}
		} else {
			Log.w(TAG, "Sound not found / can't create mediaplayer");
		}
	}
}

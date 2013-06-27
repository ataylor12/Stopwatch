package com.achep.stopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastRec extends BroadcastReceiver {

	public static final String ACTION_TIMER = "com.achep.ACTION_TIMER";

	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_TIMER))
			context.startService(new Intent(context, ServiceKlaxon.class));
		context.startService(new Intent(context, ServiceTimer.class));
	}
}

package com.achep.stopwatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ChangelogContent extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textview = new TextView(this);
		textview.setText(readChanges(getIntent().getExtras()
				.getInt("changelog") == 1 ? false : true));
		setContentView(textview);
	}

	private String readChanges(boolean all) {
		InputStream inputStream = getResources().openRawResource(
				R.raw.changelog);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (all)
			return byteArrayOutputStream.toString();
		else {

			String str = byteArrayOutputStream.toString();
			int a = 1;
			while (!str.substring(a, a + 1).equals("\n")
					| !str.substring(a + 2, a + 3).equals("\n"))
				a += 1;

			return str.substring(0, a);
		}
	}
}
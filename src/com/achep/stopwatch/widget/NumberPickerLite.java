package com.achep.stopwatch.widget;

import com.achep.stopwatch.R;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NumberPickerLite extends LinearLayout {

	private static final int VIBRATE_MS = 25;
	private static final int LONG_CLICK_MS = 400;
	private static final int TOUCH_MAX_MS = 60;
	private static final int TOUCH_SPEED_MS = 17;

	private Context mContext;
	private int value = 0;
	private int min = 0;
	private int max = 59;
	private boolean visible = true;
	Button plus;
	Button minus;
	TextView text;
	private boolean minusTouched = false;
	private boolean plusTouched = false;
	private int minusSpeed;
	private int plusSpeed;

	public NumberPickerLite(Context context) {
		super(context);
		mContext = context;
		plus = new Button(mContext);
		minus = new Button(mContext);
		text = new TextView(mContext);
	}

	public NumberPickerLite(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		plus = new Button(mContext);
		minus = new Button(mContext);
		text = new TextView(mContext);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Resources res = getResources();
		text.setTextSize(res
				.getDimension(R.dimen.numberpaicker_value_text_size));
		text.setGravity(Gravity.CENTER);
		text.setTextColor(res.getColor(R.color.text_light));
		plus.setText("+");
		plus.setTextSize(res
				.getDimension(R.dimen.numberpaicker_buttons_text_size));
		plus.setTextColor(res.getColor(R.color.text_light));
		plus.setBackgroundDrawable(res.getDrawable(R.drawable.list_selector));
		plus.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (!plusTouched) {
					plusTouched = true;
					plusSpeed = TOUCH_MAX_MS;
					mHandler.sendEmptyMessageDelayed(2, LONG_CLICK_MS);
				}
				return false;
			}
		});
		plus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				value = value == max ? min : value + 1;
				drawView();
				plusTouched = false;
				mHandler.removeMessages(2);
			}
		});
		minus.setText("-");
		minus.setTextSize(res
				.getDimension(R.dimen.numberpaicker_buttons_text_size));
		minus.setTextColor(res.getColor(R.color.text_light));
		minus.setBackgroundDrawable(res.getDrawable(R.drawable.list_selector));
		minus.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (!minusTouched) {
					minusTouched = true;
					minusSpeed = TOUCH_MAX_MS;
					mHandler.sendEmptyMessageDelayed(1, LONG_CLICK_MS);
				}
				return false;
			}
		});
		minus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				value = value == min ? max : value - 1;
				drawView();
				minusTouched = false;
				mHandler.removeMessages(1);
			}
		});
		if (visible)
			addAllViews();
		drawView();
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1: {
				if (minus.isPressed()) {
					value = value == min ? max : value - 1;
					drawView();
					if (minusSpeed == TOUCH_MAX_MS) {
						Vibrator vibrator = (Vibrator) mContext
								.getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(VIBRATE_MS);
					}
					mHandler.sendEmptyMessageDelayed(1,
							minusSpeed > TOUCH_SPEED_MS ? minusSpeed -= 1
									: TOUCH_SPEED_MS);
				} else
					minusTouched = false;
				break;
			}
			case 2: {
				if (plus.isPressed()) {
					value = value == max ? min : value + 1;
					drawView();
					if (plusSpeed == TOUCH_MAX_MS) {
						Vibrator vibrator = (Vibrator) mContext
								.getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(VIBRATE_MS);
					}
					mHandler.sendEmptyMessageDelayed(2,
							plusSpeed > TOUCH_SPEED_MS ? plusSpeed -= 1
									: TOUCH_SPEED_MS);
				} else
					plusTouched = false;
				break;
			}
			}
		}
	};

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeAllViews();
	}

	private void addAllViews() {
		setOrientation(LinearLayout.VERTICAL);
		addView(plus);
		addView(text);
		addView(minus);
	}

	private String addZero() {
		return String
				.format("%02d", value);
	}

	private final void drawView() {
		text.setText(addZero());
	}

	public int getValue() {
		return value;
	}

	public String getStrValue() {
		return addZero();
	}

	public void setMaxValue(int max) {
		this.max = max;
	}

	public void setValue(int x) {
		this.value = x;
		drawView();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible)
			addAllViews();
		else
			removeAllViews();
	}
}
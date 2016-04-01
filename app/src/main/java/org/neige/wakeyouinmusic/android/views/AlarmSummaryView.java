package org.neige.wakeyouinmusic.android.views;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.models.Alarm;

public class AlarmSummaryView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

	private Alarm alarm;
	private OnAlarmChangeListener onAlarmChangeListener;

	//View
	private TextView timeTextView, labelTextView, periodTextView, ringtoneTextView;
	private Switch enableSwitch;

	public AlarmSummaryView(Context context, OnAlarmChangeListener onAlarmChangeListener) {
		super(context);
		this.onAlarmChangeListener = onAlarmChangeListener;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.view_alarm_summary, this, true);

		timeTextView = (TextView) v.findViewById(R.id.timeTextView);
		labelTextView = (TextView) v.findViewById(R.id.labelTextView);
		periodTextView = (TextView) v.findViewById(R.id.periodTextView);
		ringtoneTextView = (TextView) v.findViewById(R.id.ringtoneTextView);
		enableSwitch = (Switch) v.findViewById(R.id.enableSwitch);
	}

	public void initView(Alarm alarm) {
		this.alarm = alarm;

		enableSwitch.setOnCheckedChangeListener(null);

		labelTextView.setText(alarm.getLabel());
		timeTextView.setText(DateUtils.formatDateTime(getContext(), alarm.getTime().getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		ringtoneTextView.setText(alarm.getRingtone().getTitle());
		enableSwitch.setChecked(alarm.isEnable());
		periodTextView.setText(AlarmHelper.getInstance().getPeriodString(alarm));

		enableSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		alarm.setEnable(isChecked);
		if (onAlarmChangeListener != null) {
			onAlarmChangeListener.OnAlarmEnableChange(alarm);
		}
	}

	public interface OnAlarmChangeListener {
		public void OnAlarmEnableChange(Alarm alarm);
	}
}
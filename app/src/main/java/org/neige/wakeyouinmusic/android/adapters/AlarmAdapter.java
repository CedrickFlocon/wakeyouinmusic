package org.neige.wakeyouinmusic.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.views.AlarmSummaryView;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends BaseAdapter {

	private Context context;
	private List<Alarm> alarms = new ArrayList<>();
	private AlarmSummaryView.OnAlarmChangeListener onAlarmChangeListener;

	public AlarmAdapter(Context context, AlarmSummaryView.OnAlarmChangeListener onAlarmChangeListener) {
		this.context = context;
		this.onAlarmChangeListener = onAlarmChangeListener;
	}

	public void setAlarms(List<Alarm> alarms) {
		this.alarms.clear();
		this.alarms.addAll(alarms);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return alarms.size();
	}

	@Override
	public Object getItem(int position) {
		return alarms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new AlarmSummaryView(context, onAlarmChangeListener);
		}
		((AlarmSummaryView) convertView).initView((Alarm) getItem(position));
		return convertView;
	}
}

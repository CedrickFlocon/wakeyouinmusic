package org.neige.wakeyouinmusic.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.adapters.AlarmAdapter;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.views.AlarmSummaryView;

import java.util.ArrayList;
import java.util.List;

public class AlarmListFragment extends Fragment implements AlarmSummaryView.OnAlarmChangeListener {

	private static final String TAG = "AlarmListFragment";

	private OnFragmentInteractionListener onFragmentInteractionListener;
	private List<Alarm> alarms = new ArrayList<>();
	private AlarmAdapter alarmAdapter;

	//View
	private ListView listViewAlarm;

	public static AlarmListFragment newInstance() {
		AlarmListFragment fragment = new AlarmListFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmAdapter = new AlarmAdapter(getActivity(), this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_alarm_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		listViewAlarm = (ListView) view.findViewById(R.id.alarmListView);
		view.findViewById(R.id.actionButton).setOnClickListener(v -> onFragmentInteractionListener.onClickNewAlarm());

		listViewAlarm.setAdapter(alarmAdapter);
		listViewAlarm.setOnItemClickListener((parent, v, position, id) -> onFragmentInteractionListener.onClickAlarm(alarms.get(position)));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void setAlarmList(List<Alarm> alarms){
		this.alarms = alarms;
		alarmAdapter.setAlarms(alarms);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		onFragmentInteractionListener = null;
	}

	@Override
	public void OnAlarmEnableChange(Alarm alarm) {
		AlarmHelper.getInstance().setAlarm(alarm);
	}

	public interface OnFragmentInteractionListener {
		public void onClickAlarm(Alarm alarm);

		public void onClickNewAlarm();
	}

}
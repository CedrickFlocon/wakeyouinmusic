package org.neige.wakeyouinmusic.android.fragments;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.dialogs.RingtoneDialogFragment;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.models.DayOfWeek;
import org.neige.wakeyouinmusic.android.models.PlaylistRingtone;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.EnumSet;

public class AlarmDetailFragment extends Fragment {
	private static final String TAG = "AlarmDetailFragment";

	private static final String PARAMETER_ALARM = "alarm";
	private static final String DIALOG_RINGTONE_TAG = "ringtone";
	private Alarm alarm;

	//View
	private TextView textViewRingtone, textViewTime;
	private EditText editTextLabel;
	private ToggleButton toggleButtonMonday, toggleButtonTuesday, toggleButtonWednesday, toggleButtonThursday, toggleButtonFriday,
			toggleButtonSaturday, toggleButtonSunday;
	private Switch switchEnable;
	private CheckBox checkBoxVibrate, shuffleCheckBox;
	private RingtoneDialogFragment ringtoneDialogFragment;
	private TimePickerDialog timePickerDialog;

	public static AlarmDetailFragment newInstance(Alarm alarm) {
		AlarmDetailFragment fragment = new AlarmDetailFragment();
		Bundle bundle = new Bundle();
		if (alarm != null) {
			bundle.putSerializable(PARAMETER_ALARM, alarm);
		}
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			alarm = (Alarm) savedInstanceState.getSerializable(PARAMETER_ALARM);
		} else {
			alarm = (Alarm) getArguments().getSerializable(PARAMETER_ALARM);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_alarm_detail, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		textViewRingtone = (TextView) view.findViewById(R.id.ringtoneTextView);
		textViewTime = (TextView) view.findViewById(R.id.timeTextView);
		editTextLabel = (EditText) view.findViewById(R.id.labelEditText);
		switchEnable = (Switch) view.findViewById(R.id.enableSwitch);
		checkBoxVibrate = (CheckBox) view.findViewById(R.id.vibrateCheckBox);
		shuffleCheckBox = (CheckBox) view.findViewById(R.id.shuffleCheckBox);

		toggleButtonMonday = (ToggleButton) view.findViewById(R.id.mondayToggleButton);
		toggleButtonTuesday = (ToggleButton) view.findViewById(R.id.tuesdayToggleButton);
		toggleButtonWednesday = (ToggleButton) view.findViewById(R.id.wednesdayToggleButton);
		toggleButtonThursday = (ToggleButton) view.findViewById(R.id.thursdayToggleButton);
		toggleButtonFriday = (ToggleButton) view.findViewById(R.id.fridayToggleButton);
		toggleButtonSaturday = (ToggleButton) view.findViewById(R.id.saturdayToggleButton);
		toggleButtonSunday = (ToggleButton) view.findViewById(R.id.sundayToggleButton);

		textViewRingtone.setOnClickListener(v -> ringtonePicker());
		textViewTime.setOnClickListener(v -> timePicker());

		DateFormatSymbols symbols = new DateFormatSymbols();
		String[] dayNames = symbols.getShortWeekdays();

		settoggleText(toggleButtonMonday, dayNames[Calendar.MONDAY]);
		settoggleText(toggleButtonTuesday, dayNames[Calendar.TUESDAY]);
		settoggleText(toggleButtonWednesday, dayNames[Calendar.WEDNESDAY]);
		settoggleText(toggleButtonThursday, dayNames[Calendar.THURSDAY]);
		settoggleText(toggleButtonFriday, dayNames[Calendar.FRIDAY]);
		settoggleText(toggleButtonSaturday, dayNames[Calendar.SATURDAY]);
		settoggleText(toggleButtonSunday, dayNames[Calendar.SUNDAY]);

		initDisplay(this.alarm);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(PARAMETER_ALARM, alarm);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void settoggleText(ToggleButton toggleButton, String text) {
		toggleButton.setTextOff(text);
		SpannableString spannableString = new SpannableString(text);
		spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
		toggleButton.setTextOn(spannableString);
	}

	private void initDisplay(Alarm alarm) {
		switchEnable.setChecked(alarm.isEnable());
		editTextLabel.setText(alarm.getLabel());
		checkBoxVibrate.setChecked(alarm.isVibrate());

		toggleButtonMonday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.MONDAY));
		toggleButtonTuesday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.TUESDAY));
		toggleButtonWednesday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.WEDNESDAY));
		toggleButtonThursday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.THURSDAY));
		toggleButtonFriday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.FRIDAY));
		toggleButtonSaturday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.SATURDAY));
		toggleButtonSunday.setChecked(alarm.getDayOfWeeks().contains(DayOfWeek.SUNDAY));

		initRingtoneDisplay(alarm);
		initTimeDisplay(alarm);
	}

	private void initRingtoneDisplay(Alarm alarm) {
		textViewRingtone.setText(alarm.getRingtone().getTitle());
		if (alarm.getRingtone() instanceof PlaylistRingtone){
			shuffleCheckBox.setChecked(((PlaylistRingtone)alarm.getRingtone()).isShuffle());
			shuffleCheckBox.setVisibility(View.VISIBLE);
		}else {
			shuffleCheckBox.setVisibility(View.GONE);
			shuffleCheckBox.setChecked(false);
		}
	}

	private void initTimeDisplay(Alarm alarm) {
		textViewTime.setText(DateUtils.formatDateTime(getActivity(), alarm.getTime().getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
	}

	private void ringtonePicker() {
		if (ringtoneDialogFragment == null) {
			ringtoneDialogFragment = RingtoneDialogFragment.newInstance(() -> {
                displayRingtone(ringtoneDialogFragment.getSelectedRingtone() instanceof PlaylistRingtone);
                if (ringtoneDialogFragment.getSelectedRingtone() instanceof PlaylistRingtone){
                    ((PlaylistRingtone) ringtoneDialogFragment.getSelectedRingtone()).setShuffle(shuffleCheckBox.isChecked());
                }
                alarm.setRingtone(ringtoneDialogFragment.getSelectedRingtone());
                initRingtoneDisplay(alarm);
            });
		} else if (ringtoneDialogFragment.getDialog() != null && ringtoneDialogFragment.getDialog().isShowing()) {
			return;
		}
		ringtoneDialogFragment.setSelectedRingtone(alarm.getRingtone());
		ringtoneDialogFragment.show(getFragmentManager(), DIALOG_RINGTONE_TAG);
	}

	private void timePicker() {
		//TODO use theme in timePickerDialog
		timePickerDialog = new TimePickerDialog(getActivity(), (view, hourOfDay, minute) -> {
			alarm.getTime().set(Calendar.HOUR_OF_DAY, hourOfDay);
			alarm.getTime().set(Calendar.MINUTE, minute);
			initTimeDisplay(alarm);
		}, alarm.getTime().get(Calendar.HOUR_OF_DAY), alarm.getTime().get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));

		timePickerDialog.show();
	}

	private void displayRingtone(boolean visibility){
		shuffleCheckBox.setVisibility(visibility ? View.VISIBLE : View.GONE);
	}

	public Alarm getAlarm() {
		if (alarm.getRingtone() instanceof PlaylistRingtone){
			((PlaylistRingtone) alarm.getRingtone()).setShuffle(shuffleCheckBox.isChecked());
		}

		alarm.setLabel(editTextLabel.getText().toString());

		EnumSet dayOfWeek = EnumSet.noneOf(DayOfWeek.class);
		if (toggleButtonMonday.isChecked()) {
			dayOfWeek.add(DayOfWeek.MONDAY);
		}
		if (toggleButtonTuesday.isChecked()) {
			dayOfWeek.add(DayOfWeek.TUESDAY);
		}
		if (toggleButtonWednesday.isChecked()) {
			dayOfWeek.add(DayOfWeek.WEDNESDAY);
		}
		if (toggleButtonThursday.isChecked()) {
			dayOfWeek.add(DayOfWeek.THURSDAY);
		}
		if (toggleButtonFriday.isChecked()) {
			dayOfWeek.add(DayOfWeek.FRIDAY);
		}
		if (toggleButtonSaturday.isChecked()) {
			dayOfWeek.add(DayOfWeek.SATURDAY);
		}
		if (toggleButtonSunday.isChecked()) {
			dayOfWeek.add(DayOfWeek.SUNDAY);
		}
		alarm.setDayOfWeeks(dayOfWeek);

		alarm.setVibrate(checkBoxVibrate.isChecked());
		alarm.setEnable(switchEnable.isChecked());

		return alarm;
	}

	public RingtoneDialogFragment getRingtoneDialogFragment() {
		return ringtoneDialogFragment;
	}
}

package org.neige.wakeyouinmusic.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.players.PlayerException;
import org.neige.wakeyouinmusic.android.players.Track;

import java.util.Calendar;

public class AlarmAlertFragment extends Fragment implements View.OnTouchListener {

	private static final String PARAMETER_ALARM = "alarm";
	private static final String TAG = "AlarmAlertFragment";

	private OnFragmentInteractionListener onFragmentInteractionListener;

	private Alarm alarm;

	private Runnable updateDateRunnable;
	private Handler updateDateHandler;
	private float distanceToSwipe;

	//View
	private ImageButton nextImageButton, previousImageButton;
	private ImageView coverImageView;
	private TextView trackTitleTextView, artisteTextView, alarmLabelTextView, errorTextView, timeTextView, dateTextView, swipeHintTextView;
	private View animatedViewDismiss, animatedViewSnooze, animatedViewAlarm;

	public static AlarmAlertFragment newInstance(Alarm alarm) {
		AlarmAlertFragment fragment = new AlarmAlertFragment();
		Bundle bundle = new Bundle();
		if (alarm != null) {
			bundle.putSerializable(PARAMETER_ALARM, alarm);
		}
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

	public AlarmAlertFragment() {
		updateDateHandler = new Handler();

		updateDateRunnable = () -> {
			updateDateTimeInformation();
			updateDateHandler.postDelayed(updateDateRunnable, 1000);
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		alarm = (Alarm) getArguments().getSerializable(PARAMETER_ALARM);

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		distanceToSwipe = size.x /4;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_alarm_alert, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		nextImageButton = (ImageButton) view.findViewById(R.id.nextButton);
		previousImageButton = (ImageButton) view.findViewById(R.id.previousButton);

		coverImageView = (ImageView) view.findViewById(R.id.coverImageView);
		trackTitleTextView = (TextView) view.findViewById(R.id.trackTitleTextView);
		artisteTextView = (TextView) view.findViewById(R.id.artisteTextView);
		alarmLabelTextView = (TextView) view.findViewById(R.id.alarmLabelTextView);
		timeTextView = (TextView) view.findViewById(R.id.timeTextView);
		dateTextView = (TextView) view.findViewById(R.id.dateTextView);
		errorTextView = (TextView) view.findViewById(R.id.errorTextView);
		swipeHintTextView = (TextView) view.findViewById(R.id.swipeHintTextView);

		animatedViewAlarm = view.findViewById(R.id.animatedViewAlarm);
		animatedViewDismiss = view.findViewById(R.id.animatedViewDismiss);
		animatedViewSnooze = view.findViewById(R.id.animatedViewSnooze);


		nextImageButton.setOnClickListener(v -> onFragmentInteractionListener.onNext());
		previousImageButton.setOnClickListener(v -> onFragmentInteractionListener.onPrevious());

		alarmLabelTextView.setText(alarm.getLabel());

		Animation growUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.button_alarm_grow_up);
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.button_alarm_fade_out);
		fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) { }

			@Override
			public void onAnimationEnd(Animation animation) {
				animatedViewAlarm.startAnimation(growUpAnimation);
				animatedViewAlarm.setAlpha(1);
			}

			@Override
			public void onAnimationRepeat(Animation animation) { }
		});

		growUpAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				animatedViewAlarm.startAnimation(fadeOutAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		animatedViewAlarm.startAnimation(growUpAnimation);
		view.findViewById(R.id.staticViewAlarm).setOnTouchListener(this);

		updateDateTimeInformation();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDateHandler.post(updateDateRunnable);
	}

	@Override
	public void onPause() {
		super.onPause();
		updateDateHandler.removeCallbacks(updateDateRunnable);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		onFragmentInteractionListener = null;
	}

	private void updateDateTimeInformation() {
		timeTextView.setText(DateUtils.formatDateTime(getActivity(), Calendar.getInstance().getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		dateTextView.setText(DateUtils.formatDateTime(getActivity(), Calendar.getInstance().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY));
	}

	public void updateTrackInformation(Track track) {
		trackTitleTextView.setText(track.getName());
		artisteTextView.setText(track.getArtiste());
		if (track.getCoverUrl() != null && track.getCoverUrl().length() > 0) {
			if (getActivity() != null) {
				Picasso.with(getActivity()).load(track.getCoverUrl()).into(coverImageView);
			}else{
				Log.e(TAG, "The activity is already detach"); //probably happen by a not clear callback
			}
		}

		playBackButton(track.getCoverUrl() != null);
	}

	public void updateErrorInformation(PlayerException e) {
		artisteTextView.setVisibility(View.GONE);
		coverImageView.setVisibility(View.GONE);
		errorTextView.setText(e.getLocalizedMessage());

		playBackButton(false);
	}

	private void playBackButton(boolean display){
		nextImageButton.setVisibility(display ? View.VISIBLE : View.INVISIBLE);
		previousImageButton.setVisibility(display ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int pointerIndex = MotionEventCompat.getActionIndex(event);
		float x = MotionEventCompat.getX(event, pointerIndex) - v.getLayoutParams().width / 2;
		switch(event.getActionMasked()){
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				float percentage = Math.min(100,Math.abs(x) * 100 / distanceToSwipe);
				if (x > 0){
					animatedViewDismiss.setAlpha(percentage/100);
					animatedViewSnooze.setAlpha(0);
				}else {
					animatedViewSnooze.setAlpha(percentage/100);
					animatedViewDismiss.setAlpha(0);
				}

				break;
			case MotionEvent.ACTION_UP:
				if (x > distanceToSwipe){
					onFragmentInteractionListener.onDismiss();
				}else if (Math.abs(x) > distanceToSwipe){
					onFragmentInteractionListener.onSnooze();
				}
			case MotionEvent.ACTION_CANCEL:
				animatedViewDismiss.setAlpha(0);
				animatedViewSnooze.setAlpha(0);
				swipeHintTextView.setVisibility(View.VISIBLE);
				break;
			default:
				return false;
		}
		return true;
	}

	public interface OnFragmentInteractionListener {
		public void onNext();

		public void onPrevious();

		public void onDismiss();

		public void onSnooze();
	}

}

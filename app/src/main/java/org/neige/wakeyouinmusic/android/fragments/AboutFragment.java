package org.neige.wakeyouinmusic.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.neige.wakeyouinmusic.android.BuildConfig;
import org.neige.wakeyouinmusic.android.R;

public class AboutFragment extends Fragment {

	private static final String TAG = "AboutFragment";

	private static final String MARKET_APP_BASE_URI = "market://details?id=";
	private static final String MARKET_WEB_BASE_URI = "http://play.google.com/store/apps/details?id=";
	private static final String EMMA_WEB_SITE_LINK = "http://www.emma-wiest.fr";

	private OnFragmentInteractionListener onFragmentInteractionListener;

	//View
	TextView versionTextView, rateApplicationTextView, emmaLinkTextView;

	public static AboutFragment newInstance(){
		AboutFragment fragment = new AboutFragment();
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

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		versionTextView = (TextView) view.findViewById(R.id.versionTextView);
		rateApplicationTextView = (TextView) view.findViewById(R.id.rateApplicationTextView);
		emmaLinkTextView = (TextView) view.findViewById(R.id.emmaLinkTextView);

		versionTextView.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

		rateApplicationTextView.setOnClickListener(v -> {
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_APP_BASE_URI + getActivity().getPackageName())));
			} catch (ActivityNotFoundException e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_WEB_BASE_URI + getActivity().getPackageName())));
			}
		});

		emmaLinkTextView.setOnClickListener(v -> {
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(EMMA_WEB_SITE_LINK)));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "Can't link : " + EMMA_WEB_SITE_LINK, e);
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();
		onFragmentInteractionListener = null;
	}

	public interface OnFragmentInteractionListener {}

}

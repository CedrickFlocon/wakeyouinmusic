package org.neige.wakeyouinmusic.android;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ActionPreference extends Preference implements View.OnClickListener {

	private View.OnClickListener onClickListener;

	public ActionPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View layout = super.onCreateView(parent);

		View view = ((ViewGroup)layout.findViewById(android.R.id.widget_frame)).getChildAt(0);
		if (view != null){
			view.setOnClickListener(this);
		}

		return layout;
	}

	public void setWidgetClickListener(View.OnClickListener clickListener) {
		onClickListener = clickListener;
	}

	@Override
	public void onClick(View v) {
		if (onClickListener != null) {
			onClickListener.onClick(v);
		}
	}
}

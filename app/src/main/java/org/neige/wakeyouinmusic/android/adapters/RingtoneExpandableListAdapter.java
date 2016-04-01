package org.neige.wakeyouinmusic.android.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.models.Ringtone;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RingtoneExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private Ringtone selectedRingtone;
	private LinkedHashMap<Class<? extends Ringtone>, Status> ringtoneStatus = new LinkedHashMap<>();
	private LinkedHashMap<Class<? extends Ringtone>, String> ringtonesErrorMessage = new LinkedHashMap<>();
	private LinkedHashMap<Class<? extends Ringtone>, String> ringtonesDisconnectMessage = new LinkedHashMap<>();
	private LinkedHashMap<Class<? extends Ringtone>, List<Ringtone>> ringtonesPlaylist = new LinkedHashMap<>();
	private ActionListener actionListener;

	public RingtoneExpandableListAdapter(Context context, LinkedHashMap<Class<? extends Ringtone>, Status> ringtoneStatus) {
		this.context = context;
		this.ringtoneStatus = ringtoneStatus;
	}

	@Override
	public int getGroupCount() {
		return ringtoneStatus.keySet().size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		switch (ringtoneStatus.get(ringtoneStatus.keySet().toArray()[groupPosition])) {
			case LOADED:
				return ringtonesPlaylist.get(ringtoneStatus.keySet().toArray()[groupPosition]).size();
			case LOADING:
				return ringtonesPlaylist.get(ringtoneStatus.keySet().toArray()[groupPosition]).size() + 1;
			case DISCONNECT:
			case ERROR:
			default:
				return 1;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		return ringtoneStatus.keySet().toArray()[groupPosition];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (ringtoneStatus.get(ringtoneStatus.keySet().toArray()[groupPosition]) == Status.LOADED || ringtoneStatus.get(ringtoneStatus.keySet().toArray()[groupPosition]) == Status.LOADING) {
			if (ringtonesPlaylist.get(getGroup(groupPosition)).size() > childPosition) {
				return ringtonesPlaylist.get(getGroup(groupPosition)).toArray()[childPosition];
			} else {
				return Status.LOADING;
			}
		} else {
			return ringtoneStatus.get(ringtoneStatus.keySet().toArray()[groupPosition]);
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_ringtone_type, parent, false);
		}
		String name;
		if (getGroup(groupPosition) == DeezerRingtone.class) {
			name = context.getResources().getString(R.string.ringtone_picker_deezer_ringtone);
		} else if (getGroup(groupPosition) == SpotifyRingtone.class) {
			name = context.getResources().getString(R.string.ringtone_picker_spotify_ringtone);
		} else if (getGroup(groupPosition) == DefaultRingtone.class) {
			name = context.getResources().getString(R.string.ringtone_picker_default_ringtone);
		} else {
			name = ((Class) getGroup(groupPosition)).getSimpleName();
		}
		((TextView) convertView.findViewById(R.id.ringtoneTypeTextView)).setText(name);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		switch (Status.values()[getChildType(groupPosition, childPosition)]) {
			case DISCONNECT:
				if (convertView == null) {
					convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_ringtone_disconnected, parent, false);
				}
				convertView.findViewById(R.id.signInButton).setOnClickListener(v -> actionListener.onConnect((Class<Ringtone>) getGroup(groupPosition)));
				String disconnectedMessage = ringtonesDisconnectMessage.get(getGroup(groupPosition));
				((TextView) convertView.findViewById(R.id.disconnectedTextView)).setText(disconnectedMessage != null ? disconnectedMessage : context.getString(R.string.disconnected));
				break;
			case LOADING:
				if (convertView == null) {
					convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_ringtone_loading, parent, false);
				}
				break;
			case ERROR:
				if (convertView == null) {
					convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_ringtone_error, parent, false);
				}
				convertView.findViewById(R.id.errorButton).setOnClickListener(v -> actionListener.onRetry((Class<Ringtone>) getGroup(groupPosition)));
				String errorMessage = ringtonesErrorMessage.get(getGroup(groupPosition));
				((TextView) convertView.findViewById(R.id.errorTextView)).setText(errorMessage != null ? errorMessage : context.getString(R.string.error_unknown));

				break;
			case LOADED:
				if (convertView == null) {
					convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_ringtone, parent, false);
				}

				RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
				radioButton.setTag(getChild(groupPosition, childPosition));
				radioButton.setText(((Ringtone) getChild(groupPosition, childPosition)).getTitle());

				radioButton.setOnCheckedChangeListener(null);
				radioButton.setChecked(radioButton.getTag().equals(selectedRingtone));
				radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
					if (isChecked) {
						selectedRingtone = (Ringtone) radioButton.getTag();
					}
					notifyDataSetChanged();
				});

				break;
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return getChildType(groupPosition, childPosition) == Status.LOADED.ordinal();
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		if (!(getChild(groupPosition, childPosition) instanceof Status)) {
			return Status.LOADED.ordinal();
		} else {
			return ((Status) getChild(groupPosition, childPosition)).ordinal();
		}
	}

	@Override
	public int getChildTypeCount() {
		return Status.values().length;
	}

	public Ringtone getSelectedRingtone() {
		return selectedRingtone;
	}

	public void setSelectedRingtone(Ringtone selectedRingtone) {
		this.selectedRingtone = selectedRingtone;
	}

	public void changeStatusToError(Class<? extends Ringtone> classType, String errorMessage) {
		ringtoneStatus.put(classType, Status.ERROR);
		ringtonesErrorMessage.put(classType, errorMessage);
		notifyDataSetChanged();
	}

	public void changeStatusToLoading(Class<? extends Ringtone> classType) {
		ringtoneStatus.put(classType, Status.LOADING);
		ringtonesPlaylist.put(classType, new ArrayList<>());
		notifyDataSetChanged();
	}

	public void changeStatusToNotConnected(Class<? extends Ringtone> classType, String message) {
		ringtoneStatus.put(classType, Status.DISCONNECT);
		ringtonesDisconnectMessage.put(classType, message);
		notifyDataSetChanged();
	}


	public void changeStatusToLoaded(Class<? extends Ringtone> classType, @Nullable List<Ringtone> ringtones, boolean hasMore) {
		ringtoneStatus.put(classType, hasMore ? Status.LOADING : Status.LOADED);
		ringtonesPlaylist.put(classType, ringtones);
		notifyDataSetChanged();
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public enum Status {
		DISCONNECT,
		LOADING,
		LOADED,
		ERROR,
	}

	public interface ActionListener {
		public void onRetry(Class<Ringtone> ringtoneClass);

		public void onConnect(Class<Ringtone> ringtoneClass);
	}
}

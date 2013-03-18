package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsAdapter extends BaseAdapter {

	private static final double AVATAR_DIP = 75.;
	
	private final Activity parent;
	
	public SubscribedChannelsAdapter(Activity parent) {
		this.parent = parent;
		fetchSubscribers();
	}
	
	private void fetchSubscribers() {
		SubscribedChannelsModel.getInstance().refresh(parent, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				notifyDataSetChanged();
				fetchMetadata();
				fetchCounters();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void fetchMetadata() {
		JSONArray subscribedChannels = SubscribedChannelsModel.getInstance().get(parent);
		for (int i = 0; i < subscribedChannels.length(); i++) {
			String channel = subscribedChannels.optString(i);
			
			ChannelMetadataModel.getInstance().refresh(parent, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					notifyDataSetChanged();
				}
				
				@Override
				public void error(Throwable throwable) {
					// TODO Auto-generated method stub
				}
			}, channel);
		}
	}

	protected void fetchCounters() {
		SyncModel.getInstance().refresh(parent, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}

	private String getAvatarURL(String channel) {
		int avatarSize = (int) (AVATAR_DIP * parent.getResources().getDisplayMetrics().density + 0.5);
		String apiAddress = Preferences.getPreference(parent, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize;
	}
	
	@Override
	public int getCount() {
		return SubscribedChannelsModel.getInstance().get(parent).length();
	}

	@Override
	public Object getItem(int arg0) {
		return SubscribedChannelsModel.getInstance().get(parent).optString(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return SubscribedChannelsModel.getInstance().get(parent).optString(arg0).hashCode();
	}

	@Override
	public View getView(int position, View arg1, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		View retView = inflater.inflate(R.layout.subscriber_entry, viewGroup, false);

		String channelJid = SubscribedChannelsModel.getInstance().get(parent).optString(position);
		JSONObject metadata = ChannelMetadataModel.getInstance().get(parent, channelJid);
		
		String channelTitle = channelJid;
		String channelDescription = null;
		
		if (metadata != null) {
			channelTitle = metadata.optString("title");
			channelDescription = metadata.optString("description");
		}
		
		TextView channelTitleView = (TextView) retView.findViewById(R.id.bcUserId);
		channelTitleView.setText(channelTitle);

     	TextView descriptionView = (TextView) retView.findViewById(R.id.bcMessage);
		descriptionView.setText(channelDescription);
		
		SmartImageView avatarView = (SmartImageView) retView.findViewById(R.id.bcProfilePic);
		avatarView.setImageUrl(getAvatarURL(channelJid), R.drawable.personal_50px);

		JSONObject counters = SyncModel.getInstance().get(parent, channelJid);
		
		if (counters != null) {
			TextView unreadCounterView = (TextView) retView.findViewById(R.id.unreadCounter);
			unreadCounterView.setText(counters.optString("totalCount"));
		}
		
//		
//		SharedPreferences sharedPreferences = parent.getSharedPreferences(Preferences.PREFS_NAME, 0);
//		String myChannel = sharedPreferences.getString(Preferences.MY_CHANNEL_JID, null);
//		
//		if (subscribedChannel.getJid().equals(myChannel)) {
//			retView.setBackgroundColor(
//					viewGroup.getResources().getColor(R.color.bc_bg_grey));
//			retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//					viewGroup.getResources().getColor(R.color.bc_orange));
//		} else {
//			if (subscribedChannel.getUnread() == 0) {
//				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//						viewGroup.getResources().getColor(R.color.bc_bg_grey));
//			} else {
//				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//						viewGroup.getResources().getColor(R.color.bc_green));
//			}
//		}
		
        return retView;
	}
}

package de.ur.mi.android.teamseeker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.UserData;
import de.ur.mi.android.teamseeker.helpers.Utility;

public class ParticipantAdapter extends ArrayAdapter<UserData> {
    private ArrayList<UserData> participants;
    private EventData eventData;

    public ParticipantAdapter(@NonNull Context context, EventData eventData, ArrayList<UserData> participants) {
        super(context, R.layout.adapter_item_participants, participants);
        this.eventData = eventData;
        this.participants = participants;
    }

    @Override
    public int getCount() {
        return participants.size();
    }

    public void updateList(ArrayList<UserData> participants, EventData eventData) {
        this.participants = participants;
        this.eventData = eventData;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View entry = convertView;
        if (entry == null) {
            entry = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_participants, parent, false);
        }
        UserData userData = participants.get(position);

        ImageView imageView_hostIcon = entry.findViewById(R.id.imageView_hostIcon);
        setHostStar(userData, imageView_hostIcon);

        TextView textView_participantName = entry.findViewById(R.id.textView_participantName);
        textView_participantName.setText(userData.getUsername());

        TextView textView_participantAge = entry.findViewById(R.id.textView_participantAge);
        textView_participantAge.setText(String.valueOf(Utility.calculateAge(userData.getBirthDate())));

        return entry;
    }

    @Nullable
    @Override
    public UserData getItem(int position) {
        return participants.get(position);
    }

    private void setHostStar(UserData user, ImageView hostIcon) {
        if (user.getUserID().equals(eventData.getEventID())) {
            hostIcon.setVisibility(View.VISIBLE);
        } else {
            hostIcon.setVisibility(View.INVISIBLE);
        }
    }
}

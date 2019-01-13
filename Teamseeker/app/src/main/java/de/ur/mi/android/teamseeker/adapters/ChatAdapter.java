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

import de.ur.mi.android.teamseeker.ChatMessage;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.R;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {
    private ArrayList<ChatMessage> chatMessages;
    private EventData eventData;

    public ChatAdapter(@NonNull Context context, EventData eventData, ArrayList<ChatMessage> chatMessages) {
        super(context, R.layout.adapter_item_chat, chatMessages);
        this.chatMessages = chatMessages;
        this.eventData = eventData;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    public void updateChat(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View entry = convertView;
        if (entry == null) {
            entry = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_chat, parent, false);
        }
        ChatMessage chatMessage = chatMessages.get(position);

        TextView textView_username = entry.findViewById(R.id.textView_username);
        TextView textView_message = entry.findViewById(R.id.textView_message);
        TextView textView_time = entry.findViewById(R.id.textView_time);
        TextView textView_date = entry.findViewById(R.id.textView_date);
        ImageView imageView_hostIcon = entry.findViewById(R.id.imageView_hostIcon);

        String displayName = chatMessage.getUserName() + ":";
        textView_username.setText(displayName);
        textView_time.setText(chatMessage.getTime().getTimeAsString());
        textView_date.setText(chatMessage.getDate().getDateAsString());
        textView_message.setText(chatMessage.getMessage());
        setHostStar(chatMessage, imageView_hostIcon);

        return entry;
    }

    @Nullable
    @Override
    public ChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    private void setHostStar(ChatMessage chatMessage, ImageView hostIcon) {
        if (chatMessage.getUserID().equals(eventData.getEventID())) {
            hostIcon.setVisibility(View.VISIBLE);
        } else {
            hostIcon.setVisibility(View.INVISIBLE);
        }
    }
}

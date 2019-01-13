package de.ur.mi.android.teamseeker.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import de.ur.mi.android.teamseeker.ChatMessage;
import de.ur.mi.android.teamseeker.DatabaseManager;
import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.ProfileActivity;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.UserData;
import de.ur.mi.android.teamseeker.adapters.ChatAdapter;
import de.ur.mi.android.teamseeker.helpers.DateContainer;
import de.ur.mi.android.teamseeker.helpers.TimeContainer;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;

import static android.app.Activity.RESULT_OK;

public class EventChatFragment extends Fragment implements EventFragment {
    private ListView chat;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private EventData eventData;

    private Button button_sendMessage;
    private EditText editText_chatMessage;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        getViews(view);
        updateEventData(eventData);
        setupChat(view);
        setOnSendAction();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fetchEventData();

    }

    public static EventChatFragment newInstance() {
        Bundle args = new Bundle();
        EventChatFragment fragment = new EventChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //region Setup
    //region Setup Views
    private void getViews(View v) {
        button_sendMessage = v.findViewById(R.id.button_sendMessage);
        editText_chatMessage = v.findViewById(R.id.editText_chatMessage);
    }

    //endregion

    //region Setup Chat
    private void setupChat(View view) {
        chat = view.findViewById(R.id.listView_chat);

        chatAdapter = new ChatAdapter(context, eventData, chatMessages);
        chat.post(new Runnable() {
            @Override
            public void run() {
                //adapter needs to be set on ui thread, doesn't work otherwise
                chat.setAdapter(chatAdapter);
            }
        });
        updateEventData(eventData);

        chat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userID = chatAdapter.getItem(position).getUserID();
                DatabaseManager.getData(UserData.class, DatabaseManager.DB_KEY_USER, UserData.USERID_KEY, userID, new OnDataDownloadCompleteListener<UserData>() {
                    @Override
                    public void onDataDownloadComplete(List<UserData> data, int resultCode) {
                        if (resultCode == RESULT_OK) {
                            UserData userData = data.get(0);
                            Intent profileIntent = new Intent(context, ProfileActivity.class);
                            profileIntent.putExtra(getString(R.string.user_intent_key), userData);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        });
    }

    private void setOnSendAction() {
        button_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText_chatMessage.getText().toString().isEmpty()) {
                    return;
                } else if (editText_chatMessage.getText().toString().length() > getResources().getInteger(R.integer.chat_message_length_max)) {
                    Toast.makeText(context, getString(R.string.error_chatmessagesize_1) + getResources().getInteger(R.integer.chat_message_length_max) + getString(R.string.error_chatmessagesize_2), Toast.LENGTH_SHORT).show();
                    return;
                }
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setDate(DateContainer.getCurrentDate());
                chatMessage.setTime(TimeContainer.getCurrentTime());
                chatMessage.setUserID(userID);
                chatMessage.setUserName(PreferenceManager.getDefaultSharedPreferences(context).getString(
                        UserData.USERNAME_KEY + FirebaseAuth.getInstance().getCurrentUser().getUid(), getString(R.string.error_usernamenotfound)));
                chatMessage.setMessage(editText_chatMessage.getText().toString());
                chatMessage.addReadBy(userID);
                editText_chatMessage.setText(null);
                if(eventData.getChatMessages().size() >= 50){
                    ArrayList<ChatMessage> newMessageSet = eventData.getChatMessages();
                    newMessageSet.remove(0);
                    eventData.setChatMessages(newMessageSet);
                }
                eventData.addChatMessage(chatMessage);
                DatabaseManager.updateData(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, eventData.getEventID(), eventData, new OnCompleteListener() {
                    @Override
                    public void onComplete(int resultCode) {
                        Toast.makeText(context, R.string.note_messagesent, Toast.LENGTH_SHORT).show();
                    }
                });
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });
    }
    //endregion

    //region Chat Management
    private void markMessagesAsRead() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean sendToCloud = false;
        for (ChatMessage message : eventData.getChatMessages()) {
            if (!message.getReadBy().contains(userID)) {
                message.addReadBy(userID);
                sendToCloud = true;
            }
        }
        if (sendToCloud) {
            DatabaseManager.updateData(DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, eventData.getEventID(), eventData, new OnCompleteListener() {
                @Override
                public void onComplete(int resultCode) {
                }
            });
        }
    }
    //endregion
    //endregion

    //region Interface Methods
    @Override
    public void fetchEventData() {
        eventData = ((EventActivity) context).getCurrentEvent();
    }

    @Override
    public void onFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            fetchEventData();
            updateEventData(eventData);
        }
    }

    @Override
    public void updateEventData(EventData updatedEventData) {
        eventData = updatedEventData;
        this.chatMessages = updatedEventData.getChatMessages();
        markMessagesAsRead();
        if (chatAdapter != null) {
            chatAdapter.updateChat(chatMessages);
            chatAdapter.notifyDataSetChanged();
            chat.setSelection(chatAdapter.getCount() - 1);
        }
    }
    //endregion
}

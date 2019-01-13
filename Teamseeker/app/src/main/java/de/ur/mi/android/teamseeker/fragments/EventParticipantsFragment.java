package de.ur.mi.android.teamseeker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.common.util.ListUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.ur.mi.android.teamseeker.DatabaseManager;
import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.ProfileActivity;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.UserData;
import de.ur.mi.android.teamseeker.adapters.ParticipantAdapter;
import de.ur.mi.android.teamseeker.helpers.Utility;

import static android.app.Activity.RESULT_OK;

public class EventParticipantsFragment extends Fragment implements EventFragment {
    private ListView participantList;
    private ParticipantAdapter participantAdapter;
    private ArrayList<UserData> participants = new ArrayList<>();
    private EventData eventData;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participants, container, false);
        participantList = view.findViewById(R.id.listView_participants);
        setupParticipantSearchFunction(view);
        setOnClickListeners();
        updateEventData(eventData);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fetchEventData();
    }

    public static EventParticipantsFragment newInstance() {
        Bundle args = new Bundle();
        EventParticipantsFragment fragment = new EventParticipantsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //region UI Setup
    private void setupParticipantSearchFunction(View view) {
        EditText editText_searchParticipant = view.findViewById(R.id.editText_searchParticipant);
        editText_searchParticipant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    participantAdapter.updateList(participants, eventData);
                    participantAdapter.notifyDataSetChanged();
                } else if (s.length() > 0) {
                    ArrayList<UserData> allResults = new ArrayList<>();
                    for (UserData user : participants) {
                        if (user.getUsername().toLowerCase().contains(s.toString().toLowerCase())) {
                            allResults.add(user);
                        }
                    }
                    participantAdapter.updateList(allResults, eventData);
                    participantAdapter.notifyDataSetChanged();
                }
            }
        });

        participantAdapter = new ParticipantAdapter(getContext(), eventData, participants);
        participantList.post(new Runnable() {
            @Override
            public void run() {
                //adapter needs to be set on uithread, doesn't work otherwise
                participantList.setAdapter(participantAdapter);
            }
        });

    }

    private void setOnClickListeners() {
        participantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData userData = participantAdapter.getItem(position);
                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtra(getString(R.string.user_intent_key), userData);
                profileIntent.putExtra(getString(R.string.return_intent_key), eventData);
                startActivity(profileIntent);
            }
        });
        participantList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                EventActivity eventActivity = (EventActivity) context;
                if (eventActivity.userIsHost()) {
                    final UserData userData = participantAdapter.getItem(position);
                    if(userData.getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        return true;
                    }
                    Utility.createAlertDialog(context, true, getString(R.string.kick_1) + userData.getUsername() + getString(R.string.kick_2), getString(R.string.kick), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EventActivity) context).kickUser(userData.getUserID());
                        }
                    }, null, null, getString(R.string.cancel), null);
                    return true;
                }
                return false;
            }
        });
    }
    //endregion

    //region interface methods
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
    public void updateEventData(final EventData updatedEventData) {
        DatabaseManager.getUsers(DatabaseManager.DB_KEY_USER, updatedEventData.getParticipants(), new de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener<UserData>() {
            @Override
            public void onDataDownloadComplete(final List<UserData> data, int resultCode) {
                EventParticipantsFragment.this.participants = (ArrayList<UserData>) data;
                if (participantAdapter != null) {
                    participantAdapter.updateList(EventParticipantsFragment.this.participants, updatedEventData);
                    participantAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    //endregion
}

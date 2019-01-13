package de.ur.mi.android.teamseeker.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.common.util.ArrayUtils;

import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.R;

public class EventInfoFragment extends Fragment implements EventFragment {

    private EditText editText_eventName, editText_eventTime, editText_eventDate, editText_eventMinAge, editText_eventDesc;
    private Spinner spinner_eventType;
    private Button button_changeEventParticipationState, button_editEvent;

    private EventData eventData;

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_show, container, false);

        getViews(view);
        setupSpinner();
        updateEventData(eventData);

        ((EventActivity) context).setButtonReferences(button_changeEventParticipationState, button_editEvent);
        setupButtons();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fetchEventData();
    }

    public static EventInfoFragment newInstance() {

        Bundle args = new Bundle();

        EventInfoFragment fragment = new EventInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //region Setup
    //region Setup UI
    private void getViews(View view) {
        editText_eventName = view.findViewById(R.id.editText_eventName);
        editText_eventDesc = view.findViewById(R.id.editText_eventDesc);
        editText_eventMinAge = view.findViewById(R.id.editText_eventMinAge);
        editText_eventDate = view.findViewById(R.id.editText_eventDate);
        editText_eventTime = view.findViewById(R.id.editText_eventTime);
        spinner_eventType = view.findViewById(R.id.spinner_eventType);

        button_editEvent = view.findViewById(R.id.button_editEvent);
        button_changeEventParticipationState = view.findViewById(R.id.button_changeEventParticipationState);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.eventType));
        spinner_eventType.setAdapter(adapter);
        spinner_eventType.setEnabled(false);
    }

    private void setupButtons() {
        final EventActivity eventActivity = (EventActivity) context;
        updateButtonState();
        button_changeEventParticipationState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventActivity.onClickParticipation();
            }
        });
        button_editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventActivity.onClickEdit();
            }
        });
    }
    //endregion

    //region UI Management
    private void updateButtonState() {
        final EventActivity eventActivity = (EventActivity) context;
        eventActivity.setButtonState();
    }
    //endregion
    //endregion

    //region interface methods
    @Override
    public void fetchEventData() {
        eventData = ((EventActivity) context).getCurrentEvent();
    }


    @Override
    public void updateEventData(EventData updatedEventData) {
        if (updatedEventData.getEventName() == null || updatedEventData.getEventName().isEmpty()) {
            //An event with no or null name has most likely not been created yet and is just a shell, thereofre there is nothing to update
            return;
        }
        editText_eventName.setText(updatedEventData.getEventName());
        editText_eventDesc.setText(updatedEventData.getEventDescription());
        editText_eventMinAge.setText(String.valueOf(updatedEventData.getMinParticipantAge()));
        editText_eventDate.setText(updatedEventData.getEventDate().getDateAsString());
        editText_eventTime.setText(updatedEventData.getEventTime().getTimeAsString());
        spinner_eventType.setSelection(ArrayUtils.indexOf(getResources().getStringArray(R.array.eventType), updatedEventData.getEventType()));
    }


    @Override
    public void onFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            fetchEventData();
            updateButtonState();
            updateEventData(eventData);
        }
    }
    //endregion


}
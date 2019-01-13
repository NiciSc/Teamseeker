package de.ur.mi.android.teamseeker.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.common.util.ArrayUtils;

import de.ur.mi.android.teamseeker.helpers.DateSelectionDialog;
import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.EventFragment;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.TimeSelectionDialog;
import de.ur.mi.android.teamseeker.helpers.DateContainer;
import de.ur.mi.android.teamseeker.helpers.TimeContainer;

public class EventEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, EventFragment {

    private EditText editText_eventName, editText_eventTime, editText_eventDate, editText_eventMinAge, editText_eventDesc, editText_eventMaxParticipants;
    private Spinner spinner_eventType;
    private Button button_changeEventParticipationState, button_editEvent;
    private EventData eventData;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_edit, container, false);

        getViews(view);
        setupSpinner();
        ((EventActivity) context).setButtonReferences(button_changeEventParticipationState, button_editEvent);
        setupButtons();
        setOnClickListeners();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    //region Setup
    //region Setup UI
    private void getViews(View view) {
        editText_eventName = view.findViewById(R.id.editText_eventName);
        editText_eventDesc = view.findViewById(R.id.editText_eventDesc);
        editText_eventMinAge = view.findViewById(R.id.editText_eventMinAge);
        editText_eventDate = view.findViewById(R.id.editText_eventDate);
        editText_eventTime = view.findViewById(R.id.editText_eventTime);
        editText_eventMaxParticipants = view.findViewById(R.id.editText_eventMaxParticipants);
        spinner_eventType = view.findViewById(R.id.spinner_eventType);

        button_editEvent = view.findViewById(R.id.button_editEvent);
        button_changeEventParticipationState = view.findViewById(R.id.button_changeEventParticipationState);
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

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.eventType));
        spinner_eventType.setAdapter(adapter);
        spinner_eventType.setEnabled(true);
    }

    private void setOnClickListeners() {
        editText_eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateSelection(v);
            }
        });
        editText_eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeSelection(v);
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

    //region Dialogs
    public void onTimeSelection(View v) {
        if (!editText_eventTime.getText().toString().isEmpty()) {
            int hour, minute;
            String time = editText_eventTime.getText().toString();
            hour = TimeContainer.getHourFromText(time);
            minute = TimeContainer.getMinuteFromText(time);
            new TimeSelectionDialog(this.context, hour, minute, this);
        } else {
            new TimeSelectionDialog(this.context, this);
        }
    }

    public void onDateSelection(View v) {
        if (!editText_eventDate.getText().toString().isEmpty()) {
            int year, month, day;
            String date = editText_eventDate.getText().toString();
            year = DateContainer.getYearFromText(date);
            month = DateContainer.getMonthFromText(date);
            day = DateContainer.getDayFromText(date);
            new DateSelectionDialog(this.context, year, month - 1, day, this); //month starts at 0 for some reason
        } else {
            new DateSelectionDialog(this.context, this);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = DateContainer.formatDate(year, month + 1, dayOfMonth); //month starts at 0 for some reason
        editText_eventDate.setText(date);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time = TimeContainer.formatTime(hourOfDay, minute);
        editText_eventTime.setText(time);
    }
    //endregion

    //region On Click Events

    /**
     * updates event with new data, keeps participants and chat
     *
     * @return
     */
    public EventData collectEventData() {
        if (editText_eventDesc.getText().toString().isEmpty() ||
                editText_eventName.getText().toString().isEmpty() ||
                editText_eventMaxParticipants.getText().toString().isEmpty() ||
                editText_eventMinAge.getText().toString().isEmpty() ||
                editText_eventTime.getText().toString().isEmpty() ||
                editText_eventDate.getText().toString().isEmpty()) {
            return null;
        }
        fetchEventData(); //get latest event data to ensure participant list and chat are up to date
        eventData.setEventDescription(editText_eventDesc.getText().toString());
        eventData.setEventName(editText_eventName.getText().toString());
        eventData.setMaxParticipants(Integer.parseInt(editText_eventMaxParticipants.getText().toString()));
        eventData.setMinParticipantAge(Integer.parseInt(editText_eventMinAge.getText().toString()));
        eventData.setEventTime(TimeContainer.getContainerFromText(editText_eventTime.getText().toString()));
        eventData.setEventDate(DateContainer.getContainerFromText(editText_eventDate.getText().toString()));
        eventData.setEventType(getResources().getStringArray(R.array.eventType)[spinner_eventType.getSelectedItemPosition()]);
        return eventData;
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
            ((EventActivity) context).setButtonReferences(button_changeEventParticipationState, button_editEvent);
            updateButtonState();
            updateEventData(eventData);
        }
    }

    @Override
    public void updateEventData(EventData updatedEventData) {
        editText_eventName.setText(updatedEventData.getEventName());
        editText_eventDesc.setText(updatedEventData.getEventDescription());
        editText_eventMinAge.setText(String.valueOf(updatedEventData.getMinParticipantAge()));
        editText_eventDate.setText(updatedEventData.getEventDate().getDateAsString());
        editText_eventTime.setText(updatedEventData.getEventTime().getTimeAsString());
        editText_eventMaxParticipants.setText(String.valueOf(updatedEventData.getMaxParticipants()));
        spinner_eventType.setSelection(ArrayUtils.indexOf(getResources().getStringArray(R.array.eventType), updatedEventData.getEventType()));
    }
    //endregion


}
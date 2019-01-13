package de.ur.mi.android.teamseeker.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;


import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;

import de.ur.mi.android.teamseeker.helpers.DateSelectionDialog;
import de.ur.mi.android.teamseeker.MapsActivity;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.filter.Filter;
import de.ur.mi.android.teamseeker.helpers.DateContainer;

public class EventFilterFragment extends android.support.v4.app.Fragment {

    // Layout elements fragment_filter.xml
    private EditText editText_eventName, editText_date;
    private Button button_applyFilter;
    private Spinner spinner_distance;
    private Spinner spinner_eventType;

    //Getting context
    private Context context;

    //Variables for caching
    private double spinnerDistanceValue;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        getViews(view);
        setupEventTypeSpinner();
        setupDateSpinner();
        setupFilterButton();
        setupDateOnClickListener();
        return view;
    }

    /**
     * getViews() - setting up views, initiating components of fragment
     *
     * @param view
     */
    public void getViews(View view) {
        editText_eventName = view.findViewById(R.id.editText_eventName_filter);
        spinner_eventType = view.findViewById(R.id.spinner_eventType_filter);
        spinner_distance = view.findViewById(R.id.spinner_distance_filter);
        editText_date = view.findViewById(R.id.editText_date_filter);
        button_applyFilter = view.findViewById(R.id.button_apply_filter);
    }
    private void setupFilterButton(){
        button_applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter();
                ((MapsActivity) getActivity()).toggleFilterWindow();
            }
        });
    }

    /**
     * setupEventTypeSpinner() - method for setting adapter
     */
    public void setupEventTypeSpinner() {
        ArrayList<String> spinnerItems = ArrayUtils.toArrayList(getResources().getStringArray(R.array.eventType));
        spinnerItems.add(0, getString(R.string.spinner_alltypes));
        ArrayAdapter<String> adapter_type = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, spinnerItems);
        spinner_eventType.setAdapter(adapter_type);
    }

    /**
     * setupDateSpinner() - method for setting adapter
     */
    public void setupDateSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.distances, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_distance.setAdapter(adapter);
        spinner_distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        spinnerDistanceValue = Double.MAX_VALUE;
                        break;
                    case 1:
                        spinnerDistanceValue = 10000.0;
                        break;
                    case 2:
                        spinnerDistanceValue = 20000.0;
                        break;
                    case 3:
                        spinnerDistanceValue = 50000.0;
                        break;
                    case 4:
                        spinnerDistanceValue = 100000.0;
                        break;
                    case 5:
                        spinnerDistanceValue = 200000.0;
                        break;
                    case 6:
                        spinnerDistanceValue = 300000.0;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * applyFilter() - creating a Filter object with parameters set inside the fragment
     */
    public void applyFilter() {
        Filter filter = Filter.empty();

        //EventName Search
        if (!editText_eventName.getText().toString().isEmpty()) {
            filter.addFilter(Filter.FILTER_NAME, editText_eventName.getText().toString());
        }
        //Spinner for event type
        if (spinner_eventType.getSelectedItemPosition() > 0) {
            filter.addFilter(Filter.FILTER_TYPE, spinner_eventType.getSelectedItem().toString());
        }
        //Spinner for distance
        if (spinner_distance.getSelectedItemPosition() > 0) {
            filter.addFilter(Filter.FILTER_MAXRADIUS, spinnerDistanceValue);
        }
        //EditText for Date
        if (!editText_date.getText().toString().isEmpty()) {
            filter.addFilter(Filter.FILTER_DATE, editText_date.getText().toString());
        }

        if(filter.isEmpty()){
            filter = Filter.all();
        }

        ((MapsActivity) getActivity()).searchEventsByFilter(filter);

    }

    /**
     * setupDateOnClickListener() - Creating proper EditText for "Date"
     */
    public void setupDateOnClickListener() {
        editText_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText_date.getText().toString().isEmpty()) {
                    int year, month, day;
                    String date = editText_date.getText().toString();
                    year = DateContainer.getYearFromText(date);
                    month = DateContainer.getMonthFromText(date);
                    day = DateContainer.getDayFromText(date);
                    new DateSelectionDialog(context, year, month - 1, day, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String date = DateContainer.formatDate(year, month + 1, dayOfMonth); //month starts at 0 for some reason
                            editText_date.setText(date);
                        }
                    }); //month starts at 0 for some reason
                } else {
                    new DateSelectionDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String date = DateContainer.formatDate(year, month + 1, dayOfMonth); //month starts at 0 for some reason
                            editText_date.setText(date);
                        }
                    });
                }
            }
        });
    }


}

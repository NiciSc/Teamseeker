package de.ur.mi.android.teamseeker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import org.joda.time.DateTime;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

public class TimeSelectionDialog {
    public TimeSelectionDialog(Context context, TimePickerDialog.OnTimeSetListener onTimeSetListener){
        DateTime dateTime = new DateTime();
        int hour = dateTime.getHourOfDay();
        int minute = dateTime.getMinuteOfHour();
        showDialog(context, onTimeSetListener, hour, minute);
    }
    public TimeSelectionDialog(Context context, int hour, int minute, TimePickerDialog.OnTimeSetListener onTimeSetListener){
        showDialog(context, onTimeSetListener, hour, minute);
    }
    private void showDialog(Context context, TimePickerDialog.OnTimeSetListener onDateSetListener, int hour, int minute) {
        TimePickerDialog tpd = new TimePickerDialog(context, onDateSetListener, hour, minute, true);
        tpd.show();
    }
}

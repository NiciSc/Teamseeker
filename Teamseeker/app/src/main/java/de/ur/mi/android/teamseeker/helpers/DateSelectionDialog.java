package de.ur.mi.android.teamseeker.helpers;

import android.app.DatePickerDialog;
import android.content.Context;

import java.util.Calendar;

public class DateSelectionDialog{

    /**
     * https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     * https://developer.android.com/reference/android/app/DatePickerDialog
     */
    public DateSelectionDialog(Context context, DatePickerDialog.OnDateSetListener onDateSetListener){
        Calendar calendar = Calendar.getInstance();
        int dialogYear = calendar.get(Calendar.YEAR);
        int dialogMonth = calendar.get(Calendar.MONTH);
        int dialogDay = calendar.get(Calendar.DAY_OF_MONTH);
        showDialog(context, onDateSetListener, dialogYear, dialogMonth, dialogDay);
    }
    public DateSelectionDialog(Context context, int year, int month, int day, DatePickerDialog.OnDateSetListener onDateSetListener){
        showDialog(context, onDateSetListener, year, month, day);
    }
    /**
     * https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
     * https://developer.android.com/reference/android/app/DatePickerDialog
     */
    private void showDialog(Context context, DatePickerDialog.OnDateSetListener onDateSetListener, int year, int month, int day){
        DatePickerDialog dpd = new DatePickerDialog(context, onDateSetListener, year, month, day);
        dpd.show();
    }
}

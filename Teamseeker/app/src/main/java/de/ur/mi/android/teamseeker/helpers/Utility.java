package de.ur.mi.android.teamseeker.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.util.ArrayUtils;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import de.ur.mi.android.teamseeker.R;

public final class Utility {

    //region enum
    public static <E extends Enum<E>> E stringToEnum(Class<E> enumType, String enumName) {
        return Enum.valueOf(enumType, enumName);
    }

    public static String enumToString(Enum<?> enumFieldName) {
        return enumFieldName.name();
    }

    public static List<String> enumToStringList(Class<? extends Enum<?>> e) {
        ArrayList<String> results = new ArrayList<>();
        for (Object o : e.getEnumConstants()) {
            results.add(o.toString());
        }
        return results;
    }
    //endregion

    /**
     * Code retrieved from following urls, using joda time
     * https://stackoverflow.com/questions/6252678/converting-a-date-string-to-a-datetime-object-using-joda-time-library
     * https://www.baeldung.com/java-get-age
     *
     * @param birthDate
     * @return
     */
    public static int calculateAge(String birthDate) {
        DateTime birthDay = getDateTimeFromString(birthDate);
        DateTime currentDay = new DateTime(); //gets date at moment of creation
        int age = Years.yearsBetween(birthDay, currentDay).getYears();
        return age;
    }

    public static DateTime getDateTimeFromString(String date) {
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
        DateTime ret = timeFormatter.parseDateTime(date);
        return ret;
    }


    public static void createAlertDialog(Context context, boolean cancelable, String message,
                                         String confirmButtonText,
                                         final @Nullable DialogInterface.OnClickListener onConfirmClickListener,
                                         String denyButtonText,
                                         final @Nullable DialogInterface.OnClickListener onDenyClickListener,
                                         String neutralButtonText,
                                         final @Nullable DialogInterface.OnClickListener onNeutralClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(cancelable);
        if (confirmButtonText != null) {
            builder.setPositiveButton(confirmButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onConfirmClickListener != null) {
                        onConfirmClickListener.onClick(dialog, which);
                    }
                    dialog.cancel();
                }
            });
        }
        if (denyButtonText != null) {
            builder.setNegativeButton(denyButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onDenyClickListener != null) {
                        onDenyClickListener.onClick(dialog, which);
                    }
                    dialog.cancel();
                }
            });
        }
        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (onNeutralClickListener != null) {
                        onNeutralClickListener.onClick(dialog, which);
                    }
                    dialog.cancel();
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static int getMatches(String s1, String s2) {
        String longer = s1.length() > s2.length() ? s1 : s2;
        String shorter = s1.length() > s2.length() ? s2 : s1;

        Character[] charArray = ArrayUtils.toWrapperArray(longer.toCharArray());
        ArrayList<Character> charList = ArrayUtils.toArrayList(charArray);

        Character[] searchCharArray = ArrayUtils.toWrapperArray(shorter.toCharArray());
        ArrayList<Character> searchCharList = ArrayUtils.toArrayList(searchCharArray);

        charList.removeAll(searchCharList);
        return longer.length() - charList.size();
    }
}

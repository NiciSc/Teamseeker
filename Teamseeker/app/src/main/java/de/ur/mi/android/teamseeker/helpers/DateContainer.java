package de.ur.mi.android.teamseeker.helpers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.joda.time.DateTime;

public class DateContainer implements Parcelable {
    private int year;
    private int month;
    private int day;

    public DateContainer() {
        //empty constructor needed for firebase database
    }

    public DateContainer(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DateContainer)) {
            return false;
        } else {
            DateContainer compDate = (DateContainer) obj;
            return compDate.getDateAsString().equals(getDateAsString());
        }
    }

    //region Setters
    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }
    //endregion

    //region Getters
    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
    //endregion

    //region static utility methods
    public static String formatDate(int year, int month, int day) {
        String monthText = String.valueOf(month); //month starts at 0 for some reason
        String dayText = String.valueOf(day);
        if (month < 10) {
            monthText = "0" + monthText;
        }
        if (day < 10) {
            dayText = "0" + dayText;
        }
        return dayText + "." + monthText + "." + year;
    }

    public static DateContainer getCurrentDate() {
        DateTime dateTime = new DateTime();
        return new DateContainer(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    public static int getYearFromText(String date) {
        String year = date.split("\\.")[2];
        return Integer.parseInt(year);
    }

    public static int getMonthFromText(String date) {
        String month = date.split("\\.")[1];
        return Integer.parseInt(month);
    }

    public static int getDayFromText(String date) {
        String day = date.split("\\.")[0];
        return Integer.parseInt(day);
    }

    public static DateContainer getContainerFromText(String date) {
        return new DateContainer(getYearFromText(date), getMonthFromText(date), getDayFromText(date));
    }
    //endregion

    //region utility methods
    public String getDateAsString() {
        String monthText = String.valueOf(month);
        String dayText = String.valueOf(day);
        if (month < 10) {
            monthText = "0" + monthText;
        }
        if (day < 10) {
            dayText = "0" + dayText;
        }
        return dayText + "." + monthText + "." + year;
    }
    //endregion

    //region Parcelable implementation
    protected DateContainer(Parcel in) {
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
    }
    public static final Creator<DateContainer> CREATOR = new Creator<DateContainer>() {
        @Override
        public DateContainer createFromParcel(Parcel in) {
            return new DateContainer(in);
        }

        @Override
        public DateContainer[] newArray(int size) {
            return new DateContainer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
    }
    //endregion
}

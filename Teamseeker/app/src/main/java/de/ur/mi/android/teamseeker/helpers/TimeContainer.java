package de.ur.mi.android.teamseeker.helpers;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

public class TimeContainer implements Parcelable {
    private int hour;
    private int minute;
    private int second;
    private int millisecond;

    public TimeContainer() {
        //empty constructor needed for firebase database
    }

    public TimeContainer(int hour, int minute, int second, int millisecond) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeContainer)) {
            return false;
        } else {
            TimeContainer compTime = (TimeContainer) obj;
            return compTime.getHour() == hour &&
                    compTime.getMinute() == minute &&
                    compTime.getSecond() == second &&
                    compTime.getMillisecond() == millisecond;
        }
    }

    //region Getters

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public int getMillisecond() {
        return millisecond;
    }

    //endregion

    //region Setters

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }

    //endregion

    //region static utility methods
    public static String formatTime(int hour, int minute) {
        String hourText = String.valueOf(hour);
        String minuteText = String.valueOf(minute);
        if (hour < 10) {
            hourText = "0" + hourText;
        }
        if (minute < 10) {
            minuteText = "0" + minuteText;
        }
        return hourText + ":" + minuteText;
    }

    public static TimeContainer getCurrentTime() {
        DateTime dateTime = new DateTime();
        return new TimeContainer(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute(), dateTime.getMillisOfSecond());
    }

    public static int getHourFromText(String time) {
        String hour = time.split(":")[0];
        return Integer.parseInt(hour);
    }

    public static int getMinuteFromText(String time) {
        String minute = time.split(":")[1];
        return Integer.parseInt(minute);
    }

    public static TimeContainer getContainerFromText(String time) {
        return new TimeContainer(getHourFromText(time), getMinuteFromText(time), 0, 0);
    }

    //endregion

    //region utility methods
    public String getTimeAsString() {
        String hourText = String.valueOf(hour);
        String minuteText = String.valueOf(minute);
        if (hour < 10) {
            hourText = "0" + hourText;
        }
        if (minute < 10) {
            minuteText = "0" + minuteText;
        }
        return hourText + ":" + minuteText;
    }
    public String getTimeAsStringFull() {
        String hourText = String.valueOf(hour);
        String minuteText = String.valueOf(minute);
        String secondText = String.valueOf(second);
        String millisText = String.valueOf(millisecond);
        if (hour < 10) {
            hourText = "0" + hourText;
        }
        if (minute < 10) {
            minuteText = "0" + minuteText;
        }
        if(second < 10) {
            secondText = "0" + secondText;
        }
        if(millisecond <10) {
            millisText = "0" + millisText;
        }
        return hourText + ":" + minuteText + ":" +  secondText + ":" + millisText;
    }
    //endregion

    //region parcelable implementation
    protected TimeContainer(Parcel in) {
        hour = in.readInt();
        minute = in.readInt();
        second = in.readInt();
        millisecond = in.readInt();
    }

    public static final Creator<TimeContainer> CREATOR = new Creator<TimeContainer>() {
        @Override
        public TimeContainer createFromParcel(Parcel in) {
            return new TimeContainer(in);
        }

        @Override
        public TimeContainer[] newArray(int size) {
            return new TimeContainer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hour);
        dest.writeInt(minute);
        dest.writeInt(second);
        dest.writeInt(millisecond);
    }
    //endregion
}

package de.ur.mi.android.teamseeker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

import de.ur.mi.android.teamseeker.helpers.DateContainer;
import de.ur.mi.android.teamseeker.helpers.TimeContainer;

public class ChatMessage implements Parcelable, Comparable {
    private DateContainer date;
    private TimeContainer time;
    private String userName;
    private String message;
    private String userID;
    private ArrayList<String> readBy = new ArrayList<>();



    public class ChatMessageComparator implements Comparator<ChatMessage>{
        @Override
        public int compare(ChatMessage o1, ChatMessage o2) {
            return o1.compareTo(o2);
        }
    }

    public ChatMessage() {
        //empty constructor needed for firebase database
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int compareTime = Integer.parseInt(((ChatMessage)o).getTime().getTimeAsString().replace(":", ""));
        int thisTIme = Integer.parseInt(getTime().getTimeAsStringFull().replace(":", ""));
        return thisTIme - compareTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChatMessage)) {
            return false;
        } else {
            ChatMessage compMessage = (ChatMessage) obj;
            return compMessage.getMessage().equals(message) &&
                    compMessage.getUserID().equals(userID) &&
                    compMessage.getTime().equals(time);

        }
    }

    //region getters
    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public DateContainer getDate() {
        return date;
    }

    public TimeContainer getTime() {
        return time;
    }

    public ArrayList<String> getReadBy() {
        return readBy;
    }

    //endregion

    //region setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(DateContainer date) {
        this.date = date;
    }

    public void setTime(TimeContainer time) {
        this.time = time;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setReadBy(ArrayList<String> readBy) {
        this.readBy = readBy;
    }

    //endregion

    //region parcelable implementation
    protected ChatMessage(Parcel in) {
        date = in.readParcelable(DateContainer.class.getClassLoader());
        time = in.readParcelable(TimeContainer.class.getClassLoader());
        userName = in.readString();
        userID = in.readString();
        message = in.readString();
        in.readStringList(readBy);
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(date, flags);
        dest.writeParcelable(time, flags);
        dest.writeString(userName);
        dest.writeString(userID);
        dest.writeString(message);
        dest.writeStringList(readBy);
    }
    //endregion

    //region utility
    public void addReadBy(String userID) {
        readBy.add(userID);
    }
    //endregion
}

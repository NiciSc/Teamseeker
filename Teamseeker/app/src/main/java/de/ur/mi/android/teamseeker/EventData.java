package de.ur.mi.android.teamseeker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.ur.mi.android.teamseeker.helpers.DateContainer;
import de.ur.mi.android.teamseeker.helpers.TimeContainer;

public class EventData implements Parcelable {
    /**
     * These strings are static in this class instead of strings.xml because they need to match the variable names
     * The strings are used to identify online database field paths
     * They are easier to maintain here than in strings.xml
     */
    //region static strings
    public static final String EVENTID_KEY = "eventID";
    public static final String EVENTNAME_KEY = "eventName";
    public static final String EVENTDESCRIPTION_KEY = "eventDescription";
    public static final String EVENTTIMEMINUTE_KEY = "minute"; //variable name from TimeContainer
    public static final String EVENTTIMEHOUR_KEY = "hour"; //variable name from TimeContainer
    public static final String EVENTDATEYEAR_KEY = "eventDate.year"; //variable name from DateContainer
    public static final String EVENTDATEMONTH_KEY = "eventDate.month"; //variable name from DateContainer
    public static final String EVENTDATEDAY_KEY = "eventDate.day"; //variable name from DateContainer
    public static final String EVENTDATE_KEY = "eventDate.dateAsString"; //variable name from DateContainer
    public static final String EVENTLOCATION_KEY = "eventLocation";
    public static final String EVENTTYPE_KEY = "eventType";
    public static final String MAXPARTICIPANTS_KEY = "maxParticipants";
    public static final String MINAGE_KEY = "minParticipantAge";
    public static final String PARTICIPANTS_KEY = "participants";
    public static final String CHATMESSAGES_KEY = "chatMessages";
    //endregion

    private String eventID, eventType,eventName,eventDescription;
    private TimeContainer eventTime;
    private DateContainer eventDate;
    private double eventLatitude, eventLongitude;
    private int maxParticipants, minParticipantAge;
    private ArrayList<String> participants = new ArrayList<>();
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();


    /**
     * Do not use mepty constructor, provide an ID
     */
    public EventData() {
        //Empty Constructor needed for de.ur.mi.android.teamseeker.DatabaseManager to deserialize object
    }

    /**
     * @param eventID same as user id (ties host to event)
     */
    public EventData(String eventID) {
        this.eventID = eventID;
        participants.add(eventID);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EventData && ((EventData) obj).getEventID().equals(eventID);
    }

    //region event data management

    /**
     * attempts to remove host and reassign
     */
    public boolean attemptSetHost(String newHostID) {
        if (participants.contains(newHostID)) {
            eventID = newHostID;
            return true;
        } else {
            return false;
        }
    }

    public void userJoined(String userID) {
        participants.add(userID);
    }

    public void userLeft(String userID) {
        participants.remove(userID);
    }

    public void addChatMessage(ChatMessage chatMessage){
        chatMessages.add(chatMessage);
    }
    //endregion

    //region getters
    public String getEventDescription() {
        return eventDescription;
    }

    public DateContainer getEventDate() {
        return eventDate;
    }

    public TimeContainer getEventTime() {
        return eventTime;
    }

    public String getEventID() {
        return eventID;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public double getEventLatitude() {
        return eventLatitude;
    }

    public double getEventLongitude() {
        return eventLongitude;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getMinParticipantAge() {
        return minParticipantAge;
    }

    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    //endregion

    //region setters
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventTime(TimeContainer eventTime) {
        this.eventTime = eventTime;
    }

    public void setEventDate(DateContainer eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEventLatitude(double eventLatitude) {
        this.eventLatitude = eventLatitude;
    }

    public void setEventLongitude(double eventLongitude) {
        this.eventLongitude = eventLongitude;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setMinParticipantAge(int minParticipantAge) {
        this.minParticipantAge = minParticipantAge;
    }

    public void setChatMessages(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    //endregion

    //region utility
    public boolean hasNewMessage() {
        boolean hasNew = false;
        for (ChatMessage chatMessage : chatMessages) {
            if (!chatMessage.getReadBy().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                hasNew = true;
            }
        }
        return hasNew;
    }
    public boolean hasParticipant(String userID){
        return participants.contains(userID);
    }

    //endregion

    //region parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    protected EventData(Parcel in) {
        eventID = in.readString();
        eventName = in.readString();
        eventDescription = in.readString();
        eventType = in.readString();
        eventLatitude = in.readDouble();
        eventLongitude = in.readDouble();
        maxParticipants = in.readInt();
        minParticipantAge = in.readInt();
        eventTime = in.readParcelable(TimeContainer.class.getClassLoader());
        eventDate = in.readParcelable(DateContainer.class.getClassLoader());
        participants = in.createStringArrayList();
        in.readList(chatMessages, ChatMessage.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventID);
        dest.writeString(eventName);
        dest.writeString(eventDescription);
        dest.writeString(eventType);
        dest.writeDouble(eventLatitude);
        dest.writeDouble(eventLongitude);
        dest.writeInt(maxParticipants);
        dest.writeInt(minParticipantAge);
        dest.writeParcelable(eventTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(eventDate, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeStringList(participants);
        dest.writeList(chatMessages);
    }

    public static final Creator<EventData> CREATOR = new Creator<EventData>() {
        @Override
        public EventData createFromParcel(Parcel in) {
            return new EventData(in);
        }

        @Override
        public EventData[] newArray(int size) {
            return new EventData[size];
        }
    };
    //endregion
}

package de.ur.mi.android.teamseeker;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable{
    /**
     * These strings are static in this class instead of strings.xml because they need to match the variable names
     * The strings are used to identify online database field paths
     * They are easier to maintain here than in strings.xml
     */
    //region static strings
    public static final String USERID_KEY = "userID";
    public static final String USERNAME_KEY = "username";
    public static final String USERBIRTHDATE_KEY = "birthDate";
    public static final String USERGENDER_KEY = "gender";
    public static final String USERFIRSTNAME_KEY = "firstName";
    public static final String USERLASTNAME_KEY = "lastName";
    //endregion

    private String userID;
    private String username;
    private String birthDate;
    private String gender;
    private String firstName;
    private String lastName;

    public UserData(){
        //Empty Constructor needed for de.ur.mi.android.teamseeker.DatabaseManager to deserialize object
    }
    public UserData(String userID){
        this.userID = userID;
    }


    protected UserData(Parcel in) {
        userID = in.readString();
        username = in.readString();
        birthDate = in.readString();
        gender = in.readString();
        firstName = in.readString();
        lastName = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserData && ((UserData)obj).getUserID().equals(getUserID());
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    //Getters-------------------------------
    public String getBirthDate() {
        return birthDate;
    }
    public String getUserID() {
        return userID;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getUsername() {
        return username;
    }
    public String getGender() {
        return gender;
    }
    //Setters--------------------------------
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(username);
        dest.writeString(birthDate);
        dest.writeString(gender);
        dest.writeString(firstName);
        dest.writeString(lastName);
    }
}

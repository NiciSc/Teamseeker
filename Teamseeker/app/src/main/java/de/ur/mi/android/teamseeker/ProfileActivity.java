package de.ur.mi.android.teamseeker;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.helpers.DateContainer;
import de.ur.mi.android.teamseeker.helpers.DateSelectionDialog;
import de.ur.mi.android.teamseeker.helpers.OverlayActivity;
import de.ur.mi.android.teamseeker.helpers.Utility;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;
import de.ur.mi.android.teamseeker.interfaces.OnDataUploadCompleteListener;

public class ProfileActivity extends OverlayActivity implements DatePickerDialog.OnDateSetListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CROP_IMAGE_REQUEST = 3;
    private static final String PROFILE_PICTURE_PATH_KEY = "imagepath";
    public static final String PROFILE_PICTURE_PATH = "/pictures/profile";

    private EditText editText_date, editText_username, editText_firstName, editText_lastName;
    private FloatingActionButton floatingActionButton_save;
    private Spinner spinner_gender;
    private ImageView imageView_profilePic;
    private UserData userData;
    private EventData returnIntentData = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(getString(R.string.intent_key_firstsetup))) {
            super.setContentViewNoOverlay(R.layout.activity_profile);
        } else {
            super.setContentView(R.layout.activity_profile);
        }

        userData = processIntent();
        setupViews();
        handleEditableState();
        attemptDataFill();
    }

    @Override
    public void onBackPressed() {
        if (returnIntentData != null) {
            Intent returnIntent = new Intent(this, EventActivity.class);
            returnIntent.putExtra(getString(R.string.event_intent_key), returnIntentData);
            startActivity(returnIntent);
        } else {
            switchActivity(MapsActivity.class);
        }
    }

    @Override
    protected void onOverlayReady() {
        hideToolbarItem(R.id.action_filter);
    }

    private UserData processIntent() {
        Intent intent = getIntent();
        String extraKey = getString(R.string.user_intent_key);
        String returnKey = getString(R.string.return_intent_key);
        if (intent.hasExtra(returnKey)) {
            returnIntentData = intent.getParcelableExtra(returnKey);
        }
        if (intent.hasExtra(extraKey)) {
            return intent.getParcelableExtra(extraKey);
        } else {
            return null;
        }
    }

    //region utility
    private void handleEditableState() {
        if (userData != null && !userData.getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            editText_firstName.setFocusable(false);
            editText_firstName.setClickable(false);
            editText_firstName.setBackground(null);

            editText_lastName.setFocusable(false);
            editText_lastName.setClickable(false);
            editText_lastName.setBackground(null);

            imageView_profilePic.setFocusable(false);
            imageView_profilePic.setClickable(false);
            imageView_profilePic.setBackground(null);

            editText_date.setFocusable(false);
            editText_date.setClickable(false);
            editText_date.setBackground(null);

            editText_username.setFocusable(false);
            editText_username.setClickable(false);
            editText_username.setBackground(null);

            spinner_gender.setEnabled(false);

        } else if (!getIntent().hasExtra(getString(R.string.intent_key_firstsetup))) {
            //Birthdate and gender edit only allowed at first startup

            spinner_gender.setEnabled(false);

            editText_date.setFocusable(false);
            editText_date.setClickable(false);
            editText_date.setBackground(null);
            floatingActionButton_save.setVisibility(View.VISIBLE);

        } else {
            floatingActionButton_save.setVisibility(View.VISIBLE);
            setupDateOnClickListener();
        }

    }

    private void checkIfUserNameExists(String userName, final OnDataDownloadCompleteListener<UserData> onDataDownloadCompleteListener) {
        DatabaseManager.getData(UserData.class, DatabaseManager.DB_KEY_USER, UserData.USERNAME_KEY, userName, new OnDataDownloadCompleteListener<UserData>() {
            @Override
            public void onDataDownloadComplete(List<UserData> data, int resultCode) {
                if (resultCode == RESULT_OK && data != null && data.size() > 0) {
                    UserData userData = data.get(0);
                    if (userData.getUserID().equals(ProfileActivity.this.userData.getUserID())) {
                        onDataDownloadCompleteListener.onDataDownloadComplete(data, RESULT_CANCELED);
                    } else {
                        onDataDownloadCompleteListener.onDataDownloadComplete(data, RESULT_OK);
                    }
                } else {
                    onDataDownloadCompleteListener.onDataDownloadComplete(null, RESULT_CANCELED);
                }
            }
        });
    }

    private void saveProfileData(final UserData userData) {
        saveInfoLocally(userData);
        final String databaseKey = DatabaseManager.DB_KEY_USER;
        DatabaseManager.documentExists(databaseKey, UserData.USERID_KEY, userData.getUserID(), new OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
                switch (resultCode) {
                    case DatabaseManager.DOCUMENT_EXISTS:
                        DatabaseManager.updateData(databaseKey, UserData.USERID_KEY, userData.getUserID(), userData, new OnCompleteListener() {
                            @Override
                            public void onComplete(int resultCode) {
                                if (resultCode == RESULT_OK) {
                                    Toast.makeText(ProfileActivity.this, R.string.toast_profile_saved, Toast.LENGTH_SHORT).show();
                                    ProfileActivity.this.switchActivity(MapsActivity.class);
                                }
                            }
                        });
                        break;
                    case DatabaseManager.DOCUMENT_EXISTS_NOT:
                        DatabaseManager.addData(userData, DatabaseManager.DB_KEY_USER, new OnDataUploadCompleteListener<UserData>() {
                            @Override
                            public void onDataUploadComplete(UserData userData, int resultCode) {
                                if (resultCode == RESULT_OK) {
                                    Toast.makeText(ProfileActivity.this, R.string.toast_profile_saved, Toast.LENGTH_SHORT).show();
                                    ProfileActivity.this.switchActivity(MapsActivity.class);
                                }
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private boolean checkRequiredFields(UserData data) {
        return !data.getBirthDate().isEmpty() &&
                !data.getUsername().isEmpty() &&
                !data.getGender().isEmpty();
    }

    private UserData collectData() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserData userData = new UserData(userID);
        userData.setBirthDate(editText_date.getText().toString());
        userData.setGender(spinner_gender.getSelectedItem().toString());
        userData.setUsername(editText_username.getText().toString());
        userData.setFirstName(editText_firstName.getText().toString());
        userData.setLastName(editText_lastName.getText().toString());
        return userData;
    }
    //endregion

    //region preferences save/load

    /**
     * https://gist.github.com/vxhviet/07429133e71b5fec2e39bd60171184cd
     * External write permission not needed for getFilesDir() according to android doc
     *
     * @return returns the file path
     */
    private void saveInfoLocally(UserData userData) {
        String userID = userData.getUserID();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).edit();
        editor.putString(UserData.USERNAME_KEY + userID, userData.getUsername());
        editor.putString(UserData.USERFIRSTNAME_KEY + userID, userData.getFirstName());
        editor.putString(UserData.USERLASTNAME_KEY + userID, userData.getLastName());
        editor.putString(UserData.USERGENDER_KEY + userID, userData.getGender());
        editor.putString(UserData.USERBIRTHDATE_KEY + userID, userData.getBirthDate());
        editor.apply();
    }

    private UserData getUserDataFromPreferences() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        if (pref.getString(UserData.USERNAME_KEY + userID, null) == null) {
            return null;
        }
        UserData userData = new UserData();
        userData.setUserID(userID);
        userData.setUsername(pref.getString(UserData.USERNAME_KEY + userID, null));
        userData.setFirstName(pref.getString(UserData.USERFIRSTNAME_KEY + userID, null));
        userData.setLastName(pref.getString(UserData.USERLASTNAME_KEY + userID, null));
        userData.setBirthDate(pref.getString(UserData.USERBIRTHDATE_KEY + userID, null));
        userData.setGender(pref.getString(UserData.USERGENDER_KEY + userID, null));

        return userData;
    }
    //endregion

    //region views
    private void attemptDataFill() {
        if (userData != null) {
            fillViews();
            attemptSetProfilePicture(userData);
        } else {
            userData = getUserDataFromPreferences();
            if (userData != null) {
                fillViews();
                attemptSetProfilePicture(userData);
            } else {
                final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseManager.getData(UserData.class, DatabaseManager.DB_KEY_USER, UserData.USERID_KEY, userID, new OnDataDownloadCompleteListener<UserData>() {
                    @Override
                    public void onDataDownloadComplete(List<UserData> userDataList, int resultCode) {
                        if (resultCode == RESULT_OK && userDataList != null && userDataList.size() > 0) {
                            userData = userDataList.get(0);
                            saveInfoLocally(userData);
                            DatabaseManager.downloadImageFromStorage(userID, new OnDataDownloadCompleteListener<Bitmap>() {
                                @Override
                                public void onDataDownloadComplete(List<Bitmap> data, int resultCode) {
                                    if (resultCode == RESULT_OK && data != null && data.size() > 0) {
                                        Bitmap downloadedPicture = data.get(0);
                                        saveImageLocally(downloadedPicture);
                                    }
                                }
                            });
                            fillViews();
                            attemptSetProfilePicture(userData);
                        } else {
                            userData = new UserData();
                            userData.setUserID(userID);
                            attemptSetProfilePicture(userData);
                        }
                    }
                });
            }
        }
    }

    private void fillViews() {
        editText_date.setText(userData.getBirthDate());
        editText_username.setText(userData.getUsername());
        spinner_gender.setSelection(ArrayUtils.indexOf(getResources().getStringArray(R.array.gender), userData.getGender()));
        editText_firstName.setText(userData.getFirstName());
        editText_lastName.setText(userData.getLastName());
    }


    private void setupViews() {
        String[] genders = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, genders);
        spinner_gender = findViewById(R.id.spinner_gender);
        spinner_gender.setAdapter(spinnerAdapter);
        spinner_gender.setSelection(1);

        editText_date = findViewById(R.id.editText_date);
        editText_username = findViewById(R.id.editText_username);
        editText_firstName = findViewById(R.id.editText_firstName);
        editText_lastName = findViewById(R.id.editText_lastName);
        imageView_profilePic = findViewById(R.id.imageView_profilePic);
        floatingActionButton_save = findViewById(R.id.button_save);
    }
    //endregion

    //region onclicks
    public void onSelectionConfirmed(View v) {
        final UserData userData = collectData();
        if (!checkRequiredFields(userData)) {
            Toast.makeText(ProfileActivity.this, R.string.error_notpopulated, Toast.LENGTH_SHORT).show();
        } else if (userData.getUsername().length() > getResources().getInteger(R.integer.username_length_max)) {
            Toast.makeText(ProfileActivity.this, R.string.error_namesize, Toast.LENGTH_SHORT).show();
        } else {
            checkIfUserNameExists(userData.getUsername(), new OnDataDownloadCompleteListener<UserData>() {
                @Override
                public void onDataDownloadComplete(List data, int resultCode) {
                    if (resultCode == RESULT_CANCELED) {
                        if (ProfileActivity.this.userData != null && ProfileActivity.this.userData.getBirthDate() == null) {
                            Utility.createAlertDialog(ProfileActivity.this, true, getString(R.string.warning_nochange), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveProfileData(userData);
                                }
                            }, null, null, getString(R.string.edit_profile), null);
                        } else {
                            saveProfileData(userData);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, R.string.error_username_taken, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void onProfileImageClick(View v) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE_REQUEST);
    }

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
                    new DateSelectionDialog(ProfileActivity.this, year, month - 1, day, ProfileActivity.this); //month starts at 0 for some reason
                } else {
                    new DateSelectionDialog(ProfileActivity.this, ProfileActivity.this);
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = DateContainer.formatDate(year, month + 1, dayOfMonth); //month starts at 0 for some reason
        editText_date.setText(date);
    }
    //endregion

    //region image management
    private Bitmap tryGetLocalImage() {
        File file = new File(this.getFilesDir(), userData.getUserID());
        if (!file.exists()) {
            return null;
        } else {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            return bmp;
        }
    }

    private String saveImageLocally(Bitmap bitmap) {
        String fullPath = this.getFilesDir().getAbsolutePath() + PROFILE_PICTURE_PATH;
        File file = new File(this.getFilesDir(), userData.getUserID());
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cropImage(Uri uri) {
        //call the standard crop action intent
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri of image
        cropIntent.setDataAndType(uri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, CROP_IMAGE_REQUEST);
    }

    private void attemptSetProfilePicture(UserData userData) {
        Bitmap bitmap = tryGetLocalImage();
        if (bitmap != null) {
            imageView_profilePic.setImageBitmap(bitmap);
        } else {
            imageView_profilePic.setImageDrawable(getResources().getDrawable(R.drawable.ic_face));
            DatabaseManager.downloadImageFromStorage(userData.getUserID(), new OnDataDownloadCompleteListener<Bitmap>() {
                @Override
                public void onDataDownloadComplete(List<Bitmap> data, int resultCode) {
                    if (resultCode == RESULT_OK) {
                        imageView_profilePic.setImageBitmap(data.get(0));
                    }
                }
            });
        }
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_IMAGE_REQUEST:
                Uri uri = data.getData();
                cropImage(uri);
                break;
            case CROP_IMAGE_REQUEST:
                Bitmap bitmap = data.getExtras().getParcelable("data");
                imageView_profilePic.setImageBitmap(bitmap);
                String filePath = saveImageLocally(bitmap);
                DatabaseManager.uploadImageToStorage(filePath, userData.getUserID(), new OnCompleteListener() {
                    @Override
                    public void onComplete(int resultCode) {
                        if (resultCode == RESULT_OK) {
                            Toast.makeText(ProfileActivity.this, R.string.toast_profilepicture_uploaded, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                break;
            default:
                break;
        }
    }
}

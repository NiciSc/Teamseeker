package de.ur.mi.android.teamseeker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;
import de.ur.mi.android.teamseeker.services.ChatService;
import de.ur.mi.android.teamseeker.services.ServiceManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextUsername, editTextPassword;

    private ServiceManager serviceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        initManagers();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (hasPreviousLoginSession()) {
            handleLoginProcedure();
        }
    }

    @Override
    public void onBackPressed() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        serviceManager.stopServiceInstance(ChatService.class);
        super.onDestroy();
    }

    private void collectViews() {
        editTextUsername = findViewById(R.id.editText_login_email);
        editTextPassword = findViewById(R.id.editText_login_userpassword);
    }
    //endregion

    //region On Click Events

    /**
     * Gets called when the login button gets pressed
     *
     * @param v
     */
    public void onLoginPressed(View v) {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        signInUser(username, password);
    }

    /**
     * Gets called when the register button gets pressed
     *
     * @param v
     */
    public void onSignupPressed(View v) {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        signUpNewUser(username, password);
    }
    //endregion

    //region setup managers
    private void initManagers() {
        DatabaseManager.initialize();
        if (serviceManager == null) {
            serviceManager = new ServiceManager(this);
        } else {
            serviceManager.stopServiceInstance(ChatService.class);
        }
    }
    //endregion

    //region persistence checks
    public void redirectLogin(View v) {
        setContentView(R.layout.activity_login);
        collectViews();
    }

    public void redirectSignup(View v) {
        setContentView(R.layout.activity_register);
        collectViews();
    }

    private void handleLoginProcedure() {
        checkUserFirstLogin(new de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener() {
            @Override
            public void onComplete(int resultCode) {
                Intent intent = null;
                switch (resultCode) {
                    case RESULT_FIRST_USER:
                        intent = new Intent(MainActivity.this, FirstSigninActivity.class);
                        break;
                    case RESULT_OK:
                        intent = new Intent(MainActivity.this, MapsActivity.class);
                        intent.putExtra(getString(R.string.app_started), true);
                        break;
                }
                startActivity(intent);
            }
        });
    }

    private boolean hasPreviousLoginSession() {
        return mAuth.getCurrentUser() != null;
    }

    private void checkUserFirstLogin(final de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener onCompleteListener) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (pref.contains(UserData.USERNAME_KEY + userID)) {
            onCompleteListener.onComplete(RESULT_OK);
        } else {
            DatabaseManager.getData(UserData.class, DatabaseManager.DB_KEY_USER, UserData.USERID_KEY, userID, new OnDataDownloadCompleteListener<UserData>() {
                @Override
                public void onDataDownloadComplete(List<UserData> data, int resultCode) {
                    if (resultCode == RESULT_OK && data.size() > 0) {
                        onCompleteListener.onComplete(RESULT_OK);
                    } else {
                        onCompleteListener.onComplete(RESULT_FIRST_USER);
                    }
                }
            });
        }
    }
    //endregion

    //region firebaseauth sign-up/in
    private void signInUser(final String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.error_notpopulated_name_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            handleLoginProcedure();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.error_authentication, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            ;
        });
    }

    private void signUpNewUser(final String username, final String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.error_notpopulated_name_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, R.string.success_registration, Toast.LENGTH_LONG).show();
                            handleLoginProcedure();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.error_authentication, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    //endregion
}
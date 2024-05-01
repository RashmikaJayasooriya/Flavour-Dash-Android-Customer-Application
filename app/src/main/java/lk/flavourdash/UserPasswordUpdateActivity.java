package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.flavourdash.utils.NotificationUtils;

public class UserPasswordUpdateActivity extends AppCompatActivity {

    private FirebaseUser user;
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_update);

        user = FirebaseAuth.getInstance().getCurrentUser();

        CircularProgressIndicator progressBar=findViewById(R.id.updatePasswordProgressBar);
        Button updateBtn=findViewById(R.id.updatePasswordButton);

        findViewById(R.id.updatePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                updateBtn.setEnabled(false);


                TextInputEditText userPasswordCurrentEditText = findViewById(R.id.userPasswordCurrentEditText);
                TextInputEditText userEmailCurrentEditText = findViewById(R.id.userEmailCurrentEditText);
                TextInputEditText userPasswordNewEditText = findViewById(R.id.userPasswordNewEditText);

                String userCurrentPassword = userPasswordCurrentEditText.getText().toString();
                String userCurrentEmail = userEmailCurrentEditText.getText().toString();
                String userNewPassword = userPasswordNewEditText.getText().toString();



                AuthCredential credential = EmailAuthProvider
                        .getCredential(userCurrentEmail, userCurrentPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated.");
                                    // User re-authenticated

                                    user.updatePassword(userNewPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        sendNotification();
                                                        Toast.makeText(UserPasswordUpdateActivity.this, "You have updated the password of your flavour dash account successfully.", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                        updateBtn.setEnabled(true);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

        //        Close Btn
        Button closeButton = findViewById(R.id.closePasswordUpdate);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private boolean isValidEmail(String email) {
        // Implement your email validation logic here
        // Return true if the email is valid, otherwise return false
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void sendNotification(){
        NotificationUtils.sendNotification(UserPasswordUpdateActivity.this,"Details Update","You have updated the password of your flavour dash account successfully.");
    }
}
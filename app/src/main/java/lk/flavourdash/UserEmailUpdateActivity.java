package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;

import lk.flavourdash.utils.NotificationUtils;

public class UserEmailUpdateActivity extends AppCompatActivity {
    private FirebaseUser user;
    public static final String TAG = MainActivity.class.getName();

    private NotificationManager notificationManager;
    private final String channelId = "error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_email_update);

        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.updateEmailButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText userPasswordCurrentEditText = findViewById(R.id.userPasswordCurrentEditText);
                TextInputEditText userEmailCurrentEditText = findViewById(R.id.userEmailCurrentEditText);
                TextInputEditText userEmailNewEditText = findViewById(R.id.userEmailNewEditText);

                String userCurrentPassword = userPasswordCurrentEditText.getText().toString();
                String userCurrentEmail = userEmailCurrentEditText.getText().toString();
                String userNewEmail = userEmailNewEditText.getText().toString();

//                Toast.makeText(UserEmailUpdateActivity.this, "userNewEmail" + userNewEmail, Toast.LENGTH_SHORT).show();

                AuthCredential credential = EmailAuthProvider
                        .getCredential(userCurrentEmail, userCurrentPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated.");

                                    user.verifyBeforeUpdateEmail(userNewEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(UserEmailUpdateActivity.this, "Please verify your new email.", Toast.LENGTH_SHORT).show();
                                                        Log.i(TAG, "Email sent.");
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error", e);
                                                }
                                            });
                                }
                            }
                        });

            }
        });

        //        Close Btn
        Button closeButton = findViewById(R.id.closeEmailUpdate);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                        } else {
                            String errorMessage = "Failed to send verification email";
                        }
                    });
        }
    }

    public void sendNotification(){
        NotificationUtils.sendNotification(UserEmailUpdateActivity.this,"Details Update","You have updated the email of your flavour dash account successfully.");
    }
}
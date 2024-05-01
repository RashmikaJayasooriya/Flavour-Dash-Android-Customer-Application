package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import lk.flavourdash.Model.User;

public class PhoneVerificationActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private User currentUser;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 second
    private static final long COUNTDOWN_TIME = 60000; // 60 seconds
    private CountDownTimer countDownTimer;
    private Button resendButton;
    private static final int MAX_RESEND_ATTEMPTS = 3; // Maximum number of resend attempts
    private int resendAttempts = 0; // Counter for resend attempts
    private ProgressBar loadingProgressBar;
    private Button submitButton;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.VISIBLE); // Show the loading indicator

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("user")) {
            currentUser = (User) intent.getSerializableExtra("user");

            TextView phoneNumber=findViewById(R.id.phoneNumber);
            phoneNumber.setText("Phone Number: "+currentUser.getMobile());

            if (currentUser != null) {
                initializeViews();
                setUpVerificationCallbacks();
                signInWithPhone(currentUser.getMobile());

                resendButton.setOnClickListener(v -> {
                    if (resendAttempts < MAX_RESEND_ATTEMPTS) {
                        resendVerificationCode(currentUser.getMobile());
                        startCountdownTimer();
                        resendAttempts++;
                    } else {
                        Toast.makeText(this, "Maximum resend attempts reached", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.i(TAG, "user not found");
            }
        } else {
            Log.i(TAG, "no content found in intent");
        }


//        Change Number
        findViewById(R.id.changeNumberButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initializeViews() {
        TextInputEditText[] verificationCodeInputs = new TextInputEditText[]{
                findViewById(R.id.textEdit1),
                findViewById(R.id.textEdit2),
                findViewById(R.id.textEdit3),
                findViewById(R.id.textEdit4),
                findViewById(R.id.textEdit5),
                findViewById(R.id.textEdit6)
        };

        resendButton = findViewById(R.id.resendButton);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {


            if (areVerificationCodeInputsEmpty(verificationCodeInputs)) {
                // Handle the case where verification code inputs are empty
                Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
            } else {
                loadingProgressBar.setVisibility(View.VISIBLE);
                String verificationCode = getVerificationCode(verificationCodeInputs);
                verifyOtp(verificationCode);
            }
        });
    }

    private boolean areVerificationCodeInputsEmpty(TextInputEditText[] verificationCodeInputs) {
        for (TextInputEditText input : verificationCodeInputs) {
            if (input.getText() == null || input.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void setUpVerificationCallbacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.i(TAG, "onVerificationCompleted: " + phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.i(TAG, "onVerificationFailed: " + e.getMessage());
                Toast.makeText(PhoneVerificationActivity.this, "Verification Failed", Toast.LENGTH_LONG).show();
                loadingProgressBar.setVisibility(View.GONE); // Hide the loading indicator
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.i(TAG, "onCodeSent: " + verificationId);
                loadingProgressBar.setVisibility(View.GONE); // Hide the loading indicator
                Toast.makeText(PhoneVerificationActivity.this, "OTP Code sent your phone", Toast.LENGTH_LONG).show();
                resendButton.setVisibility(View.VISIBLE);
                startCountdownTimer();
                submitButton.setEnabled(true);
                PhoneVerificationActivity.this.verificationId = verificationId;
                resendingToken = forceResendingToken;
            }
        };
    }


    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuth(credential);
    }

    private void signInWithPhone(String phoneNumber) {
        Toast.makeText(this, "phoneNumber"+phoneNumber, Toast.LENGTH_SHORT).show();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+94" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+94" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .setForceResendingToken(resendingToken)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {
        firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    updateUI(user);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            saveUserDetails(currentUser);
            loadingProgressBar.setVisibility(View.VISIBLE); // Show the loading indicator
//            Toast.makeText(this, "Phone Sign In Working!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(PhoneVerificationActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / COUNTDOWN_INTERVAL;
                updateResendButtonState(false, getString(R.string.resend_in, secondsRemaining));
            }

            @Override
            public void onFinish() {
                updateResendButtonState(true, getString(R.string.resend_code));
            }
        };

        countDownTimer.start();
    }

    private void updateResendButtonState(boolean isEnabled, String text) {
        resendButton.setEnabled(isEnabled);
        resendButton.setText(text);
    }

    private String getVerificationCode(TextInputEditText[] verificationCodeInputs) {
        StringBuilder verificationCode = new StringBuilder();
        for (TextInputEditText input : verificationCodeInputs) {
            verificationCode.append(input.getText().toString());
        }
        return verificationCode.toString();
    }

    private void saveUserDetails(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.i(TAG, user.getId());

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> Log.i(TAG, documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding document", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }
}
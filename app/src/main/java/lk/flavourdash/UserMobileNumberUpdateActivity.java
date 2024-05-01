package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import lk.flavourdash.Model.User;
import lk.flavourdash.utils.NotificationUtils;

public class UserMobileNumberUpdateActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;

    private String mverificationId;

    private PhoneAuthProvider.ForceResendingToken resendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_mobile_number_update);
        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        EditText phoneText = findViewById(R.id.editTextPhone);
        EditText otpText = findViewById(R.id.editTextOtp);
        TextInputLayout editTextOtpField = findViewById(R.id.editTextOtpField);
        Button verifyOtpBtn=findViewById(R.id.btnVerifyOtp);

        findViewById(R.id.btnSendOtp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithPhone(phoneText.getText().toString());
            }
        });

        verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp(otpText.getText().toString());
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.i(TAG, "onVerificationCompleted: "+phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.i(TAG, "onVerificationFailed: "+e.getMessage());
            }


            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.i(TAG, "onCodeSent: "+verificationId);
                Toast.makeText(UserMobileNumberUpdateActivity.this, "OTP Code sent your phone",Toast.LENGTH_LONG).show();

                mverificationId = verificationId;
                resendingToken = forceResendingToken;

                editTextOtpField.setVisibility(View.VISIBLE);
                verifyOtpBtn.setVisibility(View.VISIBLE);


            }
        };

//        Close Btn
        Button closeButton = findViewById(R.id.closeMobileUpdate);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void verifyOtp(String otp){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationId, otp);

        user.updatePhoneNumber(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User phone number updated.");
                            Toast.makeText(UserMobileNumberUpdateActivity.this, "User phone number updated." , Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void signInWithPhone(String phoneNumber){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+94" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = task.getResult().getUser();
                            sendNotification();
                        }
                    }
                });
    }

    public void sendNotification(){
        NotificationUtils.sendNotification(UserMobileNumberUpdateActivity.this,"Details Update","You have updated the mobile number of your flavour dash account successfully.");
    }

}
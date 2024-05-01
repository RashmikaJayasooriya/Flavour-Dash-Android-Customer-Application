package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class GetStartedActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Button getStartedBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        FirebaseApp.initializeApp(GetStartedActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());

        getStartedBtn = findViewById(R.id.getStartedButton);
        progressBar = findViewById(R.id.splashProgress);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.splashProgress).setVisibility(View.VISIBLE);
            }
        }, 600);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                userCheck();
            }
        }, 5000);


//        Get started button
        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetStartedActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void userCheck() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        if (currentUser != null) {
//            db.collection("users").whereEqualTo("id", currentUser.getUid()).whereEqualTo("active", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot querySnapshot = task.getResult();
//                        if (!querySnapshot.isEmpty()) {
//                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
//                            String documentId = documentSnapshot.getId();
//                            SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putString("userDocumentId", documentId);
//                            editor.apply();
//
//                            Intent intent = new Intent(GetStartedActivity.this, HomeActivity.class);
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            getStartedBtn.setVisibility(View.VISIBLE);
//                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//                    } else {
//                        Exception exception = task.getException();
//                    }
//                }
//            });
//        } else {
//            getStartedBtn.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.INVISIBLE);
//        }

        if (currentUser != null) {
            db.collection("users").whereEqualTo("id", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            boolean isActive = documentSnapshot.getBoolean("active");

                            if (isActive) {
                                // User is active, proceed with the login
                                String documentId = documentSnapshot.getId();
                                SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("userDocumentId", documentId);
                                editor.apply();

                                Intent intent = new Intent(GetStartedActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // User is not active -> Toast
                                Toast.makeText(GetStartedActivity.this, "Your account has been blocked by admin..", Toast.LENGTH_SHORT).show();
                                getStartedBtn.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            getStartedBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Exception exception = task.getException();
                    }
                }
            });
        } else {
            getStartedBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

    }
}
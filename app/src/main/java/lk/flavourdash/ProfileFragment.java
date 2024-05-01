package lk.flavourdash;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Arrays;
import java.util.Objects;

import lk.flavourdash.Model.Dish;
import lk.flavourdash.Model.Order;
import lk.flavourdash.Model.User;

public class ProfileFragment extends Fragment {
    public static final String TAG=MainActivity.class.getName();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private String userDocumentId;
    private User user;

    private EditText userFn;
    private EditText userLn;
    private EditText userEmail;
    private EditText userMobile;
    private ShapeableImageView profilePic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();

        TextInputLayout userMobileField = view.findViewById(R.id.userMobileField);
        userMobileField.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event, navigate to another activity
                Intent intent = new Intent(getActivity(), UserMobileNumberUpdateActivity.class);
                startActivity(intent);
            }
        });

        TextInputLayout userEmailField = view.findViewById(R.id.userEmailField);
        userEmailField.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event, navigate to another activity
                Intent intent = new Intent(getActivity(), UserEmailUpdateActivity.class);
                startActivity(intent);
            }
        });

        Button goToUpdateGeneralDetailsBtn=view.findViewById(R.id.goToUpdateGeneralDetails);
        goToUpdateGeneralDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserGeneralDetailsUpdateActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

        Button goToUpdatePasswordBtn=view.findViewById(R.id.goToUpdatePassword);
        goToUpdatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserPasswordUpdateActivity.class);
                startActivity(intent);
            }
        });

        userFn=view.findViewById(R.id.userFnEditText);
        userLn=view.findViewById(R.id.userLnEditText);
        userEmail=view.findViewById(R.id.userEmailEditText);
        userMobile=view.findViewById(R.id.userMobileEditText);
        profilePic=view.findViewById(R.id.profilePPic);

//        Fill details
        updateProfileDetails();
//        DocumentReference dishDocumentRef = firebaseFirestore.collection("users").document(userDocumentId);
//
//        dishDocumentRef.addSnapshotListener((documentSnapshot, error)  -> {
//            if (error != null) {
//                Log.e(TAG, "Error getting documents: ", error);
//                return;
//            }
//
//            if (isAdded()) {
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    user = documentSnapshot.toObject(User.class);
//
//                    userFn.setText(user.getFirstName());
//                    userLn.setText(user.getLastName());
//                    userEmail.setText(user.getEmail());
//                    userMobile.setText(user.getMobile());
//
//                    if (user.getImage() != null) {
//                        storage.getReference("user-images/" + user.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//
//                                if (!requireActivity().isFinishing() && !requireActivity().isDestroyed()) {
//                                    Glide.with(requireActivity())
//                                            .load(uri)
//                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                            .skipMemoryCache(true)
//                                            .centerCrop()
//                                            .transition(DrawableTransitionOptions.withCrossFade())
//                                            .into(profilePic);
//                                } else {
//                                    // Handle the case where the activity is finishing or has been destroyed
//                                    Log.e(TAG, "Activity is finishing or has been destroyed");
//                                }
//
//                            }
//                        });
//                    }
//                }
//
//            }
//
//
//        });

    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileDetails();
    }

    private void updateProfileDetails() {
        DocumentReference dishDocumentRef = firebaseFirestore.collection("users").document(userDocumentId);
        dishDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                user = documentSnapshot.toObject(User.class);
                updateUI();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting documents: ", e);
        });
    }

    // Method to update UI with user details
    private void updateUI() {
        if (user != null) {
            userFn.setText(user.getFirstName());
            userLn.setText(user.getLastName());
            userEmail.setText(user.getEmail());
            userMobile.setText(user.getMobile());

            if (user.getImage() != null) {
                storage.getReference("user-images/" + user.getImage()).getDownloadUrl().addOnSuccessListener(uri -> {
                    if (!requireActivity().isFinishing() && !requireActivity().isDestroyed()) {
                        Glide.with(requireActivity())
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(profilePic);
                    } else {
                        Log.e(TAG, "Activity is finishing or has been destroyed");
                    }
                });
            }
        }
    }

}
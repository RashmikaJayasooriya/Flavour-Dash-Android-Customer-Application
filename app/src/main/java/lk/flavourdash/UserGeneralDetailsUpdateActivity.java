package lk.flavourdash;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lk.flavourdash.Model.User;
import lk.flavourdash.utils.NotificationUtils;

public class UserGeneralDetailsUpdateActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private String userDocumentId;
    private FirebaseStorage storage;
    private Uri imagePath;
    private ImageButton imageButton;
    private User user;
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_general_details_update);

        Intent intent = getIntent();

        if (intent.hasExtra("user")) {
            user = (User) getIntent().getSerializableExtra("user");
        }

        SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        TextInputEditText userFnEditText = findViewById(R.id.updateUserFnEditText);
        TextInputEditText userLnEditText = findViewById(R.id.updateUserLnEditText);
        imageButton = findViewById(R.id.updateProfilePic);
        CircularProgressIndicator progressBar=findViewById(R.id.updateGeneralDetailProgressBar);
        Button updateBtn=findViewById(R.id.updateGeneralDetailButton);

//      Close Btn
        Button closeButton = findViewById(R.id.closeGeneralUpdate);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//      Fill Details

        userFnEditText.setText(user.getFirstName());
        userLnEditText.setText(user.getLastName());

        if (user.getImage() != null) {
            storage.getReference("user-images/" + user.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(UserGeneralDetailsUpdateActivity.this).load(uri).override(150, 150).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(imageButton);
                }
            });
        }

//      Profile pic


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }
        });


//        Update

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateFirstName = userFnEditText.getText().toString();
                String updateLastName = userLnEditText.getText().toString();

                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                updateBtn.setEnabled(false);


                Map<String, Object> updatedUserDetails = new HashMap<>();
                updatedUserDetails.put("firstName", updateFirstName);
                updatedUserDetails.put("lastName", updateLastName);

//                Toast.makeText(UserGeneralDetailsUpdateActivity.this, "user.getImage()"+String.valueOf(user.getImage() == null), Toast.LENGTH_SHORT).show();
                String imageId;
                if (user.getImage() == null) {
                    imageId = UUID.randomUUID().toString();
                    updatedUserDetails.put("image", imageId);

                } else {
                    imageId = user.getImage();
                }

//                Toast.makeText(UserGeneralDetailsUpdateActivity.this, "user.getImage()"+String.valueOf(imageId != null), Toast.LENGTH_SHORT).show();

                if (imageId != null) {
                    firebaseFirestore.collection("users").document(userDocumentId)
                            .update(updatedUserDetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setIndeterminate(false);
                                    progressBar.setProgressCompat(0, true);

                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    if (imagePath!=null) {
                                        StorageReference reference = storage.getReference("user-images").child(imageId);
                                        reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                progressBar.setVisibility(View.GONE);
                                                updateBtn.setEnabled(true);
                                                sendNotification();
                                                Toast.makeText(UserGeneralDetailsUpdateActivity.this, "Successfully Updated Details and Image", Toast.LENGTH_LONG).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                                progressBar.setVisibility(View.GONE);
                                                updateBtn.setEnabled(true);
                                                Toast.makeText(UserGeneralDetailsUpdateActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                                progressBar.setProgressCompat((int) progress, true);
                                            }
                                        });
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        updateBtn.setEnabled(true);
                                        sendNotification();
                                        Toast.makeText(UserGeneralDetailsUpdateActivity.this, "Successfully Updated Details and Image", Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                    progressBar.setVisibility(View.GONE);
                                    updateBtn.setEnabled(true);
                                    Toast.makeText(UserGeneralDetailsUpdateActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }

            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri selectedImage = result.getData().getData();
                        imagePath = result.getData().getData();
                        Log.i(TAG, "Image Path: " + selectedImage.getPath());

                        Glide.with(UserGeneralDetailsUpdateActivity.this)
                                .load(selectedImage)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(imageButton); // Replace "imageButton" with the actual ID of your ImageButton
                    }
                }
            });

    public void sendNotification(){
        NotificationUtils.sendNotification(UserGeneralDetailsUpdateActivity.this,"Details Update","You have updated the general details of your flavour dash account successfully.");
    }
}
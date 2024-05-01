package lk.flavourdash;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import lk.flavourdash.adapters.TabPagerAdapter;


public class SignInFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private static final String TAG = MainActivity.class.getName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar loadingProgressBar;


    private String mParam1;
    private String mParam2;

    public SignInFragment() {
        // Required empty public constructor
    }


    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        ////        sign in

        view.findViewById(R.id.signInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingProgressBar.setVisibility(View.VISIBLE);

                TextInputLayout signInEmailField = view.findViewById(R.id.signInEmailField);
                TextInputLayout signInPasswordField = view.findViewById(R.id.signInPasswordField);
                TextInputEditText emailEditText = view.findViewById(R.id.emailEditText);
                TextInputEditText passwordEditText = view.findViewById(R.id.passwordEditText);

                String email=emailEditText.getText().toString();
                String password=passwordEditText.getText().toString();

                // Perform validation
                if (email.isEmpty()) {
                    signInEmailField.setError("Email is required");
                    loadingProgressBar.setVisibility(View.GONE);
                    return;
                }else {
                    signInEmailField.setError(null);
                }

                if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    signInEmailField.setError("Invalid email address");
                    loadingProgressBar.setVisibility(View.GONE);
                    return;
                }else {
                    signInEmailField.setError(null);
                }

                if (password.isEmpty()) {
                    signInPasswordField.setError("Password is required");
                    loadingProgressBar.setVisibility(View.GONE);
                    return;
                }else {
                    signInPasswordField.setError(null);
                }


                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingProgressBar.setVisibility(View.GONE);
                            updateUI(firebaseAuth.getCurrentUser());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Incorrect Password or Email", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        ////        sign up using google

        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = Identity.getSignInClient(getContext());

        view.findViewById(R.id.googleIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder().setServerClientId(getString(R.string.web_client_id)).build();

                Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent).build();
                        signInLauncher.launch(intentSenderRequest);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        ////        sign up using microsoft
//        view.findViewById(R.id.microsoftIcon).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");;
//
//                firebaseAuth
//                        .startActivityForSignInWithProvider(getActivity(), provider.build())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                handleAuthResult(authResult);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Handle failure.
//                                Log.e(TAG, "Authentication failure", e);
//                            }
//                        });
//
//            }
//
//            private void handleAuthResult(AuthResult authResult) {
//                Log.i(TAG, "onSuccess:");
//
//                if (authResult != null) {
//                    AdditionalUserInfo additionalUserInfo = authResult.getAdditionalUserInfo();
//                    if (additionalUserInfo != null) {
//                        Log.i(TAG, "AdditionalUserInfo: " + additionalUserInfo.toString());
//
//                        Map<String, Object> profile = additionalUserInfo.getProfile();
//                        if (profile != null) {
//                            Log.i(TAG, "Profile: " + profile.toString());
//
//                            // Access user details from the profile Map
//                            String userId = (String) profile.get("id");
//                            String displayName = (String) profile.get("name");
//                            String email = (String) profile.get("email");
//
//                            Log.i(TAG, "UserId: " + userId);
//                            Log.i(TAG, "DisplayName: " + displayName);
//                            Log.i(TAG, "Email: " + email);
//
//                            Toast.makeText(getActivity(), userId + displayName + email, Toast.LENGTH_SHORT).show();
//                            // ... access other details as needed
//                        } else {
//                            Log.e(TAG, "Profile is null");
//                        }
//                    } else {
//                        Log.e(TAG, "AdditionalUserInfo is null");
//                    }
//
//                    AuthCredential credential = authResult.getCredential();
//                    if (credential != null) {
//                        Log.i(TAG, "Credential: " + credential.toString());
//                    } else {
//                        Log.e(TAG, "Credential is null");
//                    }
//                } else {
//                    Log.e(TAG, "AuthResult is null");
//                }
//            }
//
//        });

//        Sign up move
        view.findViewById(R.id.signUpMoveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSignUpTab();
            }
        });

    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(authCredential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void handleSignInResult(Intent intent) {

        loadingProgressBar.setVisibility(View.VISIBLE); // Show the loading indicator

        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            Log.e(TAG, e.getMessage());
            loadingProgressBar.setVisibility(View.GONE); // Hide the loading indicator in case of an error
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            handleSignInResult(o.getData());
        }
    });

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            ((MainActivity) requireActivity()).getSharedViewModel().setCurrentUser(user);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Log.i(TAG, "user Uid:"+ user.getUid());

            db.collection("users").whereEqualTo("id", user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        loadingProgressBar.setVisibility(View.GONE); // Hide the loading indicator
                        if (querySnapshot.isEmpty()) {
                            TabLayout tabLayout = getActivity().findViewById(R.id.signTabLayout);
                            tabLayout.selectTab(tabLayout.getTabAt(1));
                        } else {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            boolean isActive = documentSnapshot.getBoolean("active");
                            if (isActive) {
                                String documentId = documentSnapshot.getId();
                                SharedPreferences preferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("userDocumentId", documentId);
                                editor.apply();

                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish();

                            }else {
                                // User is not active -> Toast
                                Toast.makeText(getActivity(), "Your account has been blocked by admin..", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                    }
                }
            });

        }
    }

    private void switchToSignUpTab() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            TabPagerAdapter tabPagerAdapter = mainActivity.getTabPagerAdapter();

            if (tabPagerAdapter != null) {
                tabPagerAdapter.setCurrentItem(1); // 1 is the position of the SignUpFragment
            }
        }
    }

}


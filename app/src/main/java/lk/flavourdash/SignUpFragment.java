package lk.flavourdash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.flavourdash.Model.User;

public class SignUpFragment extends Fragment {

    private TextInputLayout passwordTextInputLayout;
    private TextInputEditText passwordEditText;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private static final String TAG = MainActivity.class.getName();
    private SharedViewModel sharedViewModel;
    private Boolean isOAuthSignUp = false;
    private FirebaseUser currentUser;
    private Button signUpBtn;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();


////     password
        passwordTextInputLayout = view.findViewById(R.id.signUpPasswordField);
        passwordEditText = view.findViewById(R.id.signUpPasswordEditText);

        // Set a TextChangedListener on the password EditText
        passwordEditText.addTextChangedListener(new PasswordTextWatcher());

////        Sign up

        TextInputEditText firstNameEditText = view.findViewById(R.id.signUpFnEditText);
        TextInputEditText lastNameEditText = view.findViewById(R.id.signUpLnEditText);
        TextInputEditText emailEditText = view.findViewById(R.id.signUpEmailEditText);
        TextInputEditText mobileEditText = view.findViewById(R.id.signUpMobileEditText);
        TextInputEditText passwordEditText = view.findViewById(R.id.signUpPasswordEditText);

////        Sign up with google

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe changes in the user data
        sharedViewModel.getCurrentUser().observe(getActivity(), new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser user) {

                isOAuthSignUp = true;
                currentUser = user;

                String displayName = user.getDisplayName();

                if (displayName != null && !displayName.isEmpty()) {
                    String[] nameParts = displayName.split(" ");

                    if (nameParts.length > 0) {
                        firstNameEditText.setText(nameParts[0]);
                        lastNameEditText.setText(nameParts[1]);
                    } else {
                        firstNameEditText.setText(displayName);
                    }
                }

                emailEditText.setText(user.getEmail());
                mobileEditText.setText(user.getPhoneNumber());
            }
        });

////        Sign up directly
        signUpBtn= view.findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextInputLayout signUpFnField = view.findViewById(R.id.signUpFnField);
                TextInputLayout signUpLnField = view.findViewById(R.id.signUpLnField);
                TextInputLayout signUpEmailField = view.findViewById(R.id.signUpEmailField);
                TextInputLayout signUpMobileField = view.findViewById(R.id.signUpMobileField);
                TextInputLayout signUpPasswordField = view.findViewById(R.id.signUpPasswordField);
                CheckBox checkBox = view.findViewById(R.id.signUpAgreeField);

                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String mobile = mobileEditText.getText().toString();
                String password = passwordEditText.getText().toString();


                // Perform basic validation
                if (firstName.isEmpty()) {
                    signUpFnField.setError("First name is required");
                    return;
                }else {
                    signUpFnField.setError(null);
                }

                if (lastName.isEmpty()) {
                    signUpLnField.setError("Last name is required");
                    return;
                }else {
                    signUpLnField.setError(null);
                }

                if (email.isEmpty()) {
                    signUpEmailField.setError("Email is required");
                    return;
                }else {
                    signUpEmailField.setError(null);
                }

                if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    signUpEmailField.setError("Invalid email address");
                    return;
                }else {
                    signUpEmailField.setError(null);
                }

                if (mobile.isEmpty()) {
                    signUpMobileField.setError("Mobile number is required");
                    return;
                }else {
                    signUpMobileField.setError(null);
                }

                if (!mobile.matches("0[1-9][0-9]{8}")) {
                    signUpMobileField.setError("Invalid phone number");
                    return;
                }else {
                    signUpMobileField.setError(null);
                }


                if (password.isEmpty()) {
                    signUpPasswordField.setError("Password is required");
                    return;
                }else {
                    signUpPasswordField.setError(null);
                }

                if (!checkBox.isChecked()) {
                    Toast.makeText(getActivity(), "You are not agreed with our policies", Toast.LENGTH_SHORT).show();
                    return;
                }


                Log.i(TAG, "onClick: " + isOAuthSignUp);

                if (!isOAuthSignUp) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();

                                Log.i(TAG, "success");

                                User user = new User(currentUser.getUid(), firstName, lastName, email, mobile, password,true);

                                Intent intent = new Intent(getActivity(), PhoneVerificationActivity.class);
                                intent.putExtra("user", user);
                                startActivity(intent);

                            } else {
                                Log.w(TAG, "failed");
                                Toast.makeText(getContext(), "Email Registration Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {

                    User user = new User(currentUser.getUid(), firstName, lastName, email, mobile, password,true);

                    Intent intent = new Intent(getActivity(), PhoneVerificationActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }


            }
        });

    }


    ////     password
    private class PasswordTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            // Not needed for this example
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // Validate the password and update the error message
            validatePassword(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // Not needed for this example
        }
    }

    ////     password
    private void validatePassword(String password) {
        // Define your password conditions
        boolean hasLowerCase = !password.equals(password.toUpperCase());
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = !password.matches("[A-Za-z0-9]*");

        // Initialize error message
        StringBuilder errorMessage = new StringBuilder("Password must meet the conditions:");

        // Update error message based on conditions
        if (password.length() < 8) {
            errorMessage.append("\n- At least 8 characters");
        }

        if (!hasLowerCase) {
            errorMessage.append("\n- Lowercase letters (a-z)");
        }

        if (!hasUpperCase) {
            errorMessage.append("\n- Uppercase letters (A-Z)");
        }

        if (!hasDigit) {
            errorMessage.append("\n- Numbers (0-9)");
        }

        if (!hasSpecialChar) {
            errorMessage.append("\n- Special characters");
        }

        // Clear the error if all conditions are met
        if (password.length() >= 8 && hasLowerCase && hasUpperCase && hasDigit && hasSpecialChar) {
            signUpBtn.setEnabled(true);
            passwordTextInputLayout.setError(null);
        } else {
            // Display the updated error message
            signUpBtn.setEnabled(false);
            passwordTextInputLayout.setError(errorMessage.toString());
        }
    }
}
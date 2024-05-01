package lk.flavourdash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import lk.flavourdash.adapters.TabPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getName();
    private SharedViewModel sharedViewModel;
    private TabPagerAdapter adapter;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();

//        Sign Tab Setup
        TabLayout tabLayout = findViewById(R.id.signTabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.signViewPager);

         adapter = new TabPagerAdapter(this,viewPager2);
        viewPager2.setAdapter(adapter);

        // Connect the TabLayout with the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    // Optional: Set tab titles
                    switch (position) {
                        case 0:
                            tab.setText(getString(R.string.sign_in));
                            break;
                        case 1:
                            tab.setText(getString(R.string.sign_up));
                            break;
                    }
                }).attach();



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "onTabSelected: "+tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselect
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselect
            }
        });



        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);


    }

    public SharedViewModel getSharedViewModel() {
        return sharedViewModel;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//        startActivity(intent);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Log.i(TAG, "user Uid:"+ currentUser.getUid());

//        currentUser=null;
        if(currentUser!=null){
            db.collection("users").whereEqualTo("id", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {

                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String documentId = documentSnapshot.getId();
                            SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("userDocumentId", documentId);
                            editor.apply();

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Exception exception = task.getException();
                    }
                }
            });
        }

    }

    public TabPagerAdapter getTabPagerAdapter() {
        return adapter;
    }
}
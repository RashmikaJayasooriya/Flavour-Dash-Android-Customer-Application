package lk.flavourdash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Order;
import lk.flavourdash.adapters.CartItemAdapter;
import lk.flavourdash.listeners.OnItemClickListener;
import lk.flavourdash.listeners.OnQuantityChangeListener;
import lk.flavourdash.utils.NotificationUtils;


public class CartFragment extends Fragment implements OnItemClickListener<CartItem> {

    private FirebaseFirestore firebaseFirestore;
    private String userDocumentId;
    private ArrayList<CartItem> cartItems;
    private TextView totalAmountView;
    private double totalAmount = 0;
    private CartItemAdapter cartItemAdapter;
    public static final String TAG = MainActivity.class.getName();
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress;
    private TextView estimatedTimeView;
    private LatLng nearestBranch = null;
    private static final String API_KEY = "***";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();

        totalAmountView= view.findViewById(R.id.totalAmountView);
        cartItems = new ArrayList<>();
        cartItemAdapter = new CartItemAdapter(cartItems,getContext(), this);
        RecyclerView cartItemRecyclerView = view.findViewById(R.id.cartItemRecyclerView);
        cartItemRecyclerView.setAdapter(cartItemAdapter);

        setupFirestoreListenerForCategory();

        cartItemAdapter.setOnQuantityChangeListener(new OnQuantityChangeListener() {
            @Override
            public void onQuantityChange(double itemPrice) {
                totalAmount = itemPrice;
                totalAmountView.setText(String.format(Locale.getDefault(), "%.2f", totalAmount));
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView selectedLocation = view.findViewById(R.id.selectedLocation);
        estimatedTimeView = view.findViewById(R.id.estimatedTimeView);
//        Go To Location Select
        view.findViewById(R.id.selectLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeliverLocationActivity.class);
//                startActivity(intent);
                activityResultLauncher.launch(intent);
            }
        });

//      Get info from Location Activity
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the Intent
                        selectedLatitude = data.getDoubleExtra("selectedLatitude", 0.0);
                        selectedLongitude = data.getDoubleExtra("selectedLongitude", 0.0);
                        selectedAddress = data.getStringExtra("selectedAddress");

                        Log.d("ActivityResult --", "Latitude: " + selectedLatitude);
                        Log.d("ActivityResult --", "Longitude: " + selectedLongitude);
                        Log.d("ActivityResult --", "Address: " + selectedAddress);

                        selectedLocation.setVisibility(View.VISIBLE);
                        selectedLocation.setText(selectedAddress);
                        nearestLocationAndEstimatedTime();
                    }
                });


//      Place Order
        view.findViewById(R.id.orderBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date orderTime = new Date();
                TextView requests=view.findViewById(R.id.requests);
                Order order=new Order(userDocumentId,"Pending",Double.parseDouble(totalAmountView.getText().toString()),orderTime,selectedAddress,selectedLatitude,selectedLongitude,requests.getText().toString());

                firebaseFirestore.collection("Orders").add(order).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String orderDocumentId = documentReference.getId();

                        for (CartItem cartItem : cartItems) {
                            firebaseFirestore.collection("Orders")
                                    .document(orderDocumentId)
                                    .collection("OrderItems")
                                    .add(cartItem)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            sendNotification();
                                            Toast.makeText(getActivity(), "Order Item Sent Successfully", Toast.LENGTH_SHORT).show();

                                            requests.setText("");

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), "Order Item Send Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void sendNotification(){
        NotificationUtils.sendNotification(getContext(),"Flavour Dash: Food Order","Your order send successfully.");
    }

    private void nearestLocationAndEstimatedTime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<LatLng> branches = new ArrayList<>();
        db.collection("restaurant_branches")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                branches.add(new LatLng(latitude, longitude));
                            }

                            float[] results = new float[1];
                            float minDistance = Float.MAX_VALUE;

                            for (LatLng branch : branches) {
                                Location.distanceBetween(selectedLatitude, selectedLongitude, branch.latitude, branch.longitude, results);
                                float distance = results[0];
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    nearestBranch = branch;
                                }
                            }

                            estimateRouteTime(new LatLng(selectedLatitude, selectedLongitude), nearestBranch);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    public void estimateRouteTime(LatLng userLatLng, LatLng nearestBranch) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();

        Log.i(TAG, nearestBranch.latitude+","+nearestBranch.longitude);
        Log.i(TAG, userLatLng.latitude+","+ userLatLng.longitude);

        DistanceMatrixApi.newRequest(context)
                .origins(nearestBranch.latitude+","+nearestBranch.longitude)
                .destinations(userLatLng.latitude+","+ userLatLng.longitude)
                .mode(TravelMode.DRIVING)
                .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                    @Override
                    public void onResult(DistanceMatrix result) {
                        // Here you can get the estimated time

                        long timeInSeconds = result.rows[0].elements[0].duration.inSeconds;
                        long timeInMinutes = timeInSeconds / 60;


                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                estimatedTimeView.setVisibility(View.VISIBLE);
                                estimatedTimeView.setText("Estimated Delivery Time From Nearest Branch: "+timeInMinutes + " minutes");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                    }
                });
    }


    private void setupFirestoreListenerForCategory() {

        firebaseFirestore.collection("users").document(userDocumentId).collection("cart").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    CartItem cartItem = change.getDocument().toObject(CartItem.class);
                    cartItem.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            cartItems.add(cartItem);
                            break;
                        case MODIFIED:
                            updateModifiedCategory(change);
                            break;
                        case REMOVED:
                            cartItems.removeIf(i -> i.getId().equals(cartItem.getId()));
                            break;
                    }
                }


                totalAmount=0;
                for (CartItem item : cartItems) {
                    totalAmount += item.getPortionPrices().values().iterator().next() * item.getNoOfItems();
                }
                totalAmountView.setText( String.format(Locale.getDefault(), "%.2f", totalAmount));

                cartItemAdapter.notifyDataSetChanged();
            }
        });
    }


    private void updateModifiedCategory(DocumentChange change) {
        CartItem updatedCartItem = change.getDocument().toObject(CartItem.class);
        updatedCartItem.setId(change.getDocument().getId());

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem existingCartItem = cartItems.get(i);

            if (existingCartItem.getId().equals(updatedCartItem.getId())) {
                if (existingCartItem.getNoOfItems() != updatedCartItem.getNoOfItems()) {
                    existingCartItem.setNoOfItems(updatedCartItem.getNoOfItems());
                }
                if (!existingCartItem.getOptions().equals(updatedCartItem.getOptions())) {
                    existingCartItem.setOptions(updatedCartItem.getOptions());
                }
                if (!existingCartItem.getPortionPrices().equals(updatedCartItem.getPortionPrices())) {
                    existingCartItem.setPortionPrices(updatedCartItem.getPortionPrices());
                }

                // position i
                cartItemAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onItemClick(CartItem cartItem) {
    }
}





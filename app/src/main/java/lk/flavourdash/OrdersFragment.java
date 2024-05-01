package lk.flavourdash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

import lk.flavourdash.Model.Order;
import lk.flavourdash.adapters.OrderAdapter;

public class OrdersFragment extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private String userDocumentId;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orders;
    public static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore=FirebaseFirestore.getInstance();

        orders=new ArrayList<>();
        orderAdapter=new OrderAdapter(orders,getContext());
        RecyclerView orderViewRecycleView = view.findViewById(R.id.orderViewRecycleView);
        orderViewRecycleView.setAdapter(orderAdapter);

        setupFirestoreListenerForOrders();

    }

    private void setupFirestoreListenerForOrders() {
        firebaseFirestore.collection("Orders").whereEqualTo("userId",userDocumentId ).whereIn("orderStatus", Arrays.asList("Pending", "Accept")).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Order order = change.getDocument().toObject(Order.class);
                    order.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            orders.add(order);
                            break;
                        case MODIFIED:
                            updateModifiedOrder(change);
                            break;
                        case REMOVED:
                            orders.removeIf(i -> i.getId().equals(order.getId()));
                            break;
                    }
                }

                orderAdapter.notifyDataSetChanged();
            }
        });

//        firebaseFirestore.collection("Orders").whereEqualTo("userId",userDocumentId ).whereEqualTo("orderStatus","Accept").addSnapshotListener((value, error) -> {
//            if (error != null) {
//                Log.e(TAG, "Error getting documents: ", error);
//                return;
//            }
//
//            if (value != null) {
//                for (DocumentChange change : value.getDocumentChanges()) {
//                    Order order = change.getDocument().toObject(Order.class);
//                    order.setId(change.getDocument().getId());
//
//                    switch (change.getType()) {
//                        case ADDED:
//                            orders.add(order);
//                            break;
//                        case MODIFIED:
//                            updateModifiedOrder(change);
//                            break;
//                        case REMOVED:
//                            orders.removeIf(i -> i.getId().equals(order.getId()));
//                            break;
//                    }
//                }
//
//                orderAdapter.notifyDataSetChanged();
//            }
//        });
    }

    private void updateModifiedOrder(DocumentChange change) {
        Order updatedOrder = change.getDocument().toObject(Order.class);
        updatedOrder.setId(change.getDocument().getId());

        for (int i = 0; i < orders.size(); i++) {
            Order existingOrder = orders.get(i);

            if (existingOrder.getId().equals(updatedOrder.getId())) {
                // Update only the fields that have changed
                if (!existingOrder.getOrderStatus().equals(updatedOrder.getOrderStatus())) {
                    existingOrder.setOrderStatus(updatedOrder.getOrderStatus());
                }

                // Notify the adapter that the item at position i has been modified
                orderAdapter.notifyItemChanged(i);
                break; // Exit the loop since we found the matching category
            }
        }
    }
}
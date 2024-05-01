package lk.flavourdash.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.flavourdash.MainActivity;
import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Order;
import lk.flavourdash.R;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {

    public static final String TAG = MainActivity.class.getName();
    private ArrayList<Order> data;
    private ArrayList<CartItem> cartItems;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private OrderItemAdapter orderItemAdapter;


    public OrderAdapter(ArrayList<Order> data, Context context) {
        this.data = data;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Order item = data.get(position);

        holder.orderIdView.setText(item.getId());
        holder.orderStatusView.setText(item.getOrderStatus());
        holder.orderAmountView.setText(item.getTotalAmount().toString());
        holder.orderAmountView.setText(item.getTotalAmount().toString());

        holder.orderAddressView.setText(item.getDeliveryAddress());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.orderTimeView.setText(sdf.format(item.getOrderTime()));
        holder.orderRequestView.setText(item.getRequests());

        cartItems=new ArrayList<>();
        orderItemAdapter=new OrderItemAdapter(cartItems, context.getApplicationContext());
        setupFirestoreListenerForDish(item.getId());

        LinearLayoutManager linearLayout=new LinearLayoutManager(context);
        holder.orderItemsView.setLayoutManager(linearLayout);
        holder.orderItemsView.setAdapter(orderItemAdapter);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderIdView;
        TextView orderStatusView;
        TextView orderAmountView;
        TextView orderAddressView;
        TextView orderTimeView;
        TextView orderRequestView;
        RecyclerView orderItemsView;

        MyViewHolder(View itemView) {
            super(itemView);
            orderIdView=itemView.findViewById(R.id.orderIdView);
            orderStatusView=itemView.findViewById(R.id.orderStatusView);
            orderAmountView=itemView.findViewById(R.id.orderAmountView);
            orderAddressView=itemView.findViewById(R.id.orderAddressView);
            orderTimeView=itemView.findViewById(R.id.orderTimeView);
            orderRequestView=itemView.findViewById(R.id.orderRequestView);
            orderItemsView=itemView.findViewById(R.id.orderItemsView);
        }
    }

    private void setupFirestoreListenerForDish(String orderId) {
        firebaseFirestore.collection("Orders").document(orderId).collection("OrderItems").addSnapshotListener((value, error) -> {
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

                orderItemAdapter.notifyDataSetChanged();
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

                orderItemAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

}

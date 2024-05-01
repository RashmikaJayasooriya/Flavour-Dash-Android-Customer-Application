package lk.flavourdash.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ouattararomuald.slider.ImageSlider;
import com.ouattararomuald.slider.SliderAdapter;
import com.ouattararomuald.slider.loaders.glide.GlideImageLoaderFactory;

import lk.flavourdash.MainActivity;
import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.R;
import lk.flavourdash.listeners.OnItemClickListener;
import lk.flavourdash.listeners.OnQuantityChangeListener;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.MyViewHolder> {

    private ArrayList<CartItem> data;
    private ArrayList<Dish> dishes;
    private Context context;
    private FirebaseStorage storage;
    private FirebaseFirestore firebaseFirestore;

    private OnItemClickListener listener;
    private double totalAmount = 0.0;

    private OnQuantityChangeListener quantityChangeListener;


    public CartItemAdapter(ArrayList<CartItem> data, Context context, OnItemClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
        this.storage = FirebaseStorage.getInstance();
        this.firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_dish_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        CartItem item = data.get(position);
        holder.cartDishPrice.setText(String.format(Locale.getDefault(), "%.2f", item.getPortionPrices().values().iterator().next()*item.getNoOfItems()));
        holder.cartDishQuantity.setText(String.valueOf(item.getNoOfItems()));
        holder.cartDishSize.setText(item.getPortionPrices().keySet().iterator().next());


        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference dishDocumentRef = firebaseFirestore.collection("Dishes").document(item.getDishId());

        dishDocumentRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e(MainActivity.class.getName(), "Error fetching dish document: ", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Dish dish = documentSnapshot.toObject(Dish.class);
                holder.cartDishName.setText(dish.getName());

                List<String> savedImages = dish.getImages();
                List<String> imageUrls = new ArrayList<>();
                List<String> descriptions = new ArrayList<>();

                for (int i = 0; i < savedImages.size(); i++) {
                    Log.i(MainActivity.class.getName(), "savedImages.get(i)" + savedImages.get(i));
                    StorageReference imageRef = storage.getReference("dish-images/" + savedImages.get(i));

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Convert Uri -> URL + list
                        Log.i(MainActivity.class.getName(), "uri" + uri);
                        try {
                            URL url = new URL(uri.toString());
                            imageUrls.add(url.toString());

                            if (imageUrls.size() == savedImages.size()) {
                                SliderAdapter sliderAdapter = new SliderAdapter(context, new GlideImageLoaderFactory(), imageUrls, descriptions, "1");
                                holder.cartDishImageSlider.setAdapter(sliderAdapter);
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
                }
            }
        });


        // quantity button
        holder.cartDishQuantityIncBtn.setOnClickListener(v -> {
            int newQuantity = item.getNoOfItems() + 1;
            item.setNoOfItems(newQuantity);
            holder.cartDishQuantity.setText(String.valueOf(newQuantity));

            // price * new quantity
            updatePrice(holder, item,"add");

        });

        holder.cartDishQuantityDecBtn.setOnClickListener(v -> {
            int newQuantity = item.getNoOfItems() - 1;
            if (newQuantity >= 1) {
                item.setNoOfItems(newQuantity);
                holder.cartDishQuantity.setText(String.valueOf(newQuantity));

                // new quantity
                updatePrice(holder, item,"sub");

            }
        });

        // delete button
        holder.cartDishDeleteBtn.setOnClickListener(v -> {
            deleteCartItemFromFirestore(item);
        });

    }


    private void updatePrice(MyViewHolder holder, CartItem cartItem,String type) {
        double portionPrice = cartItem.getPortionPrices().values().iterator().next();
        double totalPrice = portionPrice * cartItem.getNoOfItems();
        holder.cartDishPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));

        // Subtract the old price before updating
        if (type=="add"){
            totalAmount -= portionPrice * (cartItem.getNoOfItems() - 1);
        }else {
            totalAmount -= portionPrice * (cartItem.getNoOfItems() + 1);
        }
        totalAmount += totalPrice;

        if (quantityChangeListener != null) {
            quantityChangeListener.onQuantityChange(totalAmount);
        }
        updateQuantityInFirestore(cartItem.getId(), cartItem.getNoOfItems());
    }


    private void updateQuantityInFirestore(String cartItemId, int newQuantity) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userDocumentId = preferences.getString("userDocumentId", "");

        firebaseFirestore.collection("users").document(userDocumentId).collection("cart")
                .document(cartItemId)
                .update("noOfItems", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    // Quantity updated sucess
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void deleteCartItemFromFirestore(CartItem cartItem) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userDocumentId = preferences.getString("userDocumentId", "");

        String cartItemId = cartItem.getId();

        firebaseFirestore.collection("users").document(userDocumentId).collection("cart")
                .document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    data.remove(cartItem);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageSlider cartDishImageSlider;
        TextView cartDishSize;
        TextView cartDishName;
        TextView cartDishPrice;
        TextView cartDishQuantity;
        Button cartDishQuantityIncBtn;
        Button cartDishQuantityDecBtn;
        Button cartDishDeleteBtn;

        MyViewHolder(View itemView) {
            super(itemView);
            cartDishImageSlider = itemView.findViewById(R.id.cartDishImageSlider);
            cartDishSize = itemView.findViewById(R.id.cartDishSize);
            cartDishName = itemView.findViewById(R.id.cartDishName);
            cartDishPrice = itemView.findViewById(R.id.cartDishPrice);
            cartDishQuantity = itemView.findViewById(R.id.cartDishQuantity);
            cartDishQuantityIncBtn = itemView.findViewById(R.id.cartDishQuantityIncBtn);
            cartDishQuantityDecBtn = itemView.findViewById(R.id.cartDishQuantityDecBtn);
            cartDishDeleteBtn = itemView.findViewById(R.id.cartDishDeleteBtn);
        }
    }
}

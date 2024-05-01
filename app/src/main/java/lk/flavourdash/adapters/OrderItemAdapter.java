package lk.flavourdash.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.MyViewHolder> {

    private ArrayList<CartItem> data;
    private Context context;
    private FirebaseStorage storage;
    private FirebaseFirestore firebaseFirestore;
    private double totalAmount = 0.0;

    public OrderItemAdapter(ArrayList<CartItem> data, Context context) {
        this.data = data;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        CartItem item = data.get(position);

        holder.number.setText(String.valueOf(position+1));
        holder.orderedDishPrice.setText(String.format(Locale.getDefault(), "%.2f", item.getPortionPrices().values().iterator().next()*item.getNoOfItems()));
        holder.orderedDishQuantity.setText("X"+String.valueOf(item.getNoOfItems()));


        DocumentReference dishDocumentRef = firebaseFirestore.collection("Dishes").document(item.getDishId());

        dishDocumentRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e(MainActivity.class.getName(), "Error fetching dish document: ", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Dish dish = documentSnapshot.toObject(Dish.class);
                holder.orderedDishName.setText(dish.getName());

                List<String> savedImages = dish.getImages();
                List<String> imageUrls = new ArrayList<>();
                List<String> descriptions = new ArrayList<>();

                for (int i = 0; i < savedImages.size(); i++) {
                    Log.i(MainActivity.class.getName(), "savedImages.get(i)" + savedImages.get(i));
                    StorageReference imageRef = storage.getReference("dish-images/" + savedImages.get(i));

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.i(MainActivity.class.getName(), "uri" + uri);
                        try {
                            URL url = new URL(uri.toString());
                            imageUrls.add(url.toString());

                            // Check all URLs are fetched
                            if (imageUrls.size() == savedImages.size()) {
                                SliderAdapter sliderAdapter = new SliderAdapter(context, new GlideImageLoaderFactory(), imageUrls, descriptions, "1");
                                holder.orderedDishImageSlider.setAdapter(sliderAdapter);
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

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView orderedDishName;
        TextView orderedDishQuantity;
        TextView orderedDishPrice;
        ImageSlider orderedDishImageSlider;

        MyViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            orderedDishName = itemView.findViewById(R.id.orderedDishName);
            orderedDishQuantity = itemView.findViewById(R.id.orderedDishQuantity);
            orderedDishPrice = itemView.findViewById(R.id.orderedDishPrice);
            orderedDishImageSlider = itemView.findViewById(R.id.orderedDishImageSlider);
        }
    }
}

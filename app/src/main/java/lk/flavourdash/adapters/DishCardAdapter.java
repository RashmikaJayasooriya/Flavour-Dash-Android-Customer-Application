package lk.flavourdash.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ouattararomuald.slider.ImageSlider;
import com.ouattararomuald.slider.SliderAdapter;
import com.ouattararomuald.slider.loaders.glide.GlideImageLoaderFactory;

import lk.flavourdash.MainActivity;
import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.R;
import lk.flavourdash.SingleDishViewActivity;
import lk.flavourdash.listeners.OnItemClickListener;

public class DishCardAdapter extends RecyclerView.Adapter<DishCardAdapter.MyViewHolder> {

    private ArrayList<Dish> data;
    private ArrayList<CartItem> cartItems;
    private Context context;
    private FirebaseStorage storage;

    private OnItemClickListener listener;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });


    public DishCardAdapter(ArrayList<Dish> data, ArrayList<CartItem> cartItems, Context context, OnItemClickListener listener) {
        this.data = data;
        this.cartItems=cartItems;
        this.context = context;
        this.listener = listener;
        this.storage = FirebaseStorage.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_card_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Dish item = data.get(position);

        int totNoOfItems=0;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getDishId().equals(item.getId())) {
                totNoOfItems+=cartItem.getNoOfItems();
                holder.dishCartItemNo.setVisibility(View.VISIBLE);
            }
        }

        holder.dishCartItemNo.setText(String.valueOf(totNoOfItems));

        holder.dishCardProductName.setText(item.getName());
        holder.dishCardCategoryName.setText(item.getCategory());
        holder.dishCardProductRating.setText(item.getRating().toString());

        double price = item.getPortionPrices().values().iterator().next();
        String formattedPrice = String.format(Locale.getDefault(), "%.2f", price);
        holder.dishCardProductPrice.setText(formattedPrice);


        List<String> savedImages = item.getImages();
        List<String> imageUrls = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for (int i = 0; i < savedImages.size(); i++) {
            Log.i(MainActivity.class.getName(), "savedImages.get(i)" + savedImages.get(i));
            storage.getReference("dish-images/" + savedImages.get(i)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.i(MainActivity.class.getName(), "uri" + uri);
                    try {
                        URL url = new URL(uri.toString());
                        imageUrls.add(url.toString());

                        if (imageUrls.size() == savedImages.size()) {
                            SliderAdapter sliderAdapter = new SliderAdapter(context, new GlideImageLoaderFactory(),imageUrls,descriptions,"1");
                            holder.sliderView.setAdapter(sliderAdapter);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        holder.materialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the context is not null before using it
                if (view.getContext() != null) {
                    Intent intent = new Intent(view.getContext(), SingleDishViewActivity.class);
                    intent.putExtra("dish",item);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line
                    view.getContext().startActivity(intent);
                } else {
                    Log.e("DishCardAdapter", "Context is null");
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageSlider sliderView;
        TextView dishCardProductName;
        TextView dishCardCategoryName;
        TextView dishCardProductPrice;
        TextView dishCardProductRating;
        TextView dishCartItemNo;
        CardView materialCardView;

        MyViewHolder(View itemView) {
            super(itemView);
            sliderView=itemView.findViewById(R.id.dish_card_image_slider);
            dishCardProductName=itemView.findViewById(R.id.dishCardProductName);
            dishCardProductRating=itemView.findViewById(R.id.dishCardProductRating);
            dishCardCategoryName=itemView.findViewById(R.id.dishCardCategoryName);
            dishCardProductPrice=itemView.findViewById(R.id.dishCardProductPrice);
            dishCartItemNo=itemView.findViewById(R.id.dishCartItemNo);
            materialCardView=itemView.findViewById(R.id.card);
        }
    }
}

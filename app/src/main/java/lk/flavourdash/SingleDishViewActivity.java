package lk.flavourdash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.ouattararomuald.slider.ImageSlider;
import com.ouattararomuald.slider.SliderAdapter;
import com.ouattararomuald.slider.loaders.glide.GlideImageLoaderFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.adapters.DishViewOptionViewAdapter;
import lk.flavourdash.adapters.DishViewPriceViewAdapter;
import lk.flavourdash.listeners.OnCardSelectedListener;
import lk.flavourdash.listeners.OnDishOptionCheckedChangeListener;
import lk.flavourdash.utils.NotificationUtils;

public class SingleDishViewActivity extends AppCompatActivity implements OnDishOptionCheckedChangeListener, OnCardSelectedListener {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private ArrayList<String> selectedDishOptions = new ArrayList<>();
    private Map<String, Double> selectedDishPricePortion;
    private Dish dish;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_dish_view);

        firebaseFirestore=FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

//      Backbutton
        MaterialButton backButton = findViewById(R.id.floatingActionCloseButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("dish")) {
            dish = (Dish) intent.getSerializableExtra("dish");

            TextView productName= findViewById(R.id.singleProductName);
            TextView productDescription= findViewById(R.id.singleProductDescription);
            TextView productRating= findViewById(R.id.singleProductRating);
            ImageSlider productImages =findViewById(R.id.dish_card_image_slider);
            RecyclerView productPortionPricesRecyclerView =findViewById(R.id.singleProductPricePortion);
            RecyclerView productOptionsRecyclerView =findViewById(R.id.singleProductOptions);

            assert dish != null;
            productName.setText(dish.getName());
            productDescription.setText(dish.getDescription());
            productRating.setText(dish.getRating()+" Ratings");

//            Product Images
            List<String> savedImages = dish.getImages();
//            Toast.makeText(SingleDishViewActivity.this, "item.getImages()" + dish.getImages().size(), Toast.LENGTH_SHORT).show();
            List<String> imageUrls = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();

            for (int i = 0; i < savedImages.size(); i++) {
                Log.i(MainActivity.class.getName(), "savedImages.get(i)" + savedImages.get(i));
                storage.getReference("dish-images/" + savedImages.get(i)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Convert Uri to URL and add to the list
                        Log.i(MainActivity.class.getName(), "uri" + uri);
                        try {
                            URL url = new URL(uri.toString());
                            imageUrls.add(url.toString());

                            // Check if all URLs have been fetched
                            if (imageUrls.size() == savedImages.size()) {
                                // Proceed with the code that depends on imageUrls
//                                Toast.makeText(SingleDishViewActivity.this, "imageUrls" + imageUrls.size(), Toast.LENGTH_SHORT).show();
                                // Other code using imageUrls can go here
                                SliderAdapter sliderAdapter = new SliderAdapter(SingleDishViewActivity.this, new GlideImageLoaderFactory(),imageUrls,descriptions,"1");
                                productImages.setAdapter(sliderAdapter);
                            }
                        } catch (MalformedURLException e) {
                            // Handle the exception if the conversion fails
                            e.printStackTrace();
                        }
                    }
                });
            }


//            Product Price and Portion
            DishViewPriceViewAdapter dishViewPriceViewAdapter = new DishViewPriceViewAdapter(dish.getPortionPrices(),SingleDishViewActivity.this,this);
//            productPortionPricesRecyclerView.setLayoutManager(new LinearLayoutManager(SingleDishViewActivity.this));
            productPortionPricesRecyclerView.setAdapter(dishViewPriceViewAdapter);

//            Product Options
            DishViewOptionViewAdapter dishViewOptionViewAdapter=new DishViewOptionViewAdapter(dish.getOptions(),this);
            productOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(SingleDishViewActivity.this));
            productOptionsRecyclerView.setAdapter(dishViewOptionViewAdapter);

//          Item Select
            Button decreaseButton = findViewById(R.id.decreaseButton);
            Button increaseButton = findViewById(R.id.increaseButton);
            TextView quantityText = findViewById(R.id.quantityText);

            decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = Integer.parseInt(quantityText.getText().toString());
                    if (quantity > 1) {
                        quantity--;
                        quantityText.setText(String.valueOf(quantity));
                    }
                }
            });

            increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = Integer.parseInt(quantityText.getText().toString());
                    quantity++;
                    quantityText.setText(String.valueOf(quantity));
                }
            });

//          Add to cart
            findViewById(R.id.singleProductAddToCart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView quantityText = findViewById(R.id.quantityText);
                    String quantityString = quantityText.getText().toString();
                    int quantity = Integer.parseInt(quantityString);

                    CartItem cartItem=new CartItem(dish.getId(),selectedDishPricePortion,selectedDishOptions,quantity);

                    SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    String userDocumentId = preferences.getString("userDocumentId", "");

                    firebaseFirestore.collection("users")
                            .document(userDocumentId)
                            .collection("cart")
                            .whereEqualTo("dishId", dish.getId())
                            .whereEqualTo("portionPrices", selectedDishPricePortion)
                            .whereEqualTo("options", selectedDishOptions)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    // Update the quantity
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    CartItem existingCartItem = documentSnapshot.toObject(CartItem.class);
                                    if (existingCartItem != null) {
                                        existingCartItem.updateQuantity(quantity);
                                        firebaseFirestore.collection("users")
                                                .document(userDocumentId)
                                                .collection("cart")
                                                .document(documentSnapshot.getId())
                                                .set(existingCartItem)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Quantity updated successfully
                                                    Toast.makeText(SingleDishViewActivity.this, "Added to Cart Successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(SingleDishViewActivity.this, "Error Occured "+e, Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    // Insert a new item
                                    firebaseFirestore.collection("users")
                                            .document(userDocumentId)
                                            .collection("cart")
                                            .add(cartItem)
                                            .addOnSuccessListener(documentReference -> {
                                                sendNotification();
                                                Toast.makeText(SingleDishViewActivity.this, "Added to Cart Successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SingleDishViewActivity.this, "Error Occured "+e, Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SingleDishViewActivity.this, "Error Occured "+e, Toast.LENGTH_SHORT).show();
                            });

                }
            });


        }

    }

    public void sendNotification(){
        NotificationUtils.sendNotification(SingleDishViewActivity.this,"Added to Cart","Added to Cart Successfully");
    }

    @Override
    public void onDishOptionChecked(String dishOption, boolean isChecked) {
        // Handle the checked state and dishOption in your main activity
        if (isChecked) {
            if (!selectedDishOptions.contains(dishOption)) {
                selectedDishOptions.add(dishOption);
            }
        } else {
            selectedDishOptions.remove(dishOption);
        }

        Log.d("SelectedDishOptions", selectedDishOptions.toString());
    }

    @Override
    public void onItemSelected(Map.Entry<String, Double> selectedItem) {
        if (selectedDishPricePortion == null) {
            // Initialize the map if it's null
            selectedDishPricePortion = new HashMap<>();
        }
        if (selectedItem!=null){
            selectedDishPricePortion.clear();
            selectedDishPricePortion.put(selectedItem.getKey(),selectedItem.getValue());
//            Toast.makeText(SingleDishViewActivity.this, "selectedItem.getKey()"+selectedItem.getKey(), Toast.LENGTH_SHORT).show();
        }
    }

}
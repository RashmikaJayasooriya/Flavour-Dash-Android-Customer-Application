package lk.flavourdash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;

import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Category;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.adapters.DishCardAdapter;
import lk.flavourdash.listeners.OnItemClickListener;

public class ListActivity extends AppCompatActivity implements OnItemClickListener<Category> {
    private FirebaseFirestore firebaseFirestore;
    private String userDocumentId;
    private ArrayList<CartItem> cartItems;
    private FirebaseStorage storage;
    private DishCardAdapter dishCardAdapter;
    private ArrayList<Dish> dishes;
    private BottomSheetDialog bottomSheetDialog;
    public static final String TAG = MainActivity.class.getName();
    private int selectedSortOption = R.id.radioAToZ; // Default to A-z


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        firebaseFirestore=FirebaseFirestore.getInstance();
        SharedPreferences preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        Intent intent=getIntent();

        if(intent.hasExtra("categoryName")){
            Category categoryName = (Category) intent.getSerializableExtra("categoryName");

            TextView listName=findViewById(R.id.textView7);
            listName.setText(categoryName.getName());

            dishes=new ArrayList<>();
            cartItems=new ArrayList<>();
            dishCardAdapter=new DishCardAdapter(dishes,cartItems, ListActivity.this, this);
            setupFirestoreListenerForDish(categoryName.getName());
            setupFirestoreListenerForCart();

            RecyclerView recyclerView=findViewById(R.id.dishListRecycleView);
            recyclerView.setAdapter(dishCardAdapter);
        }


//      Bottom sheet
        Chip chip = findViewById(R.id.chip);

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }
        });

//      Back button
        MaterialButton backButton = findViewById(R.id.listBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showBottomSheet() {
        bottomSheetDialog.show();

        RadioGroup sortRadioGroup = bottomSheetDialog.findViewById(R.id.sortRadioGroup);

        sortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                selectedSortOption = checkedId;
                applySorting();
                bottomSheetDialog.dismiss();
            }
        });
    }


    private void applySorting() {

        if (selectedSortOption == R.id.radioHighToLow) {
            Collections.sort(dishes, (dish1, dish2) -> {
                double price1 = dish1.getPortionPrices().isEmpty() ? 0 : dish1.getPortionPrices().values().iterator().next();
                double price2 = dish2.getPortionPrices().isEmpty() ? 0 : dish2.getPortionPrices().values().iterator().next();
                return Double.compare(price2, price1);
            });
        } else if(selectedSortOption == R.id.radioLowToHigh) {
            Collections.sort(dishes, (dish1, dish2) -> {
                double price1 = dish1.getPortionPrices().isEmpty() ? 0 : dish1.getPortionPrices().values().iterator().next();
                double price2 = dish2.getPortionPrices().isEmpty() ? 0 : dish2.getPortionPrices().values().iterator().next();
                return Double.compare(price1, price2);
            });
        } else if (selectedSortOption == R.id.radioAToZ) {
            Collections.sort(dishes, (dish1, dish2) -> {
                String name1 = dish1.getName();
                String name2 = dish2.getName();
                return name1.compareToIgnoreCase(name2);
            });
        }else if (selectedSortOption==R.id.radioZToA){
            Collections.sort(dishes, (dish1, dish2) -> {
                String name1 = dish1.getName();
                String name2 = dish2.getName();
                return name2.compareToIgnoreCase(name1);
            });

        }else if (selectedSortOption==R.id.radioCategoryAToZ) {
            Collections.sort(dishes, (dish1, dish2) -> {
                String name1 = dish1.getCategory();
                String name2 = dish2.getCategory();
                return name2.compareToIgnoreCase(name2);
            });
        }else {
            Collections.sort(dishes, (dish1, dish2) -> {
                String name1 = dish1.getSubCategory();
                String name2 = dish2.getSubCategory();
                return name2.compareToIgnoreCase(name2);
            });

        }

        // Notify adapter data set changed
        dishCardAdapter.notifyDataSetChanged();
    }


    private void setupFirestoreListenerForDish(String selectedCategoryName) {
        firebaseFirestore.collection("Dishes")
                .whereEqualTo("category", selectedCategoryName)
                .whereEqualTo("availability",true)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting documents: ", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange change : value.getDocumentChanges()) {
                            Dish dish = change.getDocument().toObject(Dish.class);
                            dish.setId(change.getDocument().getId());

                            switch (change.getType()) {
                                case ADDED:
                                    dishes.add(dish);
                                    break;
                                case MODIFIED:
                                    updateModifiedDish(change);
                                    break;
                                case REMOVED:
                                    dishes.removeIf(i -> i.getId().equals(dish.getId()));
                                    break;
                            }
                        }

                        dishCardAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateModifiedDish(DocumentChange change) {
        Dish updatedDish = change.getDocument().toObject(Dish.class);
        updatedDish.setId(change.getDocument().getId());

        for (int i = 0; i < dishes.size(); i++) {
            Dish existingDish = dishes.get(i);

            if (existingDish.getId().equals(updatedDish.getId())) {
                // Update only the fields that have changed
                if (!existingDish.getName().equals(updatedDish.getName())) {
                    existingDish.setName(updatedDish.getName());
                }
                if (!existingDish.getImages().equals(updatedDish.getImages())) {
                    existingDish.setImages(updatedDish.getImages());
                }
                if (!existingDish.getRating().equals(updatedDish.getRating())) {
                    existingDish.setRating(updatedDish.getRating());
                }
                if (!existingDish.getCategory().equals(updatedDish.getCategory())) {
                    existingDish.setCategory(updatedDish.getCategory());
                }
                if (!existingDish.getPortionPrices().equals(updatedDish.getPortionPrices())) {
                    existingDish.setPortionPrices(updatedDish.getPortionPrices());
                }

                dishCardAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void setupFirestoreListenerForCart() {

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
                            updateModifiedCart(change);
                            break;
                        case REMOVED:
                            cartItems.removeIf(i -> i.getId().equals(cartItem.getId()));
                            break;
                    }
                }
                dishCardAdapter.notifyDataSetChanged();
            }
        });
    }


    private void updateModifiedCart(DocumentChange change) {
        CartItem updatedCartItem = change.getDocument().toObject(CartItem.class);
        updatedCartItem.setId(change.getDocument().getId());

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem existingCartItem = cartItems.get(i);

            if (existingCartItem.getId().equals(updatedCartItem.getId())) {
                // Update only the fields that have changed
                if (existingCartItem.getNoOfItems() != updatedCartItem.getNoOfItems()) {
                    existingCartItem.setNoOfItems(updatedCartItem.getNoOfItems());
                }
                if (!existingCartItem.getOptions().equals(updatedCartItem.getOptions())) {
                    existingCartItem.setOptions(updatedCartItem.getOptions());
                }
                if (!existingCartItem.getPortionPrices().equals(updatedCartItem.getPortionPrices())) {
                    existingCartItem.setPortionPrices(updatedCartItem.getPortionPrices());
                }

                dishCardAdapter.notifyItemChanged(i);
                break;
            }
        }
    }



    @Override
    public void onItemClick(Category category) {
//        CategoryAdapter dialog = CategoryAdapter.newInstance(category);
//        dialog.show(getChildFragmentManager(), "FullScreenDialog");
    }
}
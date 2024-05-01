package lk.flavourdash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Category;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.adapters.CategoryAdapter;
import lk.flavourdash.adapters.CategoryCardAdapter;
import lk.flavourdash.listeners.OnItemClickListener;

public class HomeMainFragment extends Fragment implements OnItemClickListener<Category> {
    private FirebaseFirestore firebaseFirestore;
    private String userDocumentId;
    private ArrayList<CartItem> cartItems;
    private ArrayList<Category> categories;
    private ArrayList<Category> categories2;
    private ArrayList<Dish> dishes;

    public static final String TAG = MainActivity.class.getName();
    private CategoryAdapter categoryAdapter;
    private CategoryCardAdapter categoryCardAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences preferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userDocumentId = preferences.getString("userDocumentId", "");

        // Menu
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categories, getContext(), this);
        RecyclerView menuRecycleView = view.findViewById(R.id.menuRecycleView);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
        menuRecycleView.setLayoutManager(layoutManager);
        menuRecycleView.setAdapter(categoryAdapter);

        // Category Card View
        categories2 = new ArrayList<>();
        dishes = new ArrayList<>();
        cartItems=new ArrayList<>();
        categoryCardAdapter = new CategoryCardAdapter(categories2, dishes, cartItems, getContext(), this);
        RecyclerView allCategoryCardRecycleView = view.findViewById(R.id.allCategoryCardRecycleView);
        allCategoryCardRecycleView.setAdapter(categoryCardAdapter);

        setupFirestoreListenerForCategory();
        setupFirestoreListenerForCart();

    }

    private void setupFirestoreListenerForCategory() {
        firebaseFirestore.collection("category").whereEqualTo("active",true).orderBy("name").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {

                for (DocumentChange change : value.getDocumentChanges()) {
                    Category category = change.getDocument().toObject(Category.class);
                    category.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            categories.add(category);
                            categories2.add(category);
                            fetchDishesForCategory(category);
                            break;
                        case MODIFIED:
                            updateModifiedCategory(change);
                            break;
                        case REMOVED:
                            categories.removeIf(i -> i.getId().equals(category.getId()));
                            categories2.removeIf(i -> i.getId().equals(category.getId()));
                            break;
                    }
                }

                categoryAdapter.notifyDataSetChanged();
                categoryCardAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchDishesForCategory(Category category) {
        firebaseFirestore.collection("Dishes")
                .whereEqualTo("category", category.getName())
                .whereEqualTo("availability",true)
                .orderBy("name")
                .limit(3)
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
                                    if (!dishes.stream().anyMatch(i -> i.getId().equals(dish.getId()))) {
                                        dishes.add(dish);
                                    }
                                    break;
                                case MODIFIED:
                                    updateModifiedDish(change);
                                    break;
                                case REMOVED:
                                    dishes.removeIf(i -> i.getId().equals(dish.getId()));
                                    break;
                            }
                        }

                        categoryCardAdapter.notifyDataSetChanged();
                    }
                });
    }


    private void updateModifiedCategory(DocumentChange change) {
        Category updatedCategory = change.getDocument().toObject(Category.class);
        updatedCategory.setId(change.getDocument().getId());

        for (int i = 0; i < categories.size(); i++) {
            Category existingCategory = categories.get(i);

            if (existingCategory.getId().equals(updatedCategory.getId())) {
                existingCategory.setName(updatedCategory.getName());
                existingCategory.setImage(updatedCategory.getImage());
                categoryAdapter.notifyItemChanged(i);
                break;
            }
        }

        for (int i = 0; i < categories2.size(); i++) {
            Category existingCategory = categories2.get(i);

            if (existingCategory.getId().equals(updatedCategory.getId())) {
                existingCategory.setName(updatedCategory.getName());
                existingCategory.setImage(updatedCategory.getImage());
                categoryCardAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void updateModifiedDish(DocumentChange change) {
        Dish updatedDish = change.getDocument().toObject(Dish.class);
        updatedDish.setId(change.getDocument().getId());

        for (int i = 0; i < dishes.size(); i++) {
            Dish existingDish = dishes.get(i);

//            if (existingDish.getId().equals(updatedDish.getId())) {
//                existingDish.setName(updatedDish.getName());
//                existingDish.setImages(updatedDish.getImages());
//                existingDish.setRating(updatedDish.getRating());
//                existingDish.setCategory(updatedDish.getCategory());
//                existingDish.setPortionPrices(updatedDish.getPortionPrices());
//
//                categoryCardAdapter.notifyItemChanged(i);
//                break;
//            }

            if (existingDish.getId().equals(updatedDish.getId())) {
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
                if (!existingDish.getAvailability().equals(updatedDish.getAvailability())) {
                    existingDish.setAvailability(updatedDish.getAvailability());
                }

                // Notify the adapter that the item at position i has been modified
                categoryCardAdapter.notifyItemChanged(i);
                break; // Exit the loop since we found the matching category
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
                categoryCardAdapter.notifyDataSetChanged();
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

                // Notify the adapter that the item at position i has been modified
                categoryCardAdapter.notifyItemChanged(i);
                break; // Exit the loop since we found the matching category
            }
        }
    }

    @Override
    public void onItemClick(Category category) {
        // Handle item click
    }
}

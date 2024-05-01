package lk.flavourdash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.flavourdash.Model.Category;
import lk.flavourdash.Model.Order;

public class SearchActivity extends AppCompatActivity {
    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private Map<String,Category> categories;
    private List<String> suggestions;
    private FirebaseFirestore firebaseFirestore;
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        firebaseFirestore=FirebaseFirestore.getInstance();

//      Backbutton
        MaterialButton backButton = findViewById(R.id.SearchBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


//      Search
        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);


        categories=new HashMap<>();
        suggestions = new ArrayList<>();
//        suggestions.add("Apple");
//        suggestions.add("Banana");
//        suggestions.add("Orange");
//        suggestions.add("Mango");
//        suggestions.add("Grapes");
        setupFirestoreListenerForSuggestions();


        adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, suggestions);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener((parent, view, position, id) -> {
            String suggestionName = adapter.getItem(position);
            Category category = categories.get(suggestionName);


            // Create an intent and pass the categoryId to the next activity
            Intent intent = new Intent(view.getContext(), ListActivity.class);
            intent.putExtra("categoryName", category);
            startActivity(intent);
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    listView.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.GONE);
                }
            }
        });



    }


    private void setupFirestoreListenerForSuggestions() {
        firebaseFirestore.collection("category").addSnapshotListener((value, error) -> {
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
                            categories.put(category.getName(), category);
                            suggestions.add(category.getName());
                            break;
                        case MODIFIED:
                            updateModifiedCategory(change);
                            break;
                        case REMOVED:
                            categories.remove(category.getName());
                            suggestions.remove(category.getName());
                            break;
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateModifiedCategory(DocumentChange change) {
        Category updatedCategory = change.getDocument().toObject(Category.class);
        updatedCategory.setId(change.getDocument().getId());

        String categoryName = updatedCategory.getName();

        if (categories.containsKey(categoryName)) {
            // Update only the fields that have changed
            Category existingCategory = categories.get(categoryName);
            if (!existingCategory.getName().equals(updatedCategory.getName())) {
                existingCategory.setName(updatedCategory.getName());
            }

            // Notify the adapter that the item has been modified
            adapter.notifyDataSetChanged();
        }
    }


}
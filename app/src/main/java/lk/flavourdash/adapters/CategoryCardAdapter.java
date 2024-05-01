package lk.flavourdash.adapters;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import javax.annotation.Nonnull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.flavourdash.MainActivity;
import lk.flavourdash.Model.CartItem;
import lk.flavourdash.Model.Category;
import lk.flavourdash.Model.Dish;
import lk.flavourdash.R;
import lk.flavourdash.listeners.OnItemClickListener;

public class CategoryCardAdapter extends RecyclerView.Adapter<CategoryCardAdapter.MyViewHolder> {

    public static final String TAG = MainActivity.class.getName();
    private ArrayList<Category> data;
    private ArrayList<Dish> dishes;
    ArrayList<CartItem> cartItems;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private DishCardAdapter dishCardAdapter;

    private OnItemClickListener listener;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });


    public CategoryCardAdapter(ArrayList<Category> data, ArrayList<Dish> dishes, ArrayList<CartItem> cartItems, Context context, OnItemClickListener listener) {
        this.data = data;
        this.dishes=dishes;
        this.cartItems=cartItems;
        this.context = context;
        this.listener = listener;
        this.storage = FirebaseStorage.getInstance();
        this.firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card_view_home, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Category item = data.get(position);

        ArrayList<Dish> filteredDishes = new ArrayList<>();
        for (Dish dish : dishes) {
            if (dish.getCategory().equals(item.getName())) {
                filteredDishes.add(dish);
            }
        }

        if (!filteredDishes.isEmpty()) {
            holder.cardCategoryName.setText(item.getName());
            holder.cardCategoryName.setVisibility(View.VISIBLE);
            holder.cardCategoryViewAllBtn.setVisibility(View.VISIBLE);

            dishCardAdapter = new DishCardAdapter(filteredDishes,cartItems, context.getApplicationContext(), listener);

            LinearLayoutManager linearLayout = new LinearLayoutManager(context);
            holder.cardCategoryRecyclerView.setLayoutManager(linearLayout);
            holder.cardCategoryRecyclerView.setAdapter(dishCardAdapter);
        }
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView cardCategoryName;
        Button cardCategoryViewAllBtn;
        RecyclerView cardCategoryRecyclerView;

        MyViewHolder(View itemView) {
            super(itemView);
            cardCategoryName=itemView.findViewById(R.id.cardCategoryName);
            cardCategoryViewAllBtn=itemView.findViewById(R.id.cardCategoryViewAllBtn);
            cardCategoryRecyclerView=itemView.findViewById(R.id.cardCategoryRecyclerView);
        }
    }

}

package lk.flavourdash.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import com.google.firebase.storage.FirebaseStorage;

import lk.flavourdash.ListActivity;
import lk.flavourdash.Model.Category;
import lk.flavourdash.R;
import lk.flavourdash.listeners.OnItemClickListener;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private ArrayList<Category> data;
    private Context context;
    private FirebaseStorage storage;

    private OnItemClickListener listener;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });


    public CategoryAdapter(ArrayList<Category> data, Context context, OnItemClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
        this.storage = FirebaseStorage.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_view_home, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Category item = data.get(position);

        holder.categoryNameView.setText(item.getName());
        storage.getReference("category-images/" + item.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(holder.categoryImageView.getContext()).load(uri).override(150, 150) .centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(holder.categoryImageView);
            }
        });

        holder.categoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getContext() != null) {
                    Intent intent = new Intent(view.getContext(), ListActivity.class);
                    intent.putExtra("categoryName",item);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                } else {
                    Log.e("CategoryAdapter", "Context is null");
                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImageView;
        TextView categoryNameView;

        MyViewHolder(View itemView) {
            super(itemView);
            categoryNameView=itemView.findViewById(R.id.categoryNameView);
            categoryImageView=itemView.findViewById(R.id.categoryImageView);
        }
    }
}

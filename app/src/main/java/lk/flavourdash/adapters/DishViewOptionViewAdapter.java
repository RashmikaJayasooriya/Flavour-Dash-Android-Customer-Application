package lk.flavourdash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

import lk.flavourdash.R;
import lk.flavourdash.listeners.OnDishOptionCheckedChangeListener;

public class DishViewOptionViewAdapter extends RecyclerView.Adapter<DishViewOptionViewAdapter.ViewHolder> {

    private List<String> dishOptions;
    private OnDishOptionCheckedChangeListener listener;

    public DishViewOptionViewAdapter(List<String> dishOptions,OnDishOptionCheckedChangeListener listener) {
        this.dishOptions = dishOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_option_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.checkBox.setText(dishOptions.get(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Notify the main activity when the checkbox state changes
            if (listener != null) {
                listener.onDishOptionChecked(dishOptions.get(position), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkBox;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.optionsCheckBox);
        }
    }

    public void clearData() {
        if (dishOptions != null) {
            dishOptions.clear();
            notifyDataSetChanged();
        }
    }
}

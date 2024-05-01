package lk.flavourdash.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lk.flavourdash.R;
import lk.flavourdash.listeners.OnCardSelectedListener;

public class DishViewPriceViewAdapter extends RecyclerView.Adapter<DishViewPriceViewAdapter.MyViewHolder> {

    private List<LinkedHashMap.Entry<String, Double>> data;
    private Context context;
    private int selectedItemPosition = RecyclerView.NO_POSITION;
    private OnCardSelectedListener listener;

    public DishViewPriceViewAdapter(Map<String, Double> data, Context context, OnCardSelectedListener listener) {
        this.data = new java.util.ArrayList<>(data.entrySet());
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_view_price_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Map.Entry<String, Double> item = data.get(position);

        holder.dishPortionName.setText(item.getKey());
        holder.dishPortionPrice.setText(String.valueOf(item.getValue()));

        if (position == selectedItemPosition) {
            Drawable selectedDrawable = ContextCompat.getDrawable(context, R.drawable.portion_price_select_highlight);
            holder.dishPricePortionCard.setForeground(getLayerDrawable(selectedDrawable));
        } else {
            // Reset foreground
            holder.dishPricePortionCard.setForeground(null);
        }

        // item click listener
        holder.itemView.setOnClickListener(v -> {
            // Update selected item position
            int previousSelectedItem = selectedItemPosition;
            selectedItemPosition = position;

            notifyItemChanged(previousSelectedItem);
            notifyItemChanged(selectedItemPosition);

            if (listener != null) {
                listener.onItemSelected(item);
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dishPortionName;
        TextView dishPortionPrice;
        CardView dishPricePortionCard;
        LinearLayout dishPricePortion;

        MyViewHolder(View itemView) {
            super(itemView);
            dishPortionName = itemView.findViewById(R.id.dishPortionName);
            dishPortionPrice = itemView.findViewById(R.id.dishPortionPrice);
            dishPricePortionCard = itemView.findViewById(R.id.dishPricePortionCard);

        }
    }

    private Drawable getLayerDrawable(Drawable... layers) {
        return new LayerDrawable(layers);
    }
}


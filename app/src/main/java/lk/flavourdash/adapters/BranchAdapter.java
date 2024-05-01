package lk.flavourdash.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import lk.flavourdash.Model.Branch;
import lk.flavourdash.R;
import lk.flavourdash.listeners.OnItemClickListener;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.MyViewHolder> {

    private ArrayList<Branch> data;
    private Context context;

    private OnItemClickListener listener;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });


    public BranchAdapter(ArrayList<Branch> data, Context context,OnItemClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.branch_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Branch item = data.get(position);

        holder.branchName.setText(item.getName());

        if (item.getLatitude() != null && item.getLongitude() != null) {

            Geocoder geocoder = new Geocoder(context.getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        item.getLatitude(), item.getLongitude(), 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String selectedAddress = address.getAddressLine(0);
                    holder.branchAddress.setText(selectedAddress);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            holder.branchAddress.setText("Address not available");
        }

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    // A double tap has occurred
                    Branch branch = data.get(holder.getAdapterPosition());
                    Toast.makeText(context, branch.getName().toString(), Toast.LENGTH_SHORT).show();
                    listener.onItemClick(branch);
                }
                return true;
            }
        });
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView branchName;
        TextView branchAddress;

        MyViewHolder(View itemView) {
            super(itemView);
            branchName=itemView.findViewById(R.id.branchName);
            branchAddress=itemView.findViewById(R.id.branchAddress);
        }
    }
}

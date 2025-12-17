package com.example.assignment1;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ComputerAdapter extends RecyclerView.Adapter<ComputerAdapter.ComputerViewHolder> {

    private Context context;
    private List<Computer> computerList;
    private OnComputerClickListener listener;

    public interface OnComputerClickListener {
        void onComputerClick(long id);
    }

    public ComputerAdapter(Context context, List<Computer> computerList, OnComputerClickListener listener) {
        this.context = context;
        this.computerList = computerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComputerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_computer, parent, false);
        return new ComputerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComputerViewHolder holder, int position) {
        Computer computer = computerList.get(position);
        holder.modelName.setText(computer.getModel());
        holder.brandName.setText(computer.getBrandName());
        holder.serialNumber.setText("(" + computer.getSerialNumber() + ")"); // Parentheses for style

        if (computer.getDateAdded() != null) {
            holder.dateAdded.setText(computer.getDateAdded());
        } else {
            holder.dateAdded.setText("-");
        }

        // New Fields
        if (holder.location != null) {
            holder.location.setText("Location: " + computer.getLocation());
        }

        if (holder.status != null) {
            holder.status.setText(computer.getStatus());
            // Optional: Change color based on status
            if ("Inactive".equalsIgnoreCase(computer.getStatus())) {
                holder.status.setTextColor(android.graphics.Color.RED);
            } else {
                holder.status.setTextColor(context.getResources().getColor(R.color.teal_700));
            }
        }

        // Image handling removed/hidden
        // holder.image.setImageResource(R.mipmap.ic_launcher);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComputerClick(computer.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return computerList.size();
    }

    public void updateData(List<Computer> newComputers) {
        this.computerList = newComputers;
        notifyDataSetChanged();
    }

    static class ComputerViewHolder extends RecyclerView.ViewHolder {
        TextView modelName, brandName, serialNumber, dateAdded, location, status;
        // ImageView image; // Removed

        public ComputerViewHolder(@NonNull View itemView) {
            super(itemView);
            modelName = itemView.findViewById(R.id.computer_model);
            brandName = itemView.findViewById(R.id.computer_brand);
            serialNumber = itemView.findViewById(R.id.computer_serial);
            dateAdded = itemView.findViewById(R.id.computer_date);
            location = itemView.findViewById(R.id.computer_location);
            status = itemView.findViewById(R.id.computer_status);
            // image = itemView.findViewById(R.id.computer_image);
        }
    }
}

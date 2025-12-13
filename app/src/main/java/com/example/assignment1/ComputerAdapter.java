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
        holder.price.setText(String.format("$%.2f", computer.getPrice()));

        if (computer.getImageUri() != null && !computer.getImageUri().isEmpty()) {
            try {
                holder.image.setImageURI(Uri.parse(computer.getImageUri()));
            } catch (Exception e) {
                holder.image.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            holder.image.setImageResource(R.mipmap.ic_launcher);
        }

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
        TextView modelName, brandName, price;
        ImageView image;

        public ComputerViewHolder(@NonNull View itemView) {
            super(itemView);
            modelName = itemView.findViewById(R.id.computer_model);
            brandName = itemView.findViewById(R.id.computer_brand);
            price = itemView.findViewById(R.id.computer_price);
            image = itemView.findViewById(R.id.computer_image);
        }
    }
}

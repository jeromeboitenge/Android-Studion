package com.example.assignment1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.ViewHolder> {

    public interface OnItemDeleteListener {
        void onDeleteClick(long id);
    }

    private List<Machine> machineList;
    private OnItemDeleteListener deleteListener;

    public NetworkAdapter(List<Machine> machineList, OnItemDeleteListener deleteListener) {
        this.machineList = machineList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Machine machine = machineList.get(position);
        holder.tvName.setText(machine.getName());
        holder.tvSerial.setText("SN: " + machine.getSerialNumber());
        holder.tvLocation.setText(machine.getLocation());
        holder.tvStatus.setText(machine.getStatus());

        if (machine.getDateAdded() != null && !machine.getDateAdded().isEmpty()) {
            holder.tvDate.setText(machine.getDateAdded());
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        if (machine.getBrandName() != null) {
            holder.tvBrand.setText("Brand: " + machine.getBrandName());
            holder.tvBrand.setVisibility(View.VISIBLE);
        } else {
            holder.tvBrand.setVisibility(View.GONE);
        }

        // Basic status styling
        if ("Active".equalsIgnoreCase(machine.getStatus())) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#43A047")); // Green
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Red
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(machine.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return machineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSerial, tvLocation, tvStatus, tvDate, tvBrand;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_machine_name);
            tvSerial = itemView.findViewById(R.id.tv_serial);
            tvBrand = itemView.findViewById(R.id.tv_brand); // New
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_machine);
        }
    }
}

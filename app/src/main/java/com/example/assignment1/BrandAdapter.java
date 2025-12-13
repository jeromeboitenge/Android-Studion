package com.example.assignment1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private List<Brand> brandList;
    private OnBrandActionListener listener;

    public interface OnBrandActionListener {
        void onEdit(Brand brand);

        void onDelete(Brand brand);
    }

    public BrandAdapter(List<Brand> brandList, OnBrandActionListener listener) {
        this.brandList = brandList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brandList.get(position);
        holder.name.setText(brand.getName());
        holder.description.setText(brand.getDescription());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null)
                listener.onEdit(brand);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null)
                listener.onDelete(brand);
        });
    }

    @Override
    public int getItemCount() {
        return brandList.size();
    }

    public void updateData(List<Brand> newBrands) {
        this.brandList = newBrands;
        notifyDataSetChanged();
    }

    static class BrandViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;
        ImageButton btnEdit, btnDelete;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_brand_name);
            description = itemView.findViewById(R.id.tv_brand_desc);
            btnEdit = itemView.findViewById(R.id.btn_edit_brand);
            btnDelete = itemView.findViewById(R.id.btn_delete_brand);
        }
    }
}

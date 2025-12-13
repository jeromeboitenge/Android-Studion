package com.example.assignment1;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentDetails extends Fragment {

    private static final String ARG_COMPUTER_ID = "computer_id";
    private long computerId;
    private DatabaseHelper dbHelper;
    private Computer computer;

    public interface OnComputerActionListener {
        void onEditComputer(long id);

        void onDeleteComputer();
    }

    private OnComputerActionListener listener;

    public static FragmentDetails newInstance(long computerId) {
        FragmentDetails fragment = new FragmentDetails();
        Bundle args = new Bundle();
        args.putLong(ARG_COMPUTER_ID, computerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnComputerActionListener) {
            listener = (OnComputerActionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnComputerActionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            computerId = getArguments().getLong(ARG_COMPUTER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        computer = dbHelper.getComputer(computerId);

        if (computer == null) {
            Toast.makeText(getContext(), "Error loading computer details", Toast.LENGTH_SHORT).show();
            if (listener != null)
                listener.onDeleteComputer(); // Close fragment
            return;
        }

        ImageView ivImage = view.findViewById(R.id.iv_detail_image);
        TextView tvModel = view.findViewById(R.id.tv_detail_model);
        TextView tvBrand = view.findViewById(R.id.tv_detail_brand);
        TextView tvPrice = view.findViewById(R.id.tv_detail_price);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        CheckBox cbLaptop = view.findViewById(R.id.cb_detail_laptop);
        Button btnDelete = view.findViewById(R.id.btn_detail_delete);
        Button btnEdit = view.findViewById(R.id.btn_detail_edit);

        tvModel.setText(computer.getModel());
        tvBrand.setText(computer.getBrandName());
        tvPrice.setText(String.format(Locale.getDefault(), "RWF %,.0f", computer.getPrice()));
        tvDate.setText(computer.getPurchaseDate());
        cbLaptop.setChecked(computer.isLaptop());

        if (computer.getImageUri() != null && !computer.getImageUri().isEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(computer.getImageUri()));
            } catch (Exception e) {
                ivImage.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            ivImage.setImageResource(R.mipmap.ic_launcher);
        }

        btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditComputer(computerId);
            }
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    dbHelper.deleteComputer(computerId);
                    Toast.makeText(getContext(), "Computer deleted", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onDeleteComputer();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}

package com.example.assignment1;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class FragmentDetails extends Fragment {

    private static final String ARG_COMPUTER_ID = "computer_id";
    private long computerId;
    private DatabaseHelper dbHelper;
    private OnComputerActionListener listener;

    public interface OnComputerActionListener {
        void onEditComputer(long id);

        void onDeleteComputer();
    }

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
            throw new RuntimeException(context.toString()
                    + " must implement OnComputerActionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            computerId = getArguments().getLong(ARG_COMPUTER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        TextView tvModel = view.findViewById(R.id.tv_detail_model);
        TextView tvBrand = view.findViewById(R.id.tv_detail_brand);
        TextView tvSerial = view.findViewById(R.id.tv_detail_serial); // New
        TextView tvLocation = view.findViewById(R.id.tv_detail_location); // New
        TextView tvDate = view.findViewById(R.id.tv_detail_date); // New
        TextView tvStatus = view.findViewById(R.id.tv_detail_status); // New
        ImageView ivImage = view.findViewById(R.id.iv_detail_image);
        Button btnEdit = view.findViewById(R.id.btn_edit);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        Computer computer = dbHelper.getComputer(computerId);

        if (computer != null) {
            tvModel.setText(computer.getModel());
            tvBrand.setText(computer.getBrandName());
            tvSerial.setText(computer.getSerialNumber());
            tvLocation.setText(computer.getLocation());
            tvStatus.setText(computer.getStatus());

            if (computer.getDateAdded() != null) {
                tvDate.setText(computer.getDateAdded());
            } else {
                tvDate.setText("-");
            }

            // Color status logic optional
            if ("Inactive".equalsIgnoreCase(computer.getStatus())) {
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvStatus.setTextColor(getResources().getColor(R.color.success_color)); // or specific green
            }

            if (computer.getImageUri() != null && !computer.getImageUri().isEmpty()) {
                try {
                    ivImage.setImageURI(Uri.parse(computer.getImageUri()));
                } catch (Exception e) {
                    ivImage.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                ivImage.setImageResource(R.mipmap.ic_launcher);
            }
        }

        btnEdit.setOnClickListener(v -> listener.onEditComputer(computerId));

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Machine")
                .setMessage("Are you sure you want to delete this machine?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteComputer(computerId);
                    Toast.makeText(requireContext(), "Machine Deleted", Toast.LENGTH_SHORT).show();
                    listener.onDeleteComputer();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

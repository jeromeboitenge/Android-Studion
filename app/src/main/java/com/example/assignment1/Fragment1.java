package com.example.assignment1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Fragment1 extends Fragment {

    private EditText etModel, etSerial, etLocation, etDate;
    private Spinner spinnerBrand;
    private ImageButton btnAddBrand;
    private ImageView ivPreview;
    private Button btnSave, btnPickImage;
    private Button btnCancel;
    private RadioGroup rgStatus;
    private RadioButton rbActive, rbInactive;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private Uri tempImageUri; // For Camera
    private List<Brand> brandList;
    private Calendar calendar;

    private static final String ARG_COMPUTER_ID = "computer_id";
    private long computerId = -1;
    private boolean isEditMode = false;

    public static Fragment1 newInstance(long computerId) {
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        args.putLong(ARG_COMPUTER_ID, computerId);
        fragment.setArguments(args);
        return fragment;
    }

    // Gallery Launcher
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                    }
                }
            });

    // Camera Launcher
    private final ActivityResultLauncher<Uri> mTakePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean success) {
                    if (success) {
                        selectedImageUri = tempImageUri;
                        ivPreview.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            computerId = getArguments().getLong(ARG_COMPUTER_ID, -1);
            isEditMode = computerId != -1;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_1, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        calendar = Calendar.getInstance();

        etModel = view.findViewById(R.id.et_model);
        etSerial = view.findViewById(R.id.et_serial);
        etLocation = view.findViewById(R.id.et_location);
        etDate = view.findViewById(R.id.et_date); // Bind Date Field
        spinnerBrand = view.findViewById(R.id.spinner_brand);
        btnAddBrand = view.findViewById(R.id.btn_add_brand_inline);
        ivPreview = view.findViewById(R.id.iv_preview);
        btnSave = view.findViewById(R.id.btn_save);
        btnPickImage = view.findViewById(R.id.btn_pick_image);
        btnCancel = view.findViewById(R.id.btn_cancel);
        rgStatus = view.findViewById(R.id.rg_status);
        rbActive = view.findViewById(R.id.rb_active);
        rbInactive = view.findViewById(R.id.rb_inactive);

        // Date Picker Logic
        etDate.setOnClickListener(v -> showDatePicker());
        // Init with today's date if empty
        if (etDate.getText().toString().isEmpty()) {
            updateLabel();
        }

        loadBrands();

        if (isEditMode) {
            loadComputerData();
            btnSave.setText(R.string.btn_update);
        }

        btnAddBrand.setOnClickListener(v -> showAddBrandDialog());

        btnPickImage.setOnClickListener(v -> showImageSourceDialog());

        btnSave.setOnClickListener(v -> saveComputer());

        btnCancel.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });

        return view;
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        };
        new DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void loadComputerData() {
        Computer computer = dbHelper.getComputer(computerId);
        if (computer != null) {
            etModel.setText(computer.getModel());
            etSerial.setText(computer.getSerialNumber());
            etLocation.setText(computer.getLocation());
            if (computer.getDateAdded() != null) {
                etDate.setText(computer.getDateAdded());
            }

            // Set Status
            if ("Inactive".equalsIgnoreCase(computer.getStatus())) {
                rbInactive.setChecked(true);
            } else {
                rbActive.setChecked(true);
            }

            // Set Spinner Selection (Safe check)
            if (brandList != null) {
                for (int i = 0; i < brandList.size(); i++) {
                    if (brandList.get(i).getId() == computer.getBrandId()) {
                        spinnerBrand.setSelection(i);
                        break;
                    }
                }
            }

            // Load Image
            if (computer.getImageUri() != null && !computer.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(computer.getImageUri());
                try {
                    ivPreview.setImageURI(selectedImageUri);
                } catch (Exception e) {
                    ivPreview.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }

    private void showImageSourceDialog() {
        String[] options = { getString(R.string.option_camera), getString(R.string.option_gallery) };
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.title_select_image_source));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();
                } else {
                    mGetContent.launch("image/*");
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            tempImageUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    photoFile);
            mTakePicture.launch(tempImageUri);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
    }

    private void loadBrands() {
        brandList = dbHelper.getAllBrands();
        if (brandList.isEmpty()) {
            // Optional: Provide a hint or force add a default brand if none exist
            // But seeding in DB helper should handle this.
            Log.w("Fragment1", "Brand list is empty after load");
        }
        ArrayAdapter<Brand> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, brandList);
        spinnerBrand.setAdapter(adapter);
    }

    private void showAddBrandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Brand");

        final EditText input = new EditText(requireContext());
        input.setHint("Brand Name");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newBrandName = input.getText().toString().trim();
            if (!newBrandName.isEmpty()) {
                if (dbHelper.isBrandExists(newBrandName)) {
                    Toast.makeText(requireContext(), "Brand already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    long id = dbHelper.addBrand(newBrandName, "Added via Add Computer");
                    if (id != -1) {
                        Toast.makeText(requireContext(), "Brand Added!", Toast.LENGTH_SHORT).show();
                        loadBrands(); // Refresh spinner
                        // Select new brand
                        for (int i = 0; i < brandList.size(); i++) {
                            if (brandList.get(i).getId() == id) {
                                spinnerBrand.setSelection(i);
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error adding brand", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Brand name required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveComputer() {
        try {
            String model = etModel.getText().toString().trim();
            String serial = etSerial.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String dateAdded = etDate.getText().toString().trim();

            // Safety check for spinner
            Brand selectedBrand = null;
            Object selectedItem = spinnerBrand.getSelectedItem();
            if (selectedItem instanceof Brand) {
                selectedBrand = (Brand) selectedItem;
            } else if (selectedItem != null) {
                Log.e("Fragment1", "Spinner item is not a Brand object: " + selectedItem.getClass().getName());
            }

            // Handle Status
            String status = "Active";
            if (rbInactive != null && rbInactive.isChecked()) {
                status = "Inactive";
            }

            if (model.isEmpty() || serial.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields: Name, Serial, Location", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            if (selectedBrand == null) {
                Toast.makeText(requireContext(), "Please select a brand. Add one if list is empty.", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            if (isEditMode) {
                // Update with Date
                Computer computer = new Computer(computerId, model, serial, location, imageUriString,
                        selectedBrand.getId(), dateAdded);
                computer.setStatus(status);

                int result = dbHelper.updateComputer(computer);
                if (result > 0) {
                    Toast.makeText(requireContext(), "Machine Updated!", Toast.LENGTH_SHORT).show();
                    if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                        getParentFragmentManager().popBackStack();
                    } else {
                        requireActivity().finish();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error updating machine: DB Update Failed", Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                // Add with Date
                Computer computer = new Computer(model, serial, location, imageUriString, selectedBrand.getId(),
                        dateAdded);
                computer.setStatus(status);

                long result = dbHelper.addComputer(computer);

                if (result != -1) {
                    Toast.makeText(requireContext(), "Machine Saved!", Toast.LENGTH_SHORT).show();
                    requireActivity().finish(); // Go back to Activity2
                } else {
                    Log.e("Fragment1", "Failed to add machine to DB");
                    Toast.makeText(requireContext(), "DB Error: Could not save machine.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("Fragment1", "CRITICAL ERROR in saveComputer", e);
            String errorMsg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            Toast.makeText(requireContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }
}

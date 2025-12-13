package com.example.assignment1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

    private EditText etModel, etPrice;
    private TextView tvDate;
    private Spinner spinnerBrand;
    private CheckBox cbLaptop;
    private ImageView ivPreview;
    private Button btnSave, btnPickImage;
    private Button btnCancel;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private Uri tempImageUri; // For Camera
    private List<Brand> brandList;

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
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
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
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
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

        etModel = view.findViewById(R.id.et_model);
        etPrice = view.findViewById(R.id.et_price);
        tvDate = view.findViewById(R.id.tv_date);
        spinnerBrand = view.findViewById(R.id.spinner_brand);
        cbLaptop = view.findViewById(R.id.cb_laptop);
        ivPreview = view.findViewById(R.id.iv_preview);
        btnSave = view.findViewById(R.id.btn_save);
        btnPickImage = view.findViewById(R.id.btn_pick_image);
        btnCancel = view.findViewById(R.id.btn_cancel);

        loadBrands();
        setupDatePicker();

        if (isEditMode) {
            loadComputerData();
            btnSave.setText(R.string.btn_update);
        }

        btnPickImage.setOnClickListener(v -> showImageSourceDialog());

        btnSave.setOnClickListener(v -> saveComputer());

        btnCancel.setOnClickListener(v -> {
            if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });

        return view;
    }

    private void loadComputerData() {
        Computer computer = dbHelper.getComputer(computerId);
        if (computer != null) {
            etModel.setText(computer.getModel());
            etPrice.setText(String.valueOf(computer.getPrice()));
            tvDate.setText(computer.getPurchaseDate());
            cbLaptop.setChecked(computer.isLaptop());

            // Set Spinner Selection
            for (int i = 0; i < brandList.size(); i++) {
                if (brandList.get(i).getId() == computer.getBrandId()) {
                    spinnerBrand.setSelection(i);
                    break;
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
        ArrayAdapter<Brand> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, brandList);
        spinnerBrand.setAdapter(adapter);
    }

    private void setupDatePicker() {
        tvDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1,
                                dayOfMonth);
                        tvDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveComputer() {
        String model = etModel.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String date = tvDate.getText().toString().trim();
        boolean isLaptop = cbLaptop.isChecked();
        Brand selectedBrand = (Brand) spinnerBrand.getSelectedItem();

        if (model.isEmpty() || priceStr.isEmpty() || date.equals(getString(R.string.hint_date))) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        if (isEditMode) {
            Computer computer = new Computer(computerId, model, price, date, isLaptop, imageUriString,
                    selectedBrand.getId());
            int result = dbHelper.updateComputer(computer);
            if (result > 0) {
                Toast.makeText(requireContext(), "Computer Updated!", Toast.LENGTH_SHORT).show();
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                } else {
                    requireActivity().finish();
                }
            } else {
                Toast.makeText(requireContext(), "Error updating computer", Toast.LENGTH_SHORT).show();
            }
        } else {
            Computer computer = new Computer(model, price, date, isLaptop, imageUriString, selectedBrand.getId());
            long result = dbHelper.addComputer(computer);

            if (result != -1) {
                Toast.makeText(requireContext(), "Computer Saved!", Toast.LENGTH_SHORT).show();
                requireActivity().finish(); // Go back to Activity2
            } else {
                Toast.makeText(requireContext(), "Error saving computer", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

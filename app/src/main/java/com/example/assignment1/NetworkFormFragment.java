package com.example.assignment1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.example.assignment1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NetworkFormFragment extends Fragment {

    private EditText etName, etSerial, etLocation;
    private Spinner spinnerBrand, spinnerStatus;
    private Button btnSubmit;
    private List<Brand> brandList;
    private ArrayAdapter<Brand> brandAdapter;

    // Edit Mode State
    private Long editingMachineId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_form, container, false);

        // Initialize Views
        etName = view.findViewById(R.id.et_machine_name);
        etSerial = view.findViewById(R.id.et_serial_number);
        etLocation = view.findViewById(R.id.et_location);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        spinnerBrand = view.findViewById(R.id.spinner_network_brand);
        btnSubmit = view.findViewById(R.id.btn_submit_machine);

        // Optional: Set title if needed
        TextView tvTitle = view.findViewById(R.id.tv_form_title);
        if (tvTitle != null) {
            // Title is set in XML ("New Machine"), but we could change it dynamically here
            // if needed
        }

        // Init Brand Spinner
        brandList = new ArrayList<>();
        brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, brandList);
        spinnerBrand.setAdapter(brandAdapter);

        // Init Status Spinner
        String[] statuses = { "Active", "Inactive" };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);

        // Fetch brands from server
        fetchBrands();

        btnSubmit.setOnClickListener(v -> submitMachine());

        return view;
    }

    public void prepareEdit(Machine machine) {
        editingMachineId = machine.getId();
        etName.setText(machine.getName());
        etSerial.setText(machine.getSerialNumber());
        etLocation.setText(machine.getLocation());

        // Status Spinner
        if ("Inactive".equalsIgnoreCase(machine.getStatus())) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }

        // Brand Spinner
        for (int i = 0; i < brandList.size(); i++) {
            if (brandList.get(i).getId() == machine.getBrandId()) {
                spinnerBrand.setSelection(i);
                break;
            }
        }

        btnSubmit.setText("Update Machine");

        // Update Title if possible
        if (getView() != null) {
            TextView tvTitle = getView().findViewById(R.id.tv_form_title);
            if (tvTitle != null) {
                tvTitle.setText("Edit Machine");
            }
        }
    }

    public void resetForm() {
        editingMachineId = null;
        etName.setText("");
        etSerial.setText("");
        etLocation.setText("");
        spinnerStatus.setSelection(0);
        spinnerBrand.setSelection(0);
        btnSubmit.setText("Add Machine");

        // Reset Title
        if (getView() != null) {
            TextView tvTitle = getView().findViewById(R.id.tv_form_title);
            if (tvTitle != null) {
                tvTitle.setText("New Machine");
            }
        }
    }

    private void fetchBrands() {
        if (getActivity() == null)
            return;

        NetworkActivity activity = (NetworkActivity) getActivity();
        // Uses IP for wireless tethering
        String url = "http://172.31.89.202:3000/api/brands";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            brandList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                brandList.add(new Brand(
                                        obj.getLong("id"),
                                        obj.getString("name"),
                                        obj.optString("description", "")));
                            }
                            brandAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing brands", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Using Offline Brands (Server Unreachable)",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                        brandList.clear();
                        brandList.add(new Brand(1L, "Dell (Offline)", "Offline default"));
                        brandList.add(new Brand(2L, "HP (Offline)", "Offline default"));
                        brandList.add(new Brand(3L, "Lenovo (Offline)", "Offline default"));
                        brandList.add(new Brand(4L, "Apple (Offline)", "Offline default"));
                        brandList.add(new Brand(5L, "Asus (Offline)", "Offline default"));
                        brandAdapter.notifyDataSetChanged();
                    }
                });
        activity.getRequestQueue().add(jsonArrayRequest);
    }

    private void submitMachine() {
        String name = etName.getText().toString().trim();
        String serial = etSerial.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String status = "";
        if (spinnerStatus.getSelectedItem() != null) {
            status = spinnerStatus.getSelectedItem().toString();
        }

        Brand selectedBrand = null;
        if (spinnerBrand.getSelectedItem() instanceof Brand) {
            selectedBrand = (Brand) spinnerBrand.getSelectedItem();
        }

        if (name.isEmpty() || serial.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Name and Serial are required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (getActivity() == null)
            return;

        // Safe casting with check
        if (!(getActivity() instanceof NetworkActivity)) {
            return;
        }

        NetworkActivity activity = (NetworkActivity) getActivity();
        if (activity == null)
            return;

        RequestQueue queue = activity.getRequestQueue();
        String url = activity.getBaseUrl();
        int method = Request.Method.POST;

        if (editingMachineId != null) {
            url = url + "/" + editingMachineId;
            method = Request.Method.PUT;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("serial_number", serial);
            jsonBody.put("location", location);
            jsonBody.put("status", status);
            // Include brand_id if valid
            if (selectedBrand != null && selectedBrand.getId() > 0) {
                jsonBody.put("brand_id", selectedBrand.getId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                method,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String msg = (editingMachineId != null) ? "Machine Updated!" : "Machine Added!";
                        if (getContext() != null) {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                        resetForm();
                        if (getActivity() instanceof NetworkActivity) {
                            ((NetworkActivity) getActivity()).refreshData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "Attempt failed";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorData = new String(error.networkResponse.data, "UTF-8");
                                JSONObject errorJson = new JSONObject(errorData);
                                message = errorJson.optString("error", error.getMessage());
                            } catch (Exception e) {
                                message = "Server Error: " + error.networkResponse.statusCode;
                            }
                        } else {
                            message = "Connection Error: Check Server";
                        }

                        if (getContext() != null) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        queue.add(jsonObjectRequest);
    }
}

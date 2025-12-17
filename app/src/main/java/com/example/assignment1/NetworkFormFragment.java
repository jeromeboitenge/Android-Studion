package com.example.assignment1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_form, container, false);

        etName = view.findViewById(R.id.et_machine_name);
        etSerial = view.findViewById(R.id.et_serial_number);
        etLocation = view.findViewById(R.id.et_location);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        spinnerBrand = view.findViewById(R.id.spinner_network_brand);
        btnSubmit = view.findViewById(R.id.btn_submit_machine);

        // Init Brand Spinner
        brandList = new ArrayList<>();
        brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, brandList);
        spinnerBrand.setAdapter(brandAdapter);

        // Init Status Spinner
        String[] statuses = { "Active", "Inactive" };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);

        fetchBrands();

        btnSubmit.setOnClickListener(v -> submitMachine());

        return view;
    }

    private void fetchBrands() {
        if (getActivity() == null)
            return;

        NetworkActivity activity = (NetworkActivity) getActivity();
        // Updated to Local IP for Physical Device support
        String url = "http://10.45.204.208:3000/api/brands";

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
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load brands. Using Offline.", Toast.LENGTH_SHORT)
                                    .show();
                        }

                        // FALLBACK: Load default brands so app is usable even if network fails
                        brandList.clear();
                        brandList.add(new Brand(1L, "Dell (Offline)", "Offline default"));
                        brandList.add(new Brand(2L, "HP (Offline)", "Offline default"));
                        brandList.add(new Brand(3L, "Lenovo (Offline)", "Offline default"));
                        brandAdapter.notifyDataSetChanged();

                        error.printStackTrace();
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
        NetworkActivity activity = (NetworkActivity) getActivity();
        RequestQueue queue = activity.getRequestQueue();
        String url = activity.getBaseUrl();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("serial_number", serial);
            jsonBody.put("location", location);
            jsonBody.put("status", status);
            if (selectedBrand != null) {
                jsonBody.put("brand_id", selectedBrand.getId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Machine Added!", Toast.LENGTH_SHORT).show();
                        }
                        etName.setText("");
                        etSerial.setText("");
                        etLocation.setText("");
                        // Optionally refresh data in activity
                        if (getActivity() instanceof NetworkActivity) {
                            ((NetworkActivity) getActivity()).refreshData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "Error adding machine";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorData = new String(error.networkResponse.data, "UTF-8");
                                JSONObject errorJson = new JSONObject(errorData);
                                message = "Error: " + errorJson.optString("error", error.getMessage());
                            } catch (Exception e) {
                                message = "Error: " + error.networkResponse.statusCode;
                            }
                        } else if (error.getMessage() != null) {
                            message = "Error: " + error.getMessage();
                        }

                        if (getContext() != null) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        }
                        error.printStackTrace();
                    }
                });

        queue.add(jsonObjectRequest);
    }
}

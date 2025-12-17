package com.example.assignment1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

    private EditText etName, etSerial, etLocation, etStatus;
    private Spinner spinnerBrand;
    private Button btnSubmit;
    private List<Brand> brandList;
    private ArrayAdapter<Brand> brandAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_form, container, false);

        etName = view.findViewById(R.id.et_machine_name);
        etSerial = view.findViewById(R.id.et_serial_number);
        etLocation = view.findViewById(R.id.et_location);
        etStatus = view.findViewById(R.id.et_status);
        spinnerBrand = view.findViewById(R.id.spinner_network_brand);
        btnSubmit = view.findViewById(R.id.btn_submit_machine);

        brandList = new ArrayList<>();
        // Init spinner with empty list
        brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, brandList);
        spinnerBrand.setAdapter(brandAdapter);

        fetchBrands();

        btnSubmit.setOnClickListener(v -> submitMachine());

        return view;
    }

    private void fetchBrands() {
        NetworkActivity activity = (NetworkActivity) getActivity();
        if (activity != null) {
            // Updated to Local IP for Physical Device support
            String url = "http://172.31.172.221:3000/api/brands";

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
                            // Silent fail or toast
                            // Toast.makeText(getContext(), "Failed to load brands",
                            // Toast.LENGTH_SHORT).show();
                        }
                    });
            activity.getRequestQueue().add(jsonArrayRequest);
        }
    }

    private void submitMachine() {
        String name = etName.getText().toString().trim();
        String serial = etSerial.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String status = etStatus.getText().toString().trim();

        Brand selectedBrand = null;
        if (spinnerBrand.getSelectedItem() instanceof Brand) {
            selectedBrand = (Brand) spinnerBrand.getSelectedItem();
        }

        if (name.isEmpty() || serial.isEmpty()) {
            Toast.makeText(getContext(), "Name and Serial are required", Toast.LENGTH_SHORT).show();
            return;
        }

        NetworkActivity activity = (NetworkActivity) getActivity();
        if (activity != null) {
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
                            Toast.makeText(getContext(), "Machine Added!", Toast.LENGTH_SHORT).show();
                            etName.setText("");
                            etSerial.setText("");
                            etLocation.setText("");
                            activity.refreshData(); // Refresh the list
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "Error adding machine", Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }
                    });

            queue.add(jsonObjectRequest);
        }
    }
}

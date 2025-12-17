package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NetworkActivity extends AppCompatActivity {

    // IMPORTANT: Updated for Physical Device testing
    // Was: "http://10.0.2.2:3000/api/machines"
    private static final String BASE_URL = "http://172.31.172.221:3000/api/machines";

    private RecyclerView recyclerView;
    private NetworkAdapter adapter;
    private RequestQueue requestQueue;
    private List<Machine> machineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        recyclerView = findViewById(R.id.recycler_network);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        machineList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        fetchData();

        Toast.makeText(this, "Tethering: Use Computer IP if on real device!", Toast.LENGTH_LONG).show();
    }

    public void refreshData() {
        fetchData();
    }

    private void fetchData() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            machineList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                long id = jsonObject.optLong("id");
                                String name = jsonObject.getString("name");
                                String serial = jsonObject.getString("serial_number");
                                String status = jsonObject.optString("status", "Active");
                                String location = jsonObject.optString("location", "Unknown");
                                String dateAdded = jsonObject.optString("created_at", "");
                                long brandId = jsonObject.optLong("brand_id", -1);
                                String brandName = jsonObject.optString("brand_name", "");

                                // Simple formatter if needed, or raw string from server
                                if (dateAdded.length() > 10) {
                                    dateAdded = dateAdded.substring(0, 10);
                                }

                                machineList.add(
                                        new Machine(id, name, serial, status, location, dateAdded, brandId, brandName));
                            }

                            adapter = new NetworkAdapter(machineList, id -> deleteMachine(id));
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(NetworkActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NetworkActivity.this, "Network Error. Check URL/Server.", Toast.LENGTH_SHORT)
                                .show();
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    private void deleteMachine(long id) {
        String url = BASE_URL + "/" + id;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Machine deleted", Toast.LENGTH_SHORT).show();
                    fetchData();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(deleteRequest);
    }

    public String getBaseUrl() {
        return BASE_URL;
    }
}

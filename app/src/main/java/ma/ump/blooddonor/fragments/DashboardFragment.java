package ma.ump.blooddonor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import ma.ump.blooddonor.R;

public class DashboardFragment extends Fragment {

    private RecyclerView recentRequestsRecycler;
    private ProgressBar progressBar;
    private TextView tvTotalStock, tvRecentRequestsLabel;
    private MaterialButton btnNewRequest, btnViewAppointments;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        return view;
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalStock = view.findViewById(R.id.tvTotalStock);
        recentRequestsRecycler = view.findViewById(R.id.recentRequestsRecycler);
        tvRecentRequestsLabel = view.findViewById(R.id.tvRecentRequestsLabel);
        btnNewRequest = view.findViewById(R.id.btnNewRequest);
        btnViewAppointments = view.findViewById(R.id.btnViewAppointments);
    }

    private void setupRecyclerView() {
        recentRequestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recentRequestsRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }
}

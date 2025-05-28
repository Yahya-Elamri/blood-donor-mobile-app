package ma.ump.blooddonor.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ma.ump.blooddonor.R;

public class BloodStockFragment extends Fragment {
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blood_stock, container, false);

        recyclerView = view.findViewById(R.id.bloodStockRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

}
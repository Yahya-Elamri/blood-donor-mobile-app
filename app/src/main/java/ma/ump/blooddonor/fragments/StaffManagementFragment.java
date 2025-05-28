package ma.ump.blooddonor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import ma.ump.blooddonor.R;

public class StaffManagementFragment extends Fragment {
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff, container, false);

        loadStaffMembers();
        return view;
    }

    private void showAddStaffDialog() {
        // Implementation for adding new staff members
    }

    private void loadStaffMembers() {
        // API call to fetch hospital staff
    }
}

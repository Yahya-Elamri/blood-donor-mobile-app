package ma.ump.blooddonor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ma.ump.blooddonor.R;

// CreateRequestDialog.java
public class CreateRequestDialog extends DialogFragment {
    private Spinner spinnerBloodType;
    private TextInputEditText etQuantity;
    private TextInputLayout quantityInputLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_blood_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerBloodType = view.findViewById(R.id.spinnerBloodType);
        etQuantity = view.findViewById(R.id.etQuantity);
        quantityInputLayout = view.findViewById(R.id.quantityInputLayout);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.blood_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);
    }

    private void createBloodRequest(String bloodType, int quantity) {
        // API call to create request
    }
}

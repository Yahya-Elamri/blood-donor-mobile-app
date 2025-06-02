package ma.ump.blooddonor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ma.ump.blooddonor.Constants.Constants;
import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.Donation;
import ma.ump.blooddonor.entity.Donor;
import ma.ump.blooddonor.utils.AuthUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppointmentsFragment extends Fragment {

    private static final String BASE_URL = Constants.BASE_URL + "/api/requests/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Views
    private TextInputEditText etSearchEmail, etDate, etLocation, etAmount;
    private MaterialButton btnSearchDonor, btnAddDonation;
    private TextView tvDonorInfo, tvEmptyState;
    private MaterialCardView cardDonorInfo, cardDonationForm, cardDonationsList;
    private RecyclerView rvDonations;
    private Chip chipDonationCount;

    // Adapter and data
    private DonationAdapter adapter;
    private List<Donation> donationList = new ArrayList<>();

    // Donor info
    private long donorId = -1;
    private String donorName = "";
    private String donorPrenom = "";
    private String donorEmail = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        etSearchEmail = view.findViewById(R.id.etSearchEmail);
        btnSearchDonor = view.findViewById(R.id.btnSearchDonor);
        tvDonorInfo = view.findViewById(R.id.tvDonorInfo);
        cardDonorInfo = view.findViewById(R.id.cardDonorInfo);
        cardDonationForm = view.findViewById(R.id.cardDonationForm);
        cardDonationsList = view.findViewById(R.id.cardDonationsList);
        etDate = view.findViewById(R.id.etDate);
        etLocation = view.findViewById(R.id.etLocation);
        etAmount = view.findViewById(R.id.etAmount);
        btnAddDonation = view.findViewById(R.id.btnAddDonation);
        rvDonations = view.findViewById(R.id.rvDonations);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        chipDonationCount = view.findViewById(R.id.chipDonationCount);
    }

    private void setupRecyclerView() {
        adapter = new DonationAdapter();
        rvDonations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDonations.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearchDonor.setOnClickListener(v -> searchDonorByEmail());
        btnAddDonation.setOnClickListener(v -> addDonation());
    }

    private void searchDonorByEmail() {
        String email = etSearchEmail.getText().toString().trim();
        if (email.isEmpty()) {
            showToast("Veuillez saisir un email");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "search?email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AuthUtils.getAuthToken(requireContext()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Erreur réseau");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Donor donor = new Gson().fromJson(responseBody, Donor.class);

                    requireActivity().runOnUiThread(() -> {
                        donorId = donor.getId();
                        donorName = donor.getNom();
                        donorPrenom = donor.getPrenom();
                        donorEmail = donor.getEmail();
                        displayDonorInfo();

                        // Show donation sections
                        cardDonationForm.setVisibility(View.VISIBLE);
                        cardDonationsList.setVisibility(View.VISIBLE);

                        // Load donations for this donor
                        loadDonations();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        if (response.code() == 404) {
                            showToast("Donneur non trouvé");
                        } else {
                            showToast("Erreur de recherche");
                        }
                        resetDonorSelection();
                    });
                }
            }
        });
    }

    private void displayDonorInfo() {
        String info = donorName + " " + donorPrenom + "\n" + donorEmail;
        tvDonorInfo.setText(info);
        cardDonorInfo.setVisibility(View.VISIBLE);
    }

    private void loadDonations() {
        if (donorId == -1) return;

        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "donation/by-donor/" + donorId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AuthUtils.getAuthToken(requireContext()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Erreur réseau");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<Donation>>(){}.getType();
                    donationList = new Gson().fromJson(responseBody, listType);

                    requireActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        updateDonationCount();
                        checkEmptyState();
                    });
                } else {
                    showToast("Erreur de chargement des dons");
                }
            }
        });
    }

    private void updateDonationCount() {
        chipDonationCount.setText(String.valueOf(donationList.size()));
    }

    private void checkEmptyState() {
        if (donationList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvDonations.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvDonations.setVisibility(View.VISIBLE);
        }
    }

    private void addDonation() {
        if (donorId == -1) {
            showToast("Veuillez d'abord sélectionner un donneur");
            return;
        }

        String date = etDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (date.isEmpty() || location.isEmpty() || amountStr.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            Donation donation = new Donation(null,date, location, amount);
            createDonation(donation);
        } catch (NumberFormatException e) {
            showToast("Quantité invalide");
        }
    }

    private void createDonation(Donation donation) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "donation?donorId=" + donorId;

        RequestBody body = RequestBody.create(
                new Gson().toJson(donation),
                JSON
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + AuthUtils.getAuthToken(requireContext()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Échec de l'ajout");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        showToast("Don ajouté avec succès");
                        clearForm();
                        loadDonations();
                    });
                } else {
                    showToast("Échec de l'ajout du don");
                }
            }
        });
    }

    private void deleteDonation(long id) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "donation/" + id;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + AuthUtils.getAuthToken(requireContext()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Échec de la suppression");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        showToast("Don supprimé");
                        loadDonations();
                    });
                } else {
                    showToast("Échec de la suppression");
                }
            }
        });
    }

    private void resetDonorSelection() {
        donorId = -1;
        donorName = "";
        donorEmail = "";
        cardDonorInfo.setVisibility(View.GONE);
        cardDonationForm.setVisibility(View.GONE);
        cardDonationsList.setVisibility(View.GONE);
        donationList.clear();
        adapter.notifyDataSetChanged();
        updateDonationCount();
    }

    private void clearForm() {
        etDate.setText("");
        etLocation.setText("");
        etAmount.setText("");
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    // RecyclerView Adapter
    private class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvDate, tvLocation;
            MaterialButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Donation donation = donationList.get(position);
            holder.tvAmount.setText(String.format("%d ml", donation.getAmount()));
            holder.tvDate.setText(donation.getDate());
            holder.tvLocation.setText(donation.getLieu());

            holder.btnDelete.setOnClickListener(v ->
                    deleteDonation(donation.getId()));
        }

        @Override
        public int getItemCount() {
            return donationList.size();
        }
    }
}
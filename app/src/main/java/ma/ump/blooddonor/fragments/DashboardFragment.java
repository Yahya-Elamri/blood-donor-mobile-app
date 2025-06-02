package ma.ump.blooddonor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ma.ump.blooddonor.Constants.Constants;
import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.BloodRequest;
import ma.ump.blooddonor.entity.BloodType;
import ma.ump.blooddonor.entity.Hospital;
import ma.ump.blooddonor.entity.RequestStatus;
import ma.ump.blooddonor.entity.UrgenceLevel;
import ma.ump.blooddonor.utils.AuthUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private Hospital currentHospital;
    private List<BloodRequest> urgentRequests = new ArrayList<>();
    private BloodInventoryAdapter inventoryAdapter;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();
    private ProgressBar progressBar;

    // View references
    private LinearLayout cardsContainer;
    private RecyclerView urgentRequestsRecycler;
    private GridView bloodInventoryGrid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize UI components
        cardsContainer = view.findViewById(R.id.cardsContainer);
        urgentRequestsRecycler = view.findViewById(R.id.urgentRequestsRecycler);
        bloodInventoryGrid = view.findViewById(R.id.bloodInventoryGrid);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup adapters
        urgentRequestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        urgentRequestsRecycler.setAdapter(new UrgentRequestsAdapter(urgentRequests));

        inventoryAdapter = new BloodInventoryAdapter(
                getContext(),
                new EnumMap<>(BloodType.class)
        );
        bloodInventoryGrid.setAdapter(inventoryAdapter);

        // Set button listeners
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHospitalData();
    }

    private void loadHospitalData() {
        Context context = requireContext();
        String userId = AuthUtils.getTokenId(context);
        String token = AuthUtils.getAuthToken(context);

        if (userId == null || token == null) {
            Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        fetchHospitalId(userId, token);
    }

    private void fetchHospitalId(String userId, String token) {
        String url = Constants.BASE_URL + "/api/requests/hospital-users/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("hospitalId") && !jsonObject.get("hospitalId").isJsonNull()) {
                    long hospitalId = jsonObject.get("hospitalId").getAsLong();
                    fetchBloodReqDetails(hospitalId, token);
                } else {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Hospital ID not found", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchBloodReqDetails(long hospitalId, String token) {
        String url = Constants.BASE_URL + "/api/requests/bloodreq/by-hospital/" + hospitalId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading hospital: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();

                Type listType = new TypeToken<List<BloodRequest>>() {}.getType();
                List<BloodRequest> bloodRequests = gson.fromJson(responseBody, listType);

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    updateSummaryCards(bloodRequests);
                    filterUrgentRequests(bloodRequests);
                    updateBloodInventory(bloodRequests);
                });
            }
        });
    }

    private void updateSummaryCards(List<BloodRequest> requests) {
        cardsContainer.removeAllViews();

        int total = requests.size();
        int pending = 0;
        int critical = 0;
        int fulfilled = 0;

        for (BloodRequest request : requests) {
            if (request.getStatut() == RequestStatus.PENDING) {
                pending++;
            } else if (request.getStatut() == RequestStatus.FULFILLED) {
                fulfilled++;
            }

            if (request.getUrgence() == UrgenceLevel.CRITICAL) {
                critical++;
            }
        }

        int[] cardTitles = {
                R.string.total_requests,
                R.string.pending_requests,
                R.string.critical_requests,
                R.string.fulfilled_requests
        };

        int[] cardValues = {total, pending, critical, fulfilled};

        int[] cardColors = {
                R.color.card_total,
                R.color.card_pending,
                R.color.card_critical,
                R.color.card_fulfilled
        };

        for (int i = 0; i < cardTitles.length; i++) {
            View cardView = LayoutInflater.from(getContext()).inflate(R.layout.summary_card, cardsContainer, false);

            TextView title = cardView.findViewById(R.id.cardTitle);
            TextView value = cardView.findViewById(R.id.cardValue);
            CardView card = cardView.findViewById(R.id.cardView);

            title.setText(getString(cardTitles[i]));
            value.setText(String.valueOf(cardValues[i]));
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), cardColors[i]));

            cardsContainer.addView(cardView);
        }
    }

    private void filterUrgentRequests(List<BloodRequest> requests) {
        urgentRequests = new ArrayList<>();

        for (BloodRequest request : requests) {
            UrgenceLevel level = request.getUrgence();
            if (level == UrgenceLevel.HIGH || level == UrgenceLevel.CRITICAL) {
                urgentRequests.add(request);
            }
        }

        UrgentRequestsAdapter adapter = (UrgentRequestsAdapter) urgentRequestsRecycler.getAdapter();
        if (adapter != null) {
            adapter.updateRequests(urgentRequests);
        }
    }


    private void updateBloodInventory(List<BloodRequest> requests) {
        Map<BloodType, Integer> inventory = new EnumMap<>(BloodType.class);

        for (BloodRequest request : requests) {
            BloodType type = request.getGroupeSanguin();
            int qty = request.getQuantite();

            inventory.put(type, inventory.getOrDefault(type, 0) + qty);
        }

        inventoryAdapter.updateInventory(inventory);
    }


    // Adapter classes
    private static class UrgentRequestsAdapter extends RecyclerView.Adapter<UrgentRequestsAdapter.ViewHolder> {
        private List<BloodRequest> requests;

        public UrgentRequestsAdapter(List<BloodRequest> requests) {
            this.requests = requests;
        }

        public void updateRequests(List<BloodRequest> newRequests) {
            this.requests = newRequests;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_urgent_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BloodRequest request = requests.get(position);
            holder.bloodType.setText(request.getGroupeSanguin().name().replace("_", "-"));
            holder.quantity.setText(request.getQuantite() + " ml");

            // Add patient info if available
            if (request.getPatientInfo() != null) {
                holder.patientInfo.setText(request.getPatientInfo());
                holder.patientInfo.setVisibility(View.VISIBLE);
            } else {
                holder.patientInfo.setVisibility(View.GONE);
            }

            int colorRes = request.getUrgence() == UrgenceLevel.CRITICAL ?
                    R.color.alert_red : R.color.warning_orange;
            holder.urgencyIndicator.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView bloodType, quantity, patientInfo;
            View urgencyIndicator;

            public ViewHolder(View itemView) {
                super(itemView);
                bloodType = itemView.findViewById(R.id.bloodType);
                quantity = itemView.findViewById(R.id.quantity);
                patientInfo = itemView.findViewById(R.id.patientInfo);
                urgencyIndicator = itemView.findViewById(R.id.urgencyIndicator);
            }
        }
    }

    private static class BloodInventoryAdapter extends ArrayAdapter<String> {
        private final Map<BloodType, Integer> inventory;
        private final Context context;
        private final BloodType[] bloodTypes = BloodType.values();

        public BloodInventoryAdapter(Context context, Map<BloodType, Integer> inventory) {
            super(context, R.layout.item_blood_type);
            this.context = context;
            this.inventory = new EnumMap<>(inventory);
        }

        public void updateInventory(Map<BloodType, Integer> newInventory) {
            this.inventory.clear();
            this.inventory.putAll(newInventory);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return bloodTypes.length;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            BloodType type = bloodTypes[position];

            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.item_blood_type, parent, false);
            }

            TextView typeText = convertView.findViewById(R.id.bloodType);
            TextView stockText = convertView.findViewById(R.id.stockValue);
            ProgressBar progressBar = convertView.findViewById(R.id.stockProgress);

            int stock = inventory.getOrDefault(type, 0);

            typeText.setText(type.name().replace("_", "-"));
            stockText.setText(stock + " ml");

            // Set progress (max 2000ml = 100%)
            int progress = Math.min(stock * 100 / 2000, 100);
            progressBar.setProgress(progress);

            // Set progress color based on level
            int colorRes = stock < 500 ? R.color.alert_red :
                    stock < 1000 ? R.color.warning_orange : R.color.success_green;
            progressBar.setProgressTintList(
                    ContextCompat.getColorStateList(context, colorRes));

            return convertView;
        }
    }
}
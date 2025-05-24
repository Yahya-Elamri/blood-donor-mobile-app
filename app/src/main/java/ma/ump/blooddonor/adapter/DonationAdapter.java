package ma.ump.blooddonor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.Donation;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
    private List<Donation> donations;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onShareClick(Donation donation);
        void onDetailsClick(Donation donation);
    }

    public DonationAdapter(List<Donation> donations, OnItemClickListener listener) {
        this.donations = donations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        // Format values using string resources
        holder.tvDonationId.setText(
                holder.itemView.getContext().getString(R.string.donation_number, donation.getId())
        );
        holder.tvDate.setText(formatDate(donation.getDate()));
        holder.tvLieu.setText(donation.getLieu());
        holder.tvVolume.setText(
                holder.itemView.getContext().getString(R.string.volume_placeholder, donation.getAmount())
        );

        // Set click listeners
        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareClick(donation);
            }
        });

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(donation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return donations != null ? donations.size() : 0;
    }

    public void updateData(List<Donation> newDonations) {
        donations = newDonations;
        notifyDataSetChanged();
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate; // Return raw date if parsing fails
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDonationId, tvBloodType, tvDate, tvLieu, tvVolume;
        MaterialButton btnShare, btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDonationId = itemView.findViewById(R.id.tvDonationId);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLieu = itemView.findViewById(R.id.tvLieu);
            tvVolume = itemView.findViewById(R.id.tvVolume);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
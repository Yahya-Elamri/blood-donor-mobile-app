package ma.ump.blooddonor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.Donation;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
    private List<Donation> donations;

    public DonationAdapter(List<Donation> donations) {
        this.donations = donations;
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
        holder.tvDonationId.setText("Donation #" + donation.getId());
        holder.tvDate.setText(formatDate(donation.getDate()));
        holder.tvLieu.setText(donation.getLieu());
    }

    @Override
    public int getItemCount() { return donations.size(); }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDonationId, tvDate, tvLieu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDonationId = itemView.findViewById(R.id.tvDonationId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLieu = itemView.findViewById(R.id.tvLieu);
        }
    }
}
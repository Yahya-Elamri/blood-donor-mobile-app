package ma.ump.blooddonor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ma.ump.blooddonor.entity.Hospital;

public class HospitalAdapter extends ArrayAdapter<Hospital> implements Filterable {
    private List<Hospital> originalList;
    private List<Hospital> filteredList;
    private LayoutInflater inflater;

    public HospitalAdapter(Context context, List<Hospital> hospitals) {
        super(context, android.R.layout.simple_dropdown_item_1line, hospitals);
        this.originalList = new ArrayList<>(hospitals);
        this.filteredList = new ArrayList<>(hospitals);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Hospital getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        Hospital hospital = getItem(position);

        if (hospital != null) {
            textView.setText(hospital.getNom());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = originalList;
                    results.count = originalList.size();
                } else {
                    List<Hospital> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Hospital hospital : originalList) {
                        if (hospital.getNom().toLowerCase().contains(filterPattern)) {
                            filtered.add(hospital);
                        }
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<Hospital>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<Hospital> newHospitals) {
        originalList.clear();
        originalList.addAll(newHospitals);
        filteredList.clear();
        filteredList.addAll(newHospitals);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        super.clear();
        originalList.clear();
        filteredList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends Hospital> collection) {
        originalList.addAll(collection);
        filteredList.addAll(collection);
        notifyDataSetChanged();
    }
}

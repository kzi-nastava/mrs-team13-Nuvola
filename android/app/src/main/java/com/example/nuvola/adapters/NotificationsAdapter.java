package com.example.nuvola.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.network.NotificationDTO;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final List<NotificationDTO> items;

    public NotificationsAdapter(List<NotificationDTO> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        NotificationDTO n = items.get(position);

        String type = n.type != null ? n.type : "";
        h.tvType.setText(type.replace("_", " "));
        h.tvType.setBackgroundColor(badgeColor(type));

        if (n.title != null && !n.title.isEmpty()) {
            h.tvTitle.setVisibility(View.VISIBLE);
            h.tvTitle.setText(n.title);
        } else {
            h.tvTitle.setVisibility(View.GONE);
        }

        h.tvMessage.setText(n.message != null ? n.message : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int badgeColor(String type) {
        String t = type.toLowerCase();
        if (t.contains("panic"))               return Color.parseColor("#C62828");
        if (t.contains("rideapproved"))        return Color.parseColor("#2E7D32");
        if (t.contains("rideended"))           return Color.parseColor("#546E7A");
        if (t.contains("novehicleavailable"))  return Color.parseColor("#E65100");
        if (t.contains("youareassigned"))      return Color.parseColor("#1565C0");
        if (t.contains("linkedpassanger"))     return Color.parseColor("#6A1B9A");
        if (t.contains("ridereminder"))        return Color.parseColor("#00695C");
        return Color.parseColor("#0A0F2C");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTitle, tvMessage;

        ViewHolder(View v) {
            super(v);
            tvType    = v.findViewById(R.id.tvNotifType);
            tvTitle   = v.findViewById(R.id.tvNotifTitle);
            tvMessage = v.findViewById(R.id.tvNotifMessage);
        }
    }
}
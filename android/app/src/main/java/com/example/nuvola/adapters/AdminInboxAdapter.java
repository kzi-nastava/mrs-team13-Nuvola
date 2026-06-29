package com.example.nuvola.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.network.AdminInboxItemDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminInboxAdapter extends RecyclerView.Adapter<AdminInboxAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AdminInboxItemDTO item);
    }

    private final List<AdminInboxItemDTO> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setItems(List<AdminInboxItemDTO> list) {
        items.clear();
        items.addAll(list);
        Collections.sort(items, (a, b) -> {
            String ta = a.lastMessageTime != null ? a.lastMessageTime : "";
            String tb = b.lastMessageTime != null ? b.lastMessageTime : "";
            return tb.compareTo(ta);
        });
        notifyDataSetChanged();
    }

    /** Updates the preview for a chat when a new WS message arrives. Returns true if the item was found. */
    public boolean updateLastMessage(com.example.nuvola.network.ChatMessageDTO msg) {
        for (int i = 0; i < items.size(); i++) {
            AdminInboxItemDTO item = items.get(i);
            if (item.chatId != null && item.chatId.equals(msg.chatId)) {
                item.lastMessageContent = msg.content;
                item.lastMessageSenderName = msg.senderName;
                item.lastMessageTime = msg.sentAt;
                if (i != 0) {
                    items.remove(i);
                    items.add(0, item);
                    notifyDataSetChanged();
                } else {
                    notifyItemChanged(0);
                }
                return true;
            }
        }
        return false;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_inbox, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AdminInboxItemDTO item = items.get(position);
        h.tvName.setText(item.ownerName != null ? item.ownerName : "User #" + item.userId);
        String preview = "";
        if (item.lastMessageSenderName != null) preview += item.lastMessageSenderName + ": ";
        if (item.lastMessageContent != null) preview += item.lastMessageContent;
        h.tvPreview.setText(preview.isEmpty() ? "No messages yet" : preview);
        h.tvTime.setText(item.lastMessageTime != null ? formatTime(item.lastMessageTime) : "");
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatTime(String t) {
        if (t == null) return "";
        try {
            int idx = t.indexOf('T');
            if (idx >= 0 && t.length() > idx + 5) return t.substring(idx + 1, idx + 6);
        } catch (Exception ignored) {}
        return t;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPreview, tvTime;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvInboxName);
            tvPreview = v.findViewById(R.id.tvInboxPreview);
            tvTime = v.findViewById(R.id.tvInboxTime);
        }
    }
}
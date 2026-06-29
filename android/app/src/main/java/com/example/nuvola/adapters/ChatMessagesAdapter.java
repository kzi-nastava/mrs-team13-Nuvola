package com.example.nuvola.adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.network.ChatMessageDTO;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder> {

    private final List<ChatMessageDTO> items = new ArrayList<>();
    private final long myId;

    public ChatMessagesAdapter(long myId) {
        this.myId = myId;
    }

    public void addMessage(ChatMessageDTO msg) {
        items.add(msg);
        notifyItemInserted(items.size() - 1);
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        items.clear();
        items.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ChatMessageDTO msg = items.get(position);
        boolean mine = msg.senderId != null && msg.senderId == myId;

        h.tvSender.setText(msg.senderName != null ? msg.senderName : "");
        h.tvContent.setText(msg.content != null ? msg.content : "");
        h.tvTime.setText(msg.sentAt != null ? formatTime(msg.sentAt) : "");

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) h.bubble.getLayoutParams();
        if (mine) {
            h.bubble.setBackgroundColor(Color.parseColor("#0A0F2C"));
            h.tvContent.setTextColor(Color.WHITE);
            h.tvSender.setTextColor(Color.parseColor("#AAAACC"));
            h.tvTime.setTextColor(Color.parseColor("#AAAACC"));
            params.gravity = Gravity.END;
            h.tvSender.setVisibility(View.GONE);
        } else {
            h.bubble.setBackgroundColor(Color.parseColor("#E8EAF6"));
            h.tvContent.setTextColor(Color.parseColor("#0A0F2C"));
            h.tvSender.setTextColor(Color.parseColor("#5C6BC0"));
            h.tvTime.setTextColor(Color.parseColor("#888888"));
            params.gravity = Gravity.START;
            h.tvSender.setVisibility(View.VISIBLE);
        }
        h.bubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatTime(String sentAt) {
        if (sentAt == null) return "";
        // sentAt is LocalDateTime string like "2024-07-01T10:00:00" — show just HH:mm
        try {
            int tIdx = sentAt.indexOf('T');
            if (tIdx >= 0 && sentAt.length() > tIdx + 5) {
                return sentAt.substring(tIdx + 1, tIdx + 6);
            }
        } catch (Exception ignored) {}
        return sentAt;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bubble;
        TextView tvSender, tvContent, tvTime;

        ViewHolder(View v) {
            super(v);
            bubble = v.findViewById(R.id.chatBubble);
            tvSender = v.findViewById(R.id.tvChatSender);
            tvContent = v.findViewById(R.id.tvChatContent);
            tvTime = v.findViewById(R.id.tvChatTime);
        }
    }
}
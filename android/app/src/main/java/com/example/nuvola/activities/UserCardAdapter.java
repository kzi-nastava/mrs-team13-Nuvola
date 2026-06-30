package com.example.nuvola.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nuvola.R;

import java.util.List;

import dto.AdminUserDTO;

public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.VH> {

    public interface UserActionListener {
        void onBlock(AdminUserDTO user);
        void onUnblock(AdminUserDTO user);
        void onInfo(AdminUserDTO user);
    }

    private final Context context;
    private List<AdminUserDTO> users;
    private boolean showInfoButton;
    private final UserActionListener listener;

    public UserCardAdapter(Context context, List<AdminUserDTO> users,
                           boolean showInfoButton, UserActionListener listener) {
        this.context        = context;
        this.users          = users;
        this.showInfoButton = showInfoButton;
        this.listener       = listener;
    }

    public void setUsers(List<AdminUserDTO> newUsers, boolean showInfo) {
        this.users          = newUsers;
        this.showInfoButton = showInfo;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminUserDTO u = users.get(position);

        h.tvName.setText(u.firstName + " " + u.lastName);
        h.tvEmail.setText(u.email);
        h.tvAddress.setText(u.address != null ? u.address : "");
        h.tvPhone.setText(u.phone != null ? u.phone : "");

        // ── BLOCKED state ──
        if (u.blocked) {
            h.tvBlocked.setVisibility(View.VISIBLE);
            if (u.blockingReason != null && !u.blockingReason.isEmpty()) {
                h.tvBlocked.setText("BLOCKED — " + u.blockingReason);
            } else {
                h.tvBlocked.setText("BLOCKED");
            }
            // show Unblock, hide Block
            h.btnBlock.setVisibility(View.GONE);
            h.btnUnblock.setVisibility(View.VISIBLE);
            h.btnUnblock.setOnClickListener(v -> listener.onUnblock(u));
        } else {
            h.tvBlocked.setVisibility(View.GONE);
            // show Block, hide Unblock
            h.btnBlock.setVisibility(View.VISIBLE);
            h.btnUnblock.setVisibility(View.GONE);
            h.btnBlock.setOnClickListener(v -> listener.onBlock(u));
        }

        // ── Profile picture ──
        String picUrl = (u.picture != null && !u.picture.isEmpty())
                ? "http://10.0.2.2:8080/api/profile/picture/" + u.picture
                : null;

        if (picUrl != null) {
            Glide.with(context)
                    .load(picUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(h.ivAvatar);
        } else {
            h.ivAvatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // ── Info button (drivers only) ──
        if (showInfoButton) {
            h.btnInfo.setVisibility(View.VISIBLE);
            h.btnInfo.setOnClickListener(v -> listener.onInfo(u));
        } else {
            h.btnInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    // Update a single user in the list without full reload
    public void updateUser(AdminUserDTO updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id == updated.id) {
                users.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvEmail, tvAddress, tvPhone, tvBlocked;
        Button btnBlock, btnUnblock, btnInfo;

        VH(@NonNull View v) {
            super(v);
            ivAvatar   = v.findViewById(R.id.ivAvatar);
            tvName     = v.findViewById(R.id.tvName);
            tvEmail    = v.findViewById(R.id.tvEmail);
            tvAddress  = v.findViewById(R.id.tvAddress);
            tvPhone    = v.findViewById(R.id.tvPhone);
            tvBlocked  = v.findViewById(R.id.tvBlocked);
            btnBlock   = v.findViewById(R.id.btnBlock);
            btnUnblock = v.findViewById(R.id.btnUnblock);
            btnInfo    = v.findViewById(R.id.btnInfo);
        }
    }
}
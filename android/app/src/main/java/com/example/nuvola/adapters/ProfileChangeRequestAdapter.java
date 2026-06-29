package com.example.nuvola.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dto.ProfileChangeRequestDTO;

public class ProfileChangeRequestAdapter
        extends RecyclerView.Adapter<
        ProfileChangeRequestAdapter.RequestViewHolder> {

    public interface RequestActionListener {

        void onApprove(
                ProfileChangeRequestDTO request
        );

        void onReject(
                ProfileChangeRequestDTO request
        );
    }

    private final List<ProfileChangeRequestDTO> requests =
            new ArrayList<>();

    private final RequestActionListener listener;

    public ProfileChangeRequestAdapter(
            List<ProfileChangeRequestDTO> initialRequests,
            RequestActionListener listener
    ) {
        if (initialRequests != null) {
            requests.addAll(initialRequests);
        }

        this.listener = listener;
    }

    public void setRequests(
            List<ProfileChangeRequestDTO> newRequests
    ) {
        requests.clear();

        if (newRequests != null) {
            requests.addAll(newRequests);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_profile_change_request,
                        parent,
                        false
                );

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder,
            int position
    ) {
        ProfileChangeRequestDTO request =
                requests.get(position);

        holder.bind(
                request,
                listener
        );
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView driverNameText;
        private final TextView driverEmailText;
        private final TextView statusText;
        private final TextView requestedAtText;

        private final LinearLayout changesContainer;

        private final MaterialButton rejectButton;
        private final MaterialButton approveButton;

        public RequestViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            driverNameText =
                    itemView.findViewById(
                            R.id.tvRequestDriverName
                    );

            driverEmailText =
                    itemView.findViewById(
                            R.id.tvRequestDriverEmail
                    );

            statusText =
                    itemView.findViewById(
                            R.id.tvRequestStatus
                    );

            requestedAtText =
                    itemView.findViewById(
                            R.id.tvRequestCreatedAt
                    );

            changesContainer =
                    itemView.findViewById(
                            R.id.changesContainer
                    );

            rejectButton =
                    itemView.findViewById(
                            R.id.btnRejectRequest
                    );

            approveButton =
                    itemView.findViewById(
                            R.id.btnApproveRequest
                    );
        }

        private void bind(
                ProfileChangeRequestDTO request,
                RequestActionListener listener
        ) {
            driverNameText.setText(
                    valueOrFallback(
                            request.driverName,
                            "Unknown driver"
                    )
            );

            driverEmailText.setText(
                    valueOrFallback(
                            request.driverEmail,
                            ""
                    )
            );

            statusText.setText(
                    valueOrFallback(
                            request.status,
                            "PENDING"
                    ).toUpperCase()
            );

            requestedAtText.setText(
                    "Requested: "
                            + formatDateTime(
                            request.createdAt
                    )
            );

            changesContainer.removeAllViews();

            addChangedFields(request);

            approveButton.setOnClickListener(
                    view -> {
                        if (listener != null) {
                            listener.onApprove(request);
                        }
                    }
            );

            rejectButton.setOnClickListener(
                    view -> {
                        if (listener != null) {
                            listener.onReject(request);
                        }
                    }
            );
        }

        private void addChangedFields(
                ProfileChangeRequestDTO request
        ) {
            if (request.hasFirstNameChanged()) {
                addChangeRow(
                        "FIRST NAME",
                        request.currentFirstName,
                        request.firstName
                );
            }

            if (request.hasLastNameChanged()) {
                addChangeRow(
                        "LAST NAME",
                        request.currentLastName,
                        request.lastName
                );
            }

            if (request.hasPhoneChanged()) {
                addChangeRow(
                        "PHONE",
                        request.currentPhone,
                        request.phone
                );
            }

            if (request.hasAddressChanged()) {
                addChangeRow(
                        "ADDRESS",
                        request.currentAddress,
                        request.address
                );
            }

            if (request.hasModelChanged()) {
                addChangeRow(
                        "VEHICLE MODEL",
                        request.currentModel,
                        request.model
                );
            }

            if (request.hasTypeChanged()) {
                addChangeRow(
                        "VEHICLE TYPE",
                        request.currentType,
                        request.type
                );
            }

            if (request.hasNumberOfSeatsChanged()) {
                addChangeRow(
                        "NUMBER OF SEATS",
                        objectToText(
                                request.currentNumOfSeats
                        ),
                        objectToText(
                                request.numOfSeats
                        )
                );
            }

            if (request.hasBabyFriendlyChanged()) {
                addChangeRow(
                        "BABY FRIENDLY",
                        booleanToText(
                                request.currentBabyFriendly
                        ),
                        booleanToText(
                                request.babyFriendly
                        )
                );
            }

            if (request.hasPetFriendlyChanged()) {
                addChangeRow(
                        "PET FRIENDLY",
                        booleanToText(
                                request.currentPetFriendly
                        ),
                        booleanToText(
                                request.petFriendly
                        )
                );
            }

            if (changesContainer.getChildCount() == 0) {
                addChangeRow(
                        "CHANGES",
                        "No visible changes",
                        "No visible changes"
                );
            }
        }

        private void addChangeRow(
                String label,
                String oldValue,
                String newValue
        ) {
            View row = LayoutInflater
                    .from(itemView.getContext())
                    .inflate(
                            R.layout.item_profile_change_value,
                            changesContainer,
                            false
                    );

            TextView labelText =
                    row.findViewById(
                            R.id.tvChangeLabel
                    );

            TextView oldValueText =
                    row.findViewById(
                            R.id.tvOldValue
                    );

            TextView newValueText =
                    row.findViewById(
                            R.id.tvNewValue
                    );

            labelText.setText(label);

            oldValueText.setText(
                    valueOrFallback(
                            oldValue,
                            "Not set"
                    )
            );

            oldValueText.setPaintFlags(
                    oldValueText.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );

            newValueText.setText(
                    valueOrFallback(
                            newValue,
                            "Not set"
                    )
            );

            changesContainer.addView(row);
        }

        private static String formatDateTime(
                String value
        ) {
            if (value == null
                    || value.trim().isEmpty()) {

                return "Unknown";
            }

            return value
                    .replace("T", " ")
                    .replaceAll("\\.\\d+$", "");
        }

        private static String booleanToText(
                Boolean value
        ) {
            if (value == null) {
                return "Not set";
            }

            return value
                    ? "Yes"
                    : "No";
        }

        private static String objectToText(
                Object value
        ) {
            if (value == null) {
                return "Not set";
            }

            return String.valueOf(value);
        }

        private static String valueOrFallback(
                String value,
                String fallback
        ) {
            if (value == null
                    || value.trim().isEmpty()) {

                return fallback;
            }

            return value;
        }
    }
}
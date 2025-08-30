package co.dtc.fieldwork.pactflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.util.Log;

import co.dtc.fieldwork.pactflow.R;
import co.dtc.fieldwork.pactflow.model.Contract;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {
    private List<Contract> contracts = new ArrayList<>();
    private final OnContractClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnContractClickListener {
        void onContractClick(Contract contract);
    }

    public ContractAdapter(OnContractClickListener listener) {
        this.listener = listener;
    }

    public void setContracts(List<Contract> contracts) {
        if (contracts != null) {
            this.contracts = new ArrayList<>(contracts);
            notifyDataSetChanged();
        } else {
            this.contracts.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contract, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        try {
            if (position < 0 || position >= contracts.size()) {
                Log.e("ContractAdapter", "Invalid position: " + position + ", list size: " + contracts.size());
                return;
            }

            Contract contract = contracts.get(position);
            if (contract == null) {
                Log.e("ContractAdapter", "Contract is null at position: " + position);
                return;
            }

            // Debug log contract details
            Log.d("ContractAdapter", String.format("Binding contract at position %d: %s, isFinalized: %s, ID: %s",
                    position,
                    contract.getTitle(),
                    contract.isFinalized(),
                    contract.getId()));

            // Format amount with currency
            String amount = "";
            try {
                amount = NumberFormat.getCurrencyInstance().format(contract.getAmount());
            } catch (Exception e) {
                Log.e("ContractAdapter", "Error formatting amount: " + e.getMessage());
                amount = "$" + contract.getAmount();
            }

            // Format dates
            String startDate = contract.getStartDate() != null ? contract.getStartDate().toString() : "N/A";
            String endDate = contract.getEndDate() != null ? contract.getEndDate().toString() : "N/A";
            String dates = String.format("%s - %s", startDate, endDate);

            // Set text values
            holder.tvTitle.setText(contract.getTitle() != null ? contract.getTitle() : "No Title");
            holder.tvAmount.setText(amount);
            holder.tvType.setText(contract.getContractType() != null ? contract.getContractType() : "");
            holder.tvDates.setText(dates);

            // Set contract status
            if (holder.tvStatus != null) {
                boolean isDraft = !contract.isFinalized();
                Log.d("ContractAdapter", String.format("Setting status for %s: isDraft=%s, isFinalized=%s",
                        contract.getTitle(), isDraft, contract.isFinalized()));

                if (isDraft) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText("DRAFT");
                    holder.tvStatus.setBackgroundResource(R.drawable.draft_status_bg);
                    Log.d("ContractAdapter", "Draft status shown for: " + contract.getTitle());
                } else {
                    holder.tvStatus.setVisibility(View.GONE);
                    Log.d("ContractAdapter", "Hiding status for finalized contract: " + contract.getTitle());
                }
            } else {
                Log.e("ContractAdapter", "tvStatus is null in ViewHolder");
            }

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContractClick(contract);
                } else {
                    Log.e("ContractAdapter", "Listener is null, cannot handle click");
                }
            });

        } catch (Exception e) {
            Log.e("ContractAdapter", "Error in onBindViewHolder: " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvAmount, tvType, tvDates, tvStatus;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}

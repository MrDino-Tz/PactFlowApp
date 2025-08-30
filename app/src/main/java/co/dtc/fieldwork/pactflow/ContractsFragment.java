package co.dtc.fieldwork.pactflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import co.dtc.fieldwork.pactflow.adapter.ContractAdapter;
import co.dtc.fieldwork.pactflow.model.Contract;
import co.dtc.fieldwork.pactflow.ui.contract.CreateContractActivity;
import co.dtc.fieldwork.pactflow.ui.decoration.SpacesItemDecoration;
import co.dtc.fieldwork.pactflow.viewmodel.ContractViewModel;

public class ContractsFragment extends Fragment {
    private ContractViewModel viewModel;
    private ContractAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateView;
    private MaterialButton btnCreateFirstContract;
    private TextInputEditText etSearch;
    private View rootView; // Store root view to prevent NPE

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contracts, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ContractViewModel.class);

        // Initialize views
        initializeViews(rootView);
        setupRecyclerView(rootView);
        setupSearch();
        setupRefreshLayout();
        setupClickListeners();

        // Observe ViewModel
        observeViewModel();

        // Check network and load data
        checkNetworkAndLoadData();

        return rootView;
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateView = view.findViewById(R.id.layoutEmptyState);
        btnCreateFirstContract = emptyStateView.findViewById(R.id.btnCreateFirstContract);
        etSearch = view.findViewById(R.id.etSearch);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvContracts);
        if (recyclerView == null) {
            Log.e("ContractsFragment", "RecyclerView not found in layout!");
            return;
        }

        Log.d("ContractsFragment", "Setting up RecyclerView");

        // Use a LinearLayoutManager with vertical orientation
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // Add item decoration for spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        // Set up the adapter
        adapter = new ContractAdapter(contract -> {
            // Handle contract item click
            Log.d("ContractsFragment", "Contract clicked: " + contract.getTitle());
            // TODO: Navigate to contract details
        });

        // Set the adapter on the RecyclerView
        recyclerView.setAdapter(adapter);
        Log.d("ContractsFragment", "RecyclerView adapter set");

        // Add scroll listener for debugging
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("ContractsFragment", "RecyclerView scrolled. dx: " + dx + ", dy: " + dy);
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchContracts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isNetworkAvailable()) {
                loadContracts();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                showNoInternetDialog();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupClickListeners() {
        if (rootView == null) return;

        // Add Contract button
        View btnAddContract = rootView.findViewById(R.id.btnAddContract);
        if (btnAddContract != null) {
            btnAddContract.setOnClickListener(v -> navigateToCreateContract());
        }

        // Create First Contract button in empty state
        if (btnCreateFirstContract != null) {
            btnCreateFirstContract.setOnClickListener(v -> navigateToCreateContract());
        }
    }

    private void observeViewModel() {
        viewModel.getContracts().observe(getViewLifecycleOwner(), contracts -> {
            Log.d("ContractsFragment", "Observed contracts list update. Size: " + (contracts != null ? contracts.size() : 0));
            updateContractsList(contracts);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d("ContractsFragment", "Loading state changed: " + isLoading);
            setLoading(isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e("ContractsFragment", "Error observed: " + error);
                showError(error);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkNetworkAndLoadData() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        } else {
            loadContracts();
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton("Retry", (dialog, which) -> checkNetworkAndLoadData())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadContracts() {
        viewModel.loadContracts();
    }

    private void updateContractsList(List<Contract> contracts) {
        Log.d("ContractsFragment", "updateContractsList called with " + (contracts != null ? contracts.size() : 0) + " items");

        if (getView() == null) {
            Log.e("ContractsFragment", "View is null in updateContractsList");
            return;
        }

        // Log contract details for debugging
        if (contracts != null && !contracts.isEmpty()) {
            Log.d("ContractsFragment", "Contract list contents:");
            for (int i = 0; i < Math.min(contracts.size(), 5); i++) {
                try {
                    Contract c = contracts.get(i);
                    Log.d("ContractsFragment", String.format("  [%d] %s (ID: %s, Finalized: %s)",
                            i, c.getTitle(), c.getId(), c.isFinalized()));
                } catch (Exception e) {
                    Log.e("ContractsFragment", "Error logging contract at index " + i, e);
                }
            }
            if (contracts.size() > 5) {
                Log.d("ContractsFragment", "  ... and " + (contracts.size() - 5) + " more");
            }

            // Update the adapter
            adapter.setContracts(contracts);

            // Hide empty state and show RecyclerView
            if (emptyStateView != null) {
                emptyStateView.setVisibility(View.GONE);
            }

            RecyclerView rv = getView().findViewById(R.id.rvContracts);
            if (rv != null) {
                rv.setVisibility(View.VISIBLE);
            }

            Log.d("ContractsFragment", "Updated contracts list with " + contracts.size() + " items");
        } else {
            // Show empty state and hide RecyclerView when no contracts
            if (emptyStateView != null) {
                emptyStateView.setVisibility(View.VISIBLE);
            }

            RecyclerView rv = getView().findViewById(R.id.rvContracts);
            if (rv != null) {
                rv.setVisibility(View.GONE);
            }

            // Update adapter with empty list
            adapter.setContracts(contracts != null ? contracts : new ArrayList<>());

            Log.d("ContractsFragment", "No contracts available, showing empty state");
        }

        // Force RecyclerView to update
        RecyclerView rv = getView().findViewById(R.id.rvContracts);
        if (rv != null && rv.getAdapter() != null) {
            rv.getAdapter().notifyDataSetChanged();
        }
    }

    private void setLoading(Boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            if (adapter.getItemCount() == 0) {
                emptyStateView.setVisibility(View.GONE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String errorMessage) {
        if (getContext() != null && errorMessage != null) {
            swipeRefreshLayout.setRefreshing(false);
            // Show error message (you can use a Snackbar or Toast)
            // Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCreateContract() {
        Intent intent = new Intent(getActivity(), CreateContractActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}

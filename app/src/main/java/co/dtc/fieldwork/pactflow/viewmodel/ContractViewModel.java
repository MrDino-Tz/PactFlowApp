package co.dtc.fieldwork.pactflow.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import co.dtc.fieldwork.pactflow.model.Contract;
import co.dtc.fieldwork.pactflow.repository.ContractRepository;
import co.dtc.fieldwork.pactflow.utils.NotificationHelper;

public class ContractViewModel extends ViewModel {
    private final ContractRepository repository;
    private final MutableLiveData<Contract> contractLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Contract>> contractsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private List<Contract> allContracts = new ArrayList<>();

    private NotificationHelper notificationHelper;

    public ContractViewModel() {
        this.repository = new ContractRepository();
    }

    // Initialize with context when needed
    public void init(Context context) {
        if (notificationHelper == null) {
            this.notificationHelper = new NotificationHelper(context);
        }
    }

    public LiveData<Contract> getContract() {
        return contractLiveData;
    }

    public LiveData<List<Contract>> getContracts() {
        return contractsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadContracts() {
        android.util.Log.d("ContractViewModel", "Loading contracts...");
        isLoading.setValue(true);

        repository.getAllContracts(new ContractRepository.ContractsListCallback() {
            @Override
            public void onSuccess(List<Contract> contracts) {
                android.util.Log.d("ContractViewModel", "Successfully loaded " + (contracts != null ? contracts.size() : 0) + " contracts");
                isLoading.postValue(false);
                allContracts = contracts != null ? contracts : new ArrayList<>();
                contractsLiveData.postValue(allContracts);

                // Log the first few contracts for debugging
                if (allContracts != null && !allContracts.isEmpty()) {
                    int maxLogs = Math.min(allContracts.size(), 3);
                    for (int i = 0; i < maxLogs; i++) {
                        Contract c = allContracts.get(i);
                        android.util.Log.d("ContractViewModel", String.format(
                                "Contract[%d]: %s, ID: %s, isFinalized: %s",
                                i, c.getTitle(), c.getId(), c.isFinalized()
                        ));
                    }
                }
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void searchContracts(String query) {
        if (query == null || query.trim().isEmpty()) {
            contractsLiveData.setValue(allContracts);
            return;
        }

        String searchQuery = query.toLowerCase().trim();
        List<Contract> filteredList = new ArrayList<>();

        for (Contract contract : allContracts) {
            if (contract.getTitle().toLowerCase().contains(searchQuery) ||
                    contract.getDescription().toLowerCase().contains(searchQuery) ||
                    contract.getContractType().toLowerCase().contains(searchQuery)) {
                filteredList.add(contract);
            }
        }

        contractsLiveData.setValue(filteredList);
    }

    public void createContract(Contract contract, ContractRepository.ContractCallback callback) {
        android.util.Log.d("ContractViewModel", "Creating new contract: " + contract.getTitle());

        repository.createContract(contract, new ContractRepository.ContractCallback() {
            @Override
            public void onSuccess(Contract contract) {
                android.util.Log.d("ContractViewModel", "Contract created successfully, ID: " + contract.getId());

                // Add the new contract to the list
                List<Contract> currentContracts = contractsLiveData.getValue();
                if (currentContracts == null) {
                    currentContracts = new ArrayList<>();
                }
                currentContracts.add(contract);
                contractsLiveData.setValue(currentContracts);

                // Show notification if helper is initialized
                if (notificationHelper != null) {
                    try {
                        android.util.Log.d("ContractViewModel", "Showing notification for contract: " + contract.getId());
                        notificationHelper.showContractCreatedNotification(contract);
                    } catch (Exception e) {
                        android.util.Log.e("ContractViewModel", "Error showing notification: " + e.getMessage(), e);
                    }
                } else {
                    android.util.Log.w("ContractViewModel", "NotificationHelper is null, cannot show notification");
                }

                // Notify success
                if (callback != null) {
                    callback.onSuccess(contract);
                }
            }
                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                    if (callback != null) {
                        callback.onError(message);
                    }
                }
            });
        }

        public void getContractById(Long id) {
            isLoading.setValue(true);
            repository.getContractById(id, new ContractRepository.ContractCallback() {
                @Override
                public void onSuccess(Contract contract) {
                    isLoading.postValue(false);
                    contractLiveData.postValue(contract);
                }

                @Override
                public void onError(String errorMessage) {
                    isLoading.postValue(false);
                    ContractViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        }

        public void updateContract(Long id, Contract contract) {
            isLoading.setValue(true);
            repository.updateContract(id, contract, new ContractRepository.ContractCallback() {
                @Override
                public void onSuccess(Contract contract) {
                    isLoading.postValue(false);
                    contractLiveData.postValue(contract);
                }

                @Override
                public void onError(String errorMessage) {
                    isLoading.postValue(false);
                    ContractViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        }

        public void deleteContract(Long id) {
            isLoading.setValue(true);
            repository.deleteContract(id, new ContractRepository.DeleteCallback() {
                @Override
                public void onSuccess() {
                    isLoading.postValue(false);
                    // Refresh the contracts list after deletion
                    loadContracts();
                }

                @Override
                public void onError(String errorMessage) {
                    isLoading.postValue(false);
                    ContractViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        }
    }

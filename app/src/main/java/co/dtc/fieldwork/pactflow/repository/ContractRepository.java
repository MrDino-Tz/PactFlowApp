package co.dtc.fieldwork.pactflow.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import co.dtc.fieldwork.pactflow.api.ApiClient;
import co.dtc.fieldwork.pactflow.api.ContractApiService;
import co.dtc.fieldwork.pactflow.api.response.ApiResponse;
import co.dtc.fieldwork.pactflow.model.Contract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractRepository {
    private final ContractApiService apiService;

    public ContractRepository() {
        this.apiService = ApiClient.getContractApiService();
    }

    public void createContract(Contract contract, final ContractCallback callback) {
        apiService.createContract(contract).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getContractById(Long id, final ContractCallback callback) {
        apiService.getContractById(id).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllContracts(final ContractsListCallback callback) {
        apiService.getAllContracts().enqueue(new Callback<ApiResponse<List<Contract>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contract>>> call, Response<ApiResponse<List<Contract>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Contract> contracts = response.body().getData();
                    // Log contract data for debugging
                    if (contracts != null) {
                        for (Contract contract : contracts) {
                            android.util.Log.d("ContractRepository", String.format(
                                    "Contract loaded - Title: %s, ID: %s, isFinalized: %s",
                                    contract.getTitle(),
                                    contract.getId(),
                                    contract.isFinalized()
                            ));
                        }
                    }
                    callback.onSuccess(contracts);
                } else {
                    String errorMsg = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                    android.util.Log.e("ContractRepository", "Error loading contracts: " + errorMsg);
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Contract>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateContract(Long id, Contract contract, final ContractCallback callback) {
        apiService.updateContract(id, contract).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteContract(Long id, final DeleteCallback callback) {
        apiService.deleteContract(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface ContractCallback {
        void onSuccess(Contract contract);
        void onError(String errorMessage);
    }

    public interface ContractsListCallback {
        void onSuccess(List<Contract> contracts);
        void onError(String errorMessage);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}

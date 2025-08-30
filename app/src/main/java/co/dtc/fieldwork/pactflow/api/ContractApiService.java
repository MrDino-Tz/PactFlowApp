package co.dtc.fieldwork.pactflow.api;

import java.util.List;

import co.dtc.fieldwork.pactflow.api.response.ApiResponse;
import co.dtc.fieldwork.pactflow.model.Contract;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ContractApiService {
    @GET("contracts")
    Call<ApiResponse<List<Contract>>> getAllContracts();

    @GET("contracts/{id}")
    Call<ApiResponse<Contract>> getContractById(@Path("id") Long id);

    @POST("contracts")
    Call<ApiResponse<Contract>> createContract(@Body Contract contract);

    @PUT("contracts/{id}")
    Call<ApiResponse<Contract>> updateContract(
            @Path("id") Long id,
            @Body Contract contract
    );

    @DELETE("contracts/{id}")
    Call<ApiResponse<Void>> deleteContract(@Path("id") Long id);
}

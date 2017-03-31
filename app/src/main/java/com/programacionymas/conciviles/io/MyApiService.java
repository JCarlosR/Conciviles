package com.programacionymas.conciviles.io;

import com.programacionymas.conciviles.io.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MyApiService {

    @POST("login")
    Call<LoginResponse> postLogin();

}
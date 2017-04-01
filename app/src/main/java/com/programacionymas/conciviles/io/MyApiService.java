package com.programacionymas.conciviles.io;

import com.programacionymas.conciviles.io.response.LoginResponse;
import com.programacionymas.conciviles.io.response.ProfileResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MyApiService {

    @POST("login")
    Call<LoginResponse> postLogin(@Query("email") String email, @Query("password") String password);

    @GET("profile")
    Call<ProfileResponse> getProfile(@Query("user_id") int user_id);

}
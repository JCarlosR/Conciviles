package com.programacionymas.conciviles.io;

import com.programacionymas.conciviles.io.response.LoginResponse;
import com.programacionymas.conciviles.io.response.ProfileResponse;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MyApiService {

    @POST("login")
    Call<LoginResponse> postLogin(@Query("email") String email, @Query("password") String password);

    @GET("profile")
    Call<ProfileResponse> getProfile(@Query("user_id") int user_id);

    @GET("informs")
    Call<ArrayList<Inform>> getInformsByLocationOfUser(@Query("user_id") int user_id);

    @GET("reports")
    Call<ArrayList<Report>> getReportsByInform(@Query("inform_id") int inform_id);


    // Spinner options:

    @GET("work-fronts")
    Call<ArrayList<WorkFront>> getWorkFrontsByLocationOfUser(@Query("user_id") int user_id);

    @GET("areas")
    Call<ArrayList<Area>> getAreas();

    @GET("responsible-users")
    Call<ArrayList<User>> getUsersByLocationOfUser(@Query("user_id") int user_id);

    @GET("critical-risks")
    Call<ArrayList<CriticalRisk>> getCriticalRisks();

}
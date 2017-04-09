package com.programacionymas.conciviles.io;

import com.programacionymas.conciviles.io.response.LoginResponse;
import com.programacionymas.conciviles.io.response.NewReportResponse;
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
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("reports/{id}")
    Call<Report> getReportById(@Path("id") int report_id);


    // Spinner options:

    @GET("work-fronts")
    Call<ArrayList<WorkFront>> getWorkFrontsByLocationOfUser(@Query("user_id") int user_id);

    @GET("areas")
    Call<ArrayList<Area>> getAreas();

    @GET("responsible-users")
    Call<ArrayList<User>> getUsersByLocationOfUser(@Query("user_id") int user_id);

    @GET("critical-risks")
    Call<ArrayList<CriticalRisk>> getCriticalRisks();


    // Register report
    // 2 methods are required because when there are no images attached
    // an error is thrown, @Multipart requires at least one @Part used

    @POST("reports")
    Call<NewReportResponse> postNewReport(@Query("user_id") int user_id,
                                          @Query("description") String description,
                                          @Query("work_front") int work_front_id,
                                          @Query("area") int area_id,
                                          @Query("responsible") int responsible_user_id,
                                          @Query("planned_date") String planned_date,
                                          @Query("deadline") String deadline,
                                          @Query("state") String state,
                                          @Query("actions") String actions,
                                          @Query("aspect") String aspect,
                                          @Query("potential") String potential,
                                          @Query("inspections") String inspections,
                                          @Query("critical_risk") int critical_risk_id,
                                          @Query("observations") String observations,
                                          @Query("inform_id") int inform_id
    );

    @Multipart
    @POST("reports")
    Call<NewReportResponse> postNewReportWithImages(@Query("user_id") int user_id,
                                          @Query("description") String description,
                                          @Part("image") String imageBase64,
                                          @Query("work_front") int work_front_id,
                                          @Query("area") int area_id,
                                          @Query("responsible") int responsible_user_id,
                                          @Query("planned_date") String planned_date,
                                          @Query("deadline") String deadline,
                                          @Query("state") String state,
                                          @Query("actions") String actions,
                                          @Part("image_action") String imageActionBase64,
                                          @Query("aspect") String aspect,
                                          @Query("potential") String potential,
                                          @Query("inspections") String inspections,
                                          @Query("critical_risk") int critical_risk_id,
                                          @Query("observations") String observations,
                                          @Query("inform_id") int inform_id
    );


    // Edit report

    @POST("reports/{id}")
    Call<NewReportResponse> updateNewReport(@Path("id") int report_id,
                                          @Query("description") String description,
                                          @Query("work_front") int work_front_id,
                                          @Query("area") int area_id,
                                          @Query("responsible") int responsible_user_id,
                                          @Query("planned_date") String planned_date,
                                          @Query("deadline") String deadline,
                                          @Query("state") String state,
                                          @Query("actions") String actions,
                                          @Query("aspect") String aspect,
                                          @Query("potential") String potential,
                                          @Query("inspections") String inspections,
                                          @Query("critical_risk") int critical_risk_id,
                                          @Query("observations") String observations
    );

    @Multipart
    @POST("reports/{id}")
    Call<NewReportResponse> updateNewReportWithImages(@Path("id") int report_id,
                                                    @Query("description") String description,
                                                    @Part("image") String imageBase64,
                                                    @Query("work_front") int work_front_id,
                                                    @Query("area") int area_id,
                                                    @Query("responsible") int responsible_user_id,
                                                    @Query("planned_date") String planned_date,
                                                    @Query("deadline") String deadline,
                                                    @Query("state") String state,
                                                    @Query("actions") String actions,
                                                    @Part("image_action") String imageActionBase64,
                                                    @Query("aspect") String aspect,
                                                    @Query("potential") String potential,
                                                    @Query("inspections") String inspections,
                                                    @Query("critical_risk") int critical_risk_id,
                                                    @Query("observations") String observations
    );
}
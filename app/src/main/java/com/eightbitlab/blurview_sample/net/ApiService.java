package com.eightbitlab.blurview_sample.net;

import com.eightbitlab.blurview_sample.PatientDetail.PatientModel;
import com.eightbitlab.blurview_sample.PatientDetail.VisitDetailModel;
import com.eightbitlab.blurview_sample.PatientDetail.VisitSimpleModel;
import com.eightbitlab.blurview_sample.Patient_Create.CreatePatientActivity;
import com.eightbitlab.blurview_sample.Patient_Search.PageResult;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.VisitDetail.VisitCreateDto;
import com.eightbitlab.blurview_sample.VisitDetail.VisitDetailAggregateModel;
import com.eightbitlab.blurview_sample.VisitDetail.VisitTreatmentRecordItemModel;
import com.eightbitlab.blurview_sample.VisitDetail.VisitUpdateRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.eightbitlab.blurview_sample.VisitDetail.TreatmentRecordCreateRequest;
import com.eightbitlab.blurview_sample.VisitDetail.TreatmentRecordUpdateRequest;
import com.eightbitlab.blurview_sample.VisitDetail.VisitTreatmentRecordItemModel;

import java.util.List;

import retrofit2.http.DELETE;

public interface ApiService {

    // =========================
    // 病人档案
    // =========================

    @GET("api/Patients")
    Call<ReturnInfo<List<PatientModel>>> getPatients();

    @GET("api/Patients/search")
    Call<ReturnInfo<PageResult<PatientModel>>> searchPatients(
            @Query("keyword") String keyword
    );

    @POST("api/Patients")
    Call<ReturnInfo<String>> createPatient(@Body CreatePatientActivity.CreatePatientRequest req);

    @PUT("api/Patients/update/{patientId}")
    Call<ReturnInfo<Object>> updatePatient(
            @Path("patientId") String patientId,
            @Body PatientModel req
    );

    @GET("api/Patients/{patientId}")
    Call<ReturnInfo<PatientModel>> getByPatientId(@Path("patientId") String patientId);

    // =========================
    // 病历
    // =========================

    @GET("api/Visits/getVisits")
    Call<ReturnInfo<List<VisitSimpleModel>>> getVisitsByPatientId(@Query("patientId") String patientId);

    @GET("api/Visits/{visitNo}")
    Call<ReturnInfo<VisitDetailModel>> getVisitByVisitNo(@Path("visitNo") String visitNo);

    @GET("api/Visits/detail-aggregate")
    Call<ReturnInfo<VisitDetailAggregateModel>> getVisitDetailAggregate(@Query("visitNo") String visitNo);

    @POST("api/Visits/create")
    Call<ReturnInfo<String>> createVisit(@Body VisitCreateDto dto);

    @PUT("api/Visits/updateByVisitNo")
    Call<ReturnInfo<Object>> updateByVisitNo(@Body VisitUpdateRequest dto);

    // =========================
    // 附件
    // =========================

    @Multipart
    @POST("api/Storage/upload")
    Call<ReturnInfo<Object>> uploadAttachment(
            @Part MultipartBody.Part file,
            @Part("visitNo") RequestBody visitNo,
            @Part("patientId") RequestBody patientId,
            @Part("mediaType") RequestBody mediaType
    );

    @DELETE("api/Storage/{id}")
    Call<ReturnInfo<Object>> deleteAttachment(
            @Path("id") String id
    );

    // =========================
    // 初诊复诊记录
    // =========================

    @GET("api/Record")
    Call<ReturnInfo<List<VisitTreatmentRecordItemModel>>> listTreatmentRecords(
            @Query("visitNo") String visitNo
    );


    @POST("api/Record")
    Call<ReturnInfo<Object>> createTreatmentRecord(
            @Body TreatmentRecordCreateRequest req
    );

    @PUT("api/Record")
    Call<ReturnInfo<Object>> updateTreatmentRecord(
            @Body TreatmentRecordUpdateRequest req
    );

    @DELETE("api/Record")
    Call<ReturnInfo<Object>> deleteTreatmentRecord(
            @Query("id") long id,
            @Query("visitNo") String visitNo
    );

    // =========================
    // 健康检查
    // =========================

    @GET("api/Health/ping")
    Call<ReturnInfo<Object>> ping();
}
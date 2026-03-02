package com.eightbitlab.blurview_sample.net;
import com.eightbitlab.blurview_sample.PatientDetail.PatientModel;
import com.eightbitlab.blurview_sample.PatientDetail.VisitDetailModel;
import com.eightbitlab.blurview_sample.PatientDetail.VisitSimpleModel;
import com.eightbitlab.blurview_sample.ReturnInfo;
import com.eightbitlab.blurview_sample.Patient_Create.CreatePatientActivity;
import com.eightbitlab.blurview_sample.Patient_Search.PageResult;
import com.eightbitlab.blurview_sample.VisitDetail.VisitCreateDto;
import com.eightbitlab.blurview_sample.VisitDetail.VisitUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface ApiService {
    //模糊查询
    @GET("api/Patients/search")
    Call<ReturnInfo<PageResult<PatientModel>>> searchPatients(
            @Query("Name") String name,
            @Query("Phone") String phone,
            @Query("IDCard") String idCard,
            @Query("Address") String address,
            @Query("Allergy") String allergy,
            @Query("MedicalHistory") String medicalHistory
    );

    //根据 patientId 查询病人
    @GET("api/Patients/{patientId}")
    Call<ReturnInfo<PatientModel>> getByPatientId(@Path("patientId") String patientId);

    //新建病人档案
    @retrofit2.http.POST("api/Patients")
//    Call<ReturnInfo<Object>> createPatient(@retrofit2.http.Body CreatePatientActivity.CreatePatientRequest req);
    Call<ReturnInfo<String>> createPatient(@Body CreatePatientActivity.CreatePatientRequest req);

    //根据 PatientId 获取病历
    @GET("api/Visits/{patientId}/visits")
    Call<ReturnInfo<List<VisitSimpleModel>>> getVisitsByPatientId(@Path("patientId")String patientId);

    //更新档案信息
    @PUT("api/Patients/update/{patientId}")
    Call<ReturnInfo<Object>> updatePatient(@Path("patientId") String patientId,
                                           @Body PatientModel req);
    //获取病历 visit 详情
    @GET("api/Visits/{visitNo}")
    Call<ReturnInfo<VisitDetailModel>> getVisitByVisitNo(@Path("visitNo") String visitNo);

    //创建病历
    @POST("api/Visits")
    Call<ReturnInfo<String>> createVisit(@Body VisitCreateDto dto);

    //根据病历号更新病历
    @PUT("api/Visits/updateByVisitNo")  // 这个路径按你实际 controller route 改
    Call<ReturnInfo> updateByVisitNo(@Body VisitUpdateRequest dto);
}

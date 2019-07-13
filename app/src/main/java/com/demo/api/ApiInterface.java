package com.demo.api;


import com.demo.model.MainModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET(ApiConstants.API_SEARCH_BY_DATE)
    Call<MainModel> getData(@Query("tags") String tags,
                            @Query("page") String page);

}

package com.leichao.retrofit;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * 通用api接口
 * Created by leichao on 2017/3/7.
 */

public interface IStores {

    //通用GET请求，需要拼接好参数的url
    @GET
    Observable<JsonObject> get(@Url String url);

    //获取谷歌key
    @GET("api/tms/googleKey/getGoogleKey?isVerify=0")
    Observable<ApiResult<String>> getGoogleKey();

    // 上传异常信息
    @FormUrlEncoded
    @POST("api/tms/error/upErrorInfo")
    Observable<ApiResult<Object>> upErrorInfo(
            @FieldMap Map<String, Object> params
    );

}

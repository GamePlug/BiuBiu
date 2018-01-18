package com.leichao.retrofit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 转换工厂, 将http返回结果转换为ApiResult
 */
public final class ConverterFactory extends Converter.Factory {

    public static ConverterFactory create() {
        return new ConverterFactory();
    }

    private ConverterFactory() {

    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new BeanRequestBodyConverter<>(type);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {
        return new BeanResponseBodyConverter<>(type);
    }

    static final class BeanRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        private final Type type;
        public BeanRequestBodyConverter(Type type) {
            this.type = type;
        }
        @Override
        public RequestBody convert(T value) throws IOException {
            String str = new Gson().toJson(value, type);
            return RequestBody.create(MEDIA_TYPE, str);
        }
    }

    static final class BeanResponseBodyConverter<T> implements Converter<ResponseBody, ApiResult<T>> {
        private final Type type;
        public BeanResponseBodyConverter(Type type) {
            this.type = type;
        }
        @Override
        public ApiResult<T> convert(ResponseBody value) throws IOException {
            String responseStr = value.string();
            RetrofitUtil.log("result:" + responseStr);
            ApiResult<T> apiResult;
            try {
                JsonObject jsonObject = new JsonParser().parse(responseStr).getAsJsonObject();
                String code = jsonObject.get("code").getAsString();
                if ("success".equals(code)) {
                    apiResult = new Gson().fromJson(responseStr, type);
                } else {
                    apiResult = new ApiResult<>();
                    apiResult.setType(false);
                    apiResult.setCode(code);
                    apiResult.setMessage(jsonObject.get("message").getAsString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                apiResult = new ApiResult<>();
                apiResult.setStatus(ApiResult.Status.ERROR_JSON);
                apiResult.setMessage("json_error");
            }
            apiResult.setJsonStr(responseStr);
            return apiResult;
        }
    }
}

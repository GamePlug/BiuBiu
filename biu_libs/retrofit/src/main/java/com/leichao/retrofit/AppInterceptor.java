package com.leichao.retrofit;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * OkHttp拦截操作，添加默认的参数，完整url的打印等
 * Created by Administrator on 2017/3/3.
 */
public class AppInterceptor implements Interceptor {

    // 单点登录传递的参数
    public static final String SECRET = "secret";
    public static final String SECRET_KEY = "secretKey";
    public static final String IS_VERIFY = "isVerify";// 是否需要验证单点登录
    // 接口需要默认传递的参数
    public static final String USER_ID = RetrofitConfig.getInstance().getUserIdKey();
    public static final String NONCE_STR = "nonce_str";
    public static final String SIGN = "sign";
    public static final String LANGUAGE = "language";
    public static final String VERSION = "version";
    public static final String CLIENT = "client";
    // 加密的key与值
    public static final String KEY_VALUE="key=aaaaaa";
    // 数据格式
    public static final String APPLICATION_FORM_URL = "application/x-www-form-urlencoded;charset=UTF-8";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        switch (request.method()) {
            case "GET":
                request = normalGet(request);
                break;
            case "POST":
                RequestBody body = request.body();
                MediaType contentType = body != null ? body.contentType() : null;
                if (body != null && contentType != null && contentType.toString().contains(MULTIPART_FORM_DATA)) {
                    request = uploadPost(request);
                } else {
                    request = normalPost(request);
                }
                break;
        }
        return chain.proceed(request);
    }

    /**
     * 普通get请求统一处理的操作
     */
    private Request normalGet(Request request) {
        HttpUrl url = request.url().newBuilder()
                .addQueryParameter(NONCE_STR, String.valueOf(System.currentTimeMillis()))// 增加nonce_str参数
                .addQueryParameter(LANGUAGE, getLanguage())// 增加language参数
                .addQueryParameter(VERSION, getVersion())// 增加version参数
                .addQueryParameter(CLIENT, "android")// 增加client参数
                .build();

        // 增加userId参数
        if (!TextUtils.isEmpty(getUserId()) && !url.queryParameterNames().contains(USER_ID)) {
            url = url.newBuilder().addQueryParameter(USER_ID, getUserId()).build();
        }
        // 增加sign参数
        String sign = getSign(url.toString());
        url = url.newBuilder().addQueryParameter(SIGN, sign).build();
        // 增加单点登录验证,不参与签名
        if (!url.queryParameterNames().contains(IS_VERIFY)) {
            String secret = getSecret();
            if (TextUtils.isEmpty(secret)) {
                url = url.newBuilder().addQueryParameter(IS_VERIFY, "0").build();
            } else {
                url = url.newBuilder().addQueryParameter(IS_VERIFY, "1").build();
                url = url.newBuilder().addQueryParameter(SECRET, secret).build();
                url = url.newBuilder().addQueryParameter(SECRET_KEY, getSecretKey()).build();
            }
        }

        request = request.newBuilder()
                .addHeader("Connection", "close")
                .url(url)
                .build();
        RetrofitUtil.log("url:" + request.url());
        return request;
    }

    /**
     * 普通post请求统一处理的操作
     */
    private Request normalPost(Request request) {
        RequestBody formBody = new FormBody.Builder()
                .add(NONCE_STR, String.valueOf(System.currentTimeMillis()))// 增加nonce_str参数
                .add(LANGUAGE, getLanguage())// 增加language参数
                .add(VERSION, getVersion())// 增加version参数
                .add(CLIENT, "android")// 增加client参数
                .build();
        String postBodyString = bodyToString(request.body());
        postBodyString += ((postBodyString.length() > 0) ? "&" : "") + bodyToString(formBody);

        // 增加userId参数
        Uri uri = Uri.parse(request.url() + "?" + postBodyString);
        if (!TextUtils.isEmpty(getUserId()) && !uri.getQueryParameterNames().contains(USER_ID)) {
            postBodyString += "&" + USER_ID + "=" + getUserId();
        }
        // 增加sign参数
        String sign = getSign(request.url() + "?" + postBodyString);
        postBodyString += "&" + SIGN + "=" + sign;
        // 增加单点登录验证,不参与签名
        if (!uri.getQueryParameterNames().contains(IS_VERIFY)) {
            String secret = getSecret();
            if (TextUtils.isEmpty(secret)) {
                postBodyString += "&" + IS_VERIFY + "=0";
            } else {
                postBodyString += "&" + IS_VERIFY + "=1";
                postBodyString += "&" + SECRET + "=" + secret;
                postBodyString += "&" + SECRET_KEY + "=" + getSecretKey();
            }
        }

        request = request.newBuilder()
                .addHeader("Connection", "close")
                .post(RequestBody.create(
                        MediaType.parse(APPLICATION_FORM_URL),
                        postBodyString))
                .build();
        RetrofitUtil.log("url:" + request.url() + "?" + postBodyString);
        return request;
    }

    /**
     * 文件上传统一处理的操作
     */
    private Request uploadPost(Request request) {
        RequestBody body = request.body();
        if (body == null || !(body instanceof MultipartBody) ) {
            return request;
        }
        List<MultipartBody.Part> parts = ((MultipartBody) body).parts();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(MultipartBody.Part part : parts) {
            builder.addPart(part);
        }
        String uploadBodyString = partsToString(parts);

        // 增加nonce_str参数
        String nonce_str = String.valueOf(System.currentTimeMillis());
        builder.addFormDataPart(NONCE_STR, nonce_str);
        uploadBodyString += (uploadBodyString.equals("") ? "" : "&") + NONCE_STR + "=" + nonce_str;
        // 增加language参数
        String language = getLanguage();
        builder.addFormDataPart(LANGUAGE, language);
        uploadBodyString += "&" + LANGUAGE + "=" + language;
        // 增加version参数
        String version = getVersion();
        builder.addFormDataPart(VERSION, version);
        uploadBodyString += "&" + VERSION + "=" + version;
        // 增加client参数
        String client = "android";
        builder.addFormDataPart(CLIENT, client);
        uploadBodyString += "&" + CLIENT + "=" + client;
        // 增加userId参数
        Uri uri = Uri.parse(request.url() + "?" + uploadBodyString);
        if (!TextUtils.isEmpty(getUserId()) && !uri.getQueryParameterNames().contains(USER_ID)) {
            builder.addFormDataPart(USER_ID, getUserId());
            uploadBodyString += "&" + USER_ID + "=" + getUserId();
        }
        // 增加sign参数
        String sign = getSign(request.url() + "?" + uploadBodyString);
        builder.addFormDataPart(SIGN, sign);
        uploadBodyString += "&" + SIGN + "=" + sign;
        // 增加单点登录验证,不参与签名
        if (!uri.getQueryParameterNames().contains(IS_VERIFY)) {
            String secret = getSecret();
            if (TextUtils.isEmpty(secret)) {
                builder.addFormDataPart(IS_VERIFY, "0");
                uploadBodyString += "&" + IS_VERIFY + "=0";
            } else {
                builder.addFormDataPart(IS_VERIFY, "1");
                uploadBodyString += "&" + IS_VERIFY + "=1";
                builder.addFormDataPart(SECRET, getSecret());
                uploadBodyString += "&" + SECRET + "=" + getSecret();
                builder.addFormDataPart(SECRET_KEY, getSecretKey());
                uploadBodyString += "&" + SECRET_KEY + "=" + getSecretKey();
            }
        }

        MultipartBody multiBody = builder.build();
        request = request.newBuilder()
                .addHeader("Connection", "close")
                .post(multiBody)
                .build();
        RetrofitUtil.log("url:" + request.url() + "?" + uploadBodyString);
        return request;
    }

    /**
     * 将RequestBody转换成对应的字符串
     */
    private String bodyToString(final RequestBody request){
        try {
            final Buffer buffer = new Buffer();
            if(request != null) {
                request.writeTo(buffer);
                return buffer.readUtf8();
            } else {
                return "";
            }
        } catch (final IOException e) {
            return "";
        }
    }

    /**
     * 将List<MultipartBody.Part>转换成对应的字符串
     */
    private String partsToString(List<MultipartBody.Part> parts) {
        String partsBodyString = "";
        for(MultipartBody.Part part : parts) {
            try {
                Field field = MultipartBody.Part.class.getDeclaredField("headers");
                field.setAccessible(true);
                Headers headers = (Headers) field.get(part);
                String cd = headers.get("Content-Disposition");
                if (!TextUtils.isEmpty(cd) && !cd.contains("filename")) {// 只打印非文件类型的字段
                    String key = cd.split("\"").length >= 2 ? cd.split("\"")[1] : "";
                    Field field2 = MultipartBody.Part.class.getDeclaredField("body");
                    field2.setAccessible(true);
                    RequestBody body = (RequestBody)field2.get(part);
                    String value = bodyToString(body);
                    partsBodyString += (partsBodyString.equals("") ? "" : "&") + key + "=" + value;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return partsBodyString;
    }

    /**
     * 生成加密签名
     */
    public static String getSign(String url) {
        TreeSet<String> ts = new TreeSet<>();
        Uri uri = Uri.parse(url);
        Set<String> strSet = uri.getQueryParameterNames();
        for (String key : strSet) {
            if (!key.equals(SECRET) && !key.equals(SECRET_KEY) && !key.equals(IS_VERIFY)) {// 单点登录参数不参与签名
                String value = uri.getQueryParameter(key);
                if (!TextUtils.isEmpty(value)) {// 空值不参与签名
                    ts.add(key + "=" + value);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String ob : ts) {
            sb.append(ob);
            sb.append("&");
        }
        sb.append(KEY_VALUE);
        String srcStr = sb.toString();
        String md5Str = RetrofitUtil.md5(srcStr);
        md5Str=md5Str.toUpperCase();
        return md5Str;
    }

    /**
     * 获取userId
     */
    private String getUserId() {
        String userId = RetrofitConfig.getInstance().getUserId();
        return userId != null ? userId : "";
    }

    /**
     * 获取secret
     */
    private String getSecret() {
        String secret = RetrofitConfig.getInstance().getSecret();
        return secret != null ? secret : "";
    }

    /**
     * 获取secretKey
     */
    private String getSecretKey() {
        return RetrofitConfig.getInstance().getSecretKey();
    }

    /**
     * 获取语言类型
     */
    private String getLanguage() {
        return RetrofitConfig.getInstance().getLanguage();
    }

    /**
     * 获取版本号
     */
    private String getVersion() {
        return RetrofitConfig.getInstance().getVersion();
    }

}

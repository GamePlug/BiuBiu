package com.leichao.retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class AppClient {

    /**
     * OkHttp网络请求的超时时间(30秒)
     */
    public static final long TIMEOUT = 30;

    public static Retrofit mRetrofit;

    public static Retrofit otherRetrofit;

    /**
     * 访问自己的服务器，所需Retrofit
     */
    public static Retrofit retrofit() {
        if (mRetrofit == null) {
            Cache cache = new Cache(
                    RetrofitUtil.getCacheDir(RetrofitConfig.getInstance().getApplication()),// 缓存文件位置
                    5 * 1024 * 1024// 缓存文件最大限制大小5M
            );
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)// 连接失败重连，默认为true，可以不加
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)// 链接超时
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)// 写入超时
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)// 读取超时
                    //.cookieJar(new CookiesManager())// 开启cookie功能，将cookie序列化到本地，暂不开启
                    //.sslSocketFactory(new SSLSocketFactory())// 支持https协议，暂时未支持
                    .cache(cache)// 设置OkHttp缓存
                    .addInterceptor(new AppInterceptor())// 添加自定义拦截操作//HttpLoggingInterceptor
                    .addInterceptor(new CacheInterceptor())
                    .build();

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitConfig.getInstance().getBaseUrl())
                    .addConverterFactory(ConverterFactory.create())// Gson解析转换工厂//GsonConverterFactory
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())// RxJava适配器
                    .client(okHttpClient)
                    .build();
        }
        return mRetrofit;
    }

    /**
     * 访问其他服务器，所需Retrofit
     */
    public static Retrofit retrofitOther() {
        if (otherRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)// 连接失败重连，默认为true，可以不加
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)// 链接超时
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)// 写入超时
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)// 读取超时
                    //.cookieJar(new CookiesManager())// 开启cookie功能，将cookie序列化到本地，暂不开启
                    //.sslSocketFactory(new SSLSocketFactory())// 支持https协议，暂时未支持
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            RetrofitUtil.log("url:" + request.url());
                            return chain.proceed(request);
                        }
                    })
                    .build();

            otherRetrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitConfig.getInstance().getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())// Gson解析转换工厂
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())// RxJava适配器
                    .client(okHttpClient)
                    .build();

        }
        return otherRetrofit;
    }

}

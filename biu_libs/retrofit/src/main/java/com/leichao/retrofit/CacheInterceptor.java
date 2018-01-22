package com.leichao.retrofit;

import android.text.TextUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 缓存拦截器
 * 使用方法(给接口配置Cache-Control头部)：
 * 1.在接口方法上面配置注解：@Headers(CacheInterceptor.CACHE_DAY_7)
 * 2.在接口参数前配置注解：@Header(CacheInterceptor.CACHE)
 * 配置了该头部注解，表示当数据获取失败时从缓存获取，其中max-age的值表示缓存时间(秒)
 * Created by leichao on 2017/6/28.
 */

public class CacheInterceptor implements Interceptor {

    // 用于@Header注解
    public static final String CACHE_KEY = "Cache-Control";// 缓存头的key
    public static final String DAY_7 = "max-age=" + ( 7 * 24 * 60 * 60 );// 7天缓存
    public static final String FORCE_CACHE = CacheControl.FORCE_CACHE.toString();// 强制缓存
    public static final String FORCE_NETWORK = CacheControl.FORCE_NETWORK.toString();// 强制网络
    // 用于@Headers注解
    public static final String CACHE_DAY_7 = CACHE_KEY + ": " + DAY_7;// 当从网络获取数据失败时，则取7天内的缓存。
    public static final String CACHE_FORCE_CACHE = CACHE_KEY + ": " + FORCE_CACHE;// 不管有没有缓存，只取缓存。
    public static final String CACHE_FORCE_NETWORK = CACHE_KEY + ": " + FORCE_NETWORK;// 不管有没有缓存，只取网络。

    public static final String NONCE_VERSION = "1001";// 将随机数参数改为缓存版本
    public static final int REPEAT_TIMES = 2;// 失败重试次数

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        CacheControl cacheControl = request.cacheControl();

        // OkHttp缓存只支持GET模式
        if ("GET".equals(request.method()) && !TextUtils.isEmpty(cacheControl.toString())) {
            HttpUrl url = request.url().newBuilder()
                    .setQueryParameter(AppInterceptor.NONCE_STR, NONCE_VERSION)// 修改随机数为缓存版本号
                    .removeAllQueryParameters(AppInterceptor.SIGN)// 移除签名，因为参数变化需要重新签名
                    .build();
            // 增加修改后的签名
            String sign = AppInterceptor.getSign(url.toString());
            url = url.newBuilder().addQueryParameter(AppInterceptor.SIGN, sign).build();
            // 给参数排序，因为参数顺序不同对应的缓存不同
            TreeSet<String> ts = new TreeSet<>();
            ts.addAll(url.queryParameterNames());
            HttpUrl.Builder builder = url.newBuilder();
            for (String key : ts) {
                builder.setQueryParameter(key, url.queryParameter(key));
            }
            url = builder.build();
            //L.e("缓存url:" + url);
            if (CacheControl.FORCE_CACHE.toString().equals(cacheControl.toString())) {
                // 修改request的url，直接从缓存获取数据
                request = request.newBuilder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                return getCacheResponse(chain, request, cacheControl);
            } else if (CacheControl.FORCE_NETWORK.toString().equals(cacheControl.toString())) {
                // 修改request的url，直接从网络获取数据
                request = request.newBuilder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
                return getCacheResponse(chain, request, cacheControl);
            } else {
                // 修改request的url，先从网络获取数据，失败时再从缓存获取数据
                request = request.newBuilder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
                return getCacheResponse(chain, request, cacheControl, REPEAT_TIMES);
            }
        } else {
            return getResponse(chain, request, REPEAT_TIMES);
        }
    }

    /**
     * 执行请求，并在发生异常时重连，获取数据失败时取缓存
     * @param cacheControl CacheControl
     * @param repeat 异常重连次数
     */
    private Response getCacheResponse(Chain chain, Request request, CacheControl cacheControl, int repeat) throws IOException {
        try {
            return getCacheResponse(chain, request, cacheControl);
        } catch (Exception e) {
            // 当重试次数为0或者超时，则取缓存
            if (repeat == 0 || e instanceof SocketTimeoutException) {
                //L.e("获取数据失败，从缓存获取：");
                request = request.newBuilder()
                        .cacheControl(new CacheControl.Builder().onlyIfCached()
                                .maxStale(cacheControl.maxAgeSeconds(), TimeUnit.SECONDS).build())
                        .build();
                return getCacheResponse(chain, request, cacheControl);
            } else {
                return getCacheResponse(chain, request, cacheControl, repeat-1);
            }
        }
    }

    /**
     * 执行请求，并给响应头添加Cache-Control头部信息
     * @param cacheControl CacheControl
     */
    private Response getCacheResponse(Chain chain, Request request, CacheControl cacheControl) throws IOException {
        Response response = chain.proceed(request);
        return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())// "max-age=640000"
                .removeHeader("Pragma")
                .build();
    }

    /**
     * 执行请求，并在发生异常时重连
     * @param repeat 异常重连次数
     */
    private Response getResponse(Chain chain, Request request, int repeat) throws IOException {
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            // 当重试次数为0或者超时，则抛出异常
            if (repeat == 0 || e instanceof SocketTimeoutException) {
                throw e;
            } else {
                return getResponse(chain, request, repeat-1);
            }
        }
    }

}

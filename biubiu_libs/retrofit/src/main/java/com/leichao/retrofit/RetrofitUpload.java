package com.leichao.retrofit;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 生成上传文件所需的RequestBody与Map<String, RequestBody>
 * Created by leichao on 2016/4/26.
 */
public class RetrofitUpload {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static class Builder {

        Map<String, RequestBody> params;

        public Builder() {
            params = new LinkedHashMap<>();
        }

        public Builder addFile(String key, File file) {
            RequestBody requestBody = RequestBody
                    .create(MediaType.parse(MULTIPART_FORM_DATA), file);
            params.put(key + "\"; filename=\"" + file.getName(), requestBody);
            return this;
        }

        public Builder addString(String key, String value) {
            RequestBody requestBody = RequestBody
                    .create(MediaType.parse(MULTIPART_FORM_DATA), value);
            params.put(key, requestBody);
            return this;
        }

        public Builder addInt(String key, int value) {
            return addString(key, String.valueOf(value));
        }

        // 因为此方法未使用到，所以暂时注释，用来解除方法未被使用的警告
        /*public Builder addBoolean(String key, boolean value) {
            return addString(key, String.valueOf(value));
        }*/

        public Map<String, RequestBody> build() {
            return params;
        }

    }

}

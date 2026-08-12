
package Ir.co.tfs.farazaman.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GpxUploader{

    public interface UploadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void uploadFile(Context context, File zipFile, String token, UploadCallback callback) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(NetworkTimeouts.UPLOAD_CONNECT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(NetworkTimeouts.UPLOAD_READ_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(NetworkTimeouts.UPLOAD_WRITE_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", zipFile.getName(),
                        RequestBody.create(zipFile, MediaType.parse("application/zip")))
                .build();

        Request request = new Request.Builder()
                .url(SelectedServerUrl.apiUrl(preferences, "api/GisGeolocation/upload"))
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GpxUploader", "Upload failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.message());
                }
            }
        });
    }
}

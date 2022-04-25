package com.example.bluetoothapp;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendToCloudHelper {


    public static final String mockServer = "https://8a56f495-e1ec-4849-941c-8aab59c4631f.mock.pstmn.io/device";
    // TODO: Send actual data
    public void sendData() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"name\": \"deviceName\",\n    \"address\": \"deviceAddress\",\n    \"services\": [\n        {\n        \"uuid\": \"serviceUUID\",\n        \"characteristics\": [\"characteristic1UUID\", \"characteristic2UUID\"]\n        }\n    ]\n}");
        Request request = new Request.Builder()
                .url(mockServer)
                .method("PUT", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response;
        //okhttp allows us to make asynchronous requests using the enqueue method, instead of creating threads
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unsuccessful: " + response);
                }
                if(response.code() == 200){
                    String responseData = response.body().string();
                    Log.d("SERVER: ", responseData);
                }else{
                    //Server problem
                    Log.d("SERVER PROBLEM", "COULD NOT SEND DATA");
                }
            }
        });

    }


}

package com.example.android.bakingapp.util;



import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofitManager {

    private NetService mAPIService;

    public NetService getAPIService() {
        if (mAPIService == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(NetService.RECIPE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mAPIService = retrofit.create(NetService.class);
        }
        return mAPIService;
    }

}

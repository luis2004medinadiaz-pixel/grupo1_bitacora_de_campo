package com.example.bitacora.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest body);

    @POST("api/auth/registro")
    Call<Void> registro(@Body AuthRequest body);
}

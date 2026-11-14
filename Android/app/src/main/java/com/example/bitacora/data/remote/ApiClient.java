package com.example.bitacora.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Emulador: 10.0.2.2  |  Teléfono físico: IP de tu PC en la red (ej. 192.168.1.10)
    // Si cambias esta IP, actualízala también en res/xml/network_security_config.xml
    public static final String BASE_URL = "http://10.0.2.2:5080/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // BASIC registra solo método, URL y código: NUNCA cuerpos (contraseñas y token)
            // ni encabezados. Además se oculta Authorization por si se sube el nivel.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            logging.redactHeader("Authorization");

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static RegistroApi getApi() {
        return getRetrofit().create(RegistroApi.class);
    }

    public static AuthApi getAuthApi() {
        return getRetrofit().create(AuthApi.class);
    }
}

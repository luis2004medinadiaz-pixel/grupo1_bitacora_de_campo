package com.example.bitacora.data.remote;

import com.example.bitacora.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        SessionManager session = SessionManager.getInstance();

        boolean esAuth = original.url().encodedPath().startsWith("/api/auth/");
        String token = session.getToken();
        boolean enviaToken = token != null && !esAuth;

        Request.Builder builder = original.newBuilder();
        if (enviaToken) {
            builder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401 && enviaToken) {
            session.clear();
            session.notifyExpired("Tu sesión expiró o no es válida. Inicia sesión nuevamente.");
        }
        return response;
    }
}

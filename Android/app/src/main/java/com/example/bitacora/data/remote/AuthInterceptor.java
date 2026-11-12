package com.example.bitacora.data.remote;

import com.example.bitacora.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Agrega "Authorization: Bearer <token>" a cada petición (excepto login y registro) y,
 * si el servidor responde 401 a una petición que sí llevaba token, borra la sesión y avisa a la UI.
 * El 403 NO cierra la sesión: el usuario sigue autenticado pero sin permiso para esa acción.
 */
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

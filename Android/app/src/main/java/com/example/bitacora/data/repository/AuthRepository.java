package com.example.bitacora.data.repository;

import com.example.bitacora.data.local.SessionManager;
import com.example.bitacora.data.remote.ApiClient;
import com.example.bitacora.data.remote.ApiErrors;
import com.example.bitacora.data.remote.AuthApi;
import com.example.bitacora.data.remote.AuthRequest;
import com.example.bitacora.data.remote.AuthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    public interface ResultCallback { void onResult(String error); }

    private final AuthApi api = ApiClient.getAuthApi();

    public void login(String correo, String password, ResultCallback cb) {
        api.login(new AuthRequest(correo, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionManager.getInstance().saveSession(response.body());
                    cb.onResult(null);
                } else if (response.code() == 401) {
                    // Mensaje único, igual que el de la API: no indica qué dato falló.
                    cb.onResult("Correo o contraseña incorrectos.");
                } else if (response.code() == 400) {
                    cb.onResult("Escribe tu correo y tu contraseña.");
                } else {
                    cb.onResult(ApiErrors.message(response.code()));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                cb.onResult("Sin conexión con el servidor: " + t.getMessage());
            }
        });
    }

    public void registro(String correo, String password, ResultCallback cb) {
        api.registro(new AuthRequest(correo, password)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onResult(null);
                } else if (response.code() == 409) {
                    cb.onResult("Ese correo ya está registrado.");
                } else if (response.code() == 400) {
                    cb.onResult("Revisa los datos: correo válido y contraseña de 8 a 72 caracteres con letras y números.");
                } else {
                    cb.onResult(ApiErrors.message(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onResult("Sin conexión con el servidor: " + t.getMessage());
            }
        });
    }
}

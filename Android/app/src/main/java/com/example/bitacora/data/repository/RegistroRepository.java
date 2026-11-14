package com.example.bitacora.data.repository;

import com.example.bitacora.data.remote.RegistroApi;
import com.example.bitacora.data.remote.RegistroDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroRepository {
    private final RegistroApi api;

    public interface CallbackList { void onResult(List<RegistroDto> data, String error); }
    public interface CallbackOne { void onResult(RegistroDto data, String error); }
    public interface CallbackVoid { void onResult(String error); }

    public RegistroRepository() {
        this.api = com.example.bitacora.data.remote.ApiClient.getApi();
    }

    public void getAll(CallbackList cb) {
        api.getAll().enqueue(new Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(Call<List<RegistroDto>> call, Response<List<RegistroDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onResult(response.body(), null);
                } else {
                    cb.onResult(null, com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<List<RegistroDto>> call, Throwable t) {
                cb.onResult(null, "Sin conexión o tiempo de espera agotado: " + t.getMessage());
            }
        });
    }

    /** Igual que getAll pero sobre una ruta relativa (la usa la pantalla de comparativa). */
    public void getAllFrom(String path, CallbackList cb) {
        api.getFrom(path).enqueue(new Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(Call<List<RegistroDto>> call, Response<List<RegistroDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onResult(response.body(), null);
                } else {
                    cb.onResult(null, com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<List<RegistroDto>> call, Throwable t) {
                cb.onResult(null, "Sin conexión o tiempo de espera agotado: " + t.getMessage());
            }
        });
    }

    public void getById(int id, CallbackOne cb) {
        api.getById(id).enqueue(new Callback<RegistroDto>() {
            @Override
            public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onResult(response.body(), null);
                } else {
                    cb.onResult(null, com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<RegistroDto> call, Throwable t) {
                cb.onResult(null, "Sin conexión: " + t.getMessage());
            }
        });
    }

    public void create(RegistroDto body, CallbackOne cb) {
        api.create(body).enqueue(new Callback<RegistroDto>() {
            @Override
            public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onResult(response.body(), null);
                } else {
                    cb.onResult(null, com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<RegistroDto> call, Throwable t) {
                cb.onResult(null, "Sin conexión: " + t.getMessage());
            }
        });
    }

    public void update(int id, RegistroDto body, CallbackOne cb) {
        api.update(id, body).enqueue(new Callback<RegistroDto>() {
            @Override
            public void onResponse(Call<RegistroDto> call, Response<RegistroDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onResult(response.body(), null);
                } else {
                    cb.onResult(null, com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<RegistroDto> call, Throwable t) {
                cb.onResult(null, "Sin conexión: " + t.getMessage());
            }
        });
    }

    public void delete(int id, CallbackVoid cb) {
        api.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onResult(null);
                } else {
                    cb.onResult(com.example.bitacora.data.remote.ApiErrors.message(response.code()));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onResult("Sin conexión: " + t.getMessage());
            }
        });
    }
}
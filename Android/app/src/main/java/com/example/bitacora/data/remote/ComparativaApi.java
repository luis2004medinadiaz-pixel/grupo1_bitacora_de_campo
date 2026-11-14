package com.example.bitacora.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

/** Solo para la pantalla de laboratorio: permite elegir la ruta y el token en cada llamada. */
public interface ComparativaApi {
    @GET
    Call<List<RegistroDto>> getRegistros(@Url String ruta, @Header("Authorization") String token);
}

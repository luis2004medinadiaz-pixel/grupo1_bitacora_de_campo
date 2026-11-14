package com.example.bitacora.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface RegistroApi {
    @GET("api/registros")
    Call<List<RegistroDto>> getAll();

    /** Solo para la comparativa: consulta una ruta relativa cualquiera (endpoints de prueba). */
    @GET
    Call<List<RegistroDto>> getFrom(@Url String path);

    @GET("api/registros/{id}")
    Call<RegistroDto> getById(@Path("id") int id);

    @POST("api/registros")
    Call<RegistroDto> create(@Body RegistroDto body);

    @PUT("api/registros/{id}")
    Call<RegistroDto> update(@Path("id") int id, @Body RegistroDto body);

    @DELETE("api/registros/{id}")
    Call<Void> delete(@Path("id") int id);
}
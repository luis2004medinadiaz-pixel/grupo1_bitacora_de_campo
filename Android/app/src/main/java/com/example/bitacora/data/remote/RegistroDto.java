package com.example.bitacora.data.remote;

import com.google.gson.annotations.SerializedName;

public class RegistroDto {
    @SerializedName("id")
    public int id;

    @SerializedName("titulo")
    public String titulo;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("fotoBase64")
    public String fotoBase64;

    @SerializedName("fechaCreacion")
    public String fechaCreacion;
}
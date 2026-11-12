package com.example.bitacora.data.remote;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    public String token;

    @SerializedName("expiraEn")
    public String expiraEn;

    @SerializedName("rol")
    public String rol;

    @SerializedName("correo")
    public String correo;

    @SerializedName("usuarioId")
    public int usuarioId;
}

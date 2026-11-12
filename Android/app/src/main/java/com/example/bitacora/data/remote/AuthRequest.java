package com.example.bitacora.data.remote;

import com.google.gson.annotations.SerializedName;

/** Cuerpo para /api/auth/login y /api/auth/registro (ambos reciben correo y password). */
public class AuthRequest {
    @SerializedName("correo")
    public final String correo;

    @SerializedName("password")
    public final String password;

    public AuthRequest(String correo, String password) {
        this.correo = correo;
        this.password = password;
    }
}

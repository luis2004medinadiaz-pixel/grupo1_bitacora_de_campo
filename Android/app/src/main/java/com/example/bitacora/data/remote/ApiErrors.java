package com.example.bitacora.data.remote;

/** Mensajes amigables según el código HTTP. */
public class ApiErrors {
    public static String message(int code) {
        switch (code) {
            case 400: return "Datos inválidos. Revisa la información enviada.";
            case 401: return "Tu sesión expiró o no es válida. Inicia sesión nuevamente.";
            case 403: return "No tienes permiso para realizar esta acción.";
            case 404: return "No se encontró el recurso solicitado.";
            default:  return "Error del servidor: " + code;
        }
    }
}

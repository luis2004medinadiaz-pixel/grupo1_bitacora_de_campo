package com.example.bitacora.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.example.bitacora.data.remote.AuthResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Guarda la sesión (token JWT, rol, correo) cifrada con EncryptedSharedPreferences.
 * La clave de cifrado vive en el Android Keystore, así que el token no queda en texto plano
 * en el almacenamiento del dispositivo (a diferencia de SharedPreferences comunes).
 */
public class SessionManager {
    private static final String FILE = "bitacora_session";
    private static final String K_TOKEN = "token";
    private static final String K_EXPIRA = "expira_ms";
    private static final String K_ROL = "rol";
    private static final String K_CORREO = "correo";
    private static final String K_USER_ID = "user_id";

    private static SessionManager instance;

    private final SharedPreferences prefs;
    private final MutableLiveData<String> expiredEvent = new MutableLiveData<>();

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager no inicializado (falta BitacoraApp en el manifiesto).");
        }
        return instance;
    }

    private SessionManager(Context context) {
        SharedPreferences p;
        try {
            p = open(context);
        } catch (GeneralSecurityException | IOException e) {
            // Archivo cifrado corrupto (por ejemplo, cambió el Keystore): se reinicia la sesión.
            context.deleteSharedPreferences(FILE);
            try {
                p = open(context);
            } catch (GeneralSecurityException | IOException e2) {
                throw new IllegalStateException("No se pudo abrir el almacenamiento seguro.", e2);
            }
        }
        this.prefs = p;
    }

    private static SharedPreferences open(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
    }

    public void saveSession(AuthResponse r) {
        prefs.edit()
                .putString(K_TOKEN, r.token)
                .putLong(K_EXPIRA, parseUtcMillis(r.expiraEn))
                .putString(K_ROL, r.rol)
                .putString(K_CORREO, r.correo)
                .putInt(K_USER_ID, r.usuarioId)
                .apply();
    }

    public String getToken() { return prefs.getString(K_TOKEN, null); }
    public String getRol() { return prefs.getString(K_ROL, null); }
    public String getCorreo() { return prefs.getString(K_CORREO, null); }
    public int getUserId() { return prefs.getInt(K_USER_ID, -1); }
    public boolean isAdmin() { return "Admin".equals(getRol()); }

    /** Hay token guardado y todavía no ha vencido. */
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && System.currentTimeMillis() < prefs.getLong(K_EXPIRA, 0L);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    // ---- aviso de sesión vencida (lo dispara el interceptor al recibir 401) ----
    public LiveData<String> getExpiredEvent() { return expiredEvent; }
    public void notifyExpired(String message) { expiredEvent.postValue(message); }
    public void consumeExpiredEvent() { expiredEvent.setValue(null); }

    /** El servidor envía algo como 2026-11-11T18:10:00.123Z (UTC). minSdk 24: sin java.time. */
    private static long parseUtcMillis(String iso) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.parse(iso.substring(0, 19)).getTime();
        } catch (Exception e) {
            return 0L; // formato inesperado: se considera vencido
        }
    }
}

package com.example.bitacora;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitacora.data.local.SessionManager;

public abstract class BaseAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.getInstance().getExpiredEvent().observe(this, msg -> {
            if (msg != null) {
                SessionManager.getInstance().consumeExpiredEvent();
                goToLogin(msg);
            }
        });
    }

    protected boolean ensureSession() {
        SessionManager s = SessionManager.getInstance();
        if (!s.isLoggedIn()) {
            s.clear();
            goToLogin("Tu sesión expiró. Inicia sesión nuevamente.");
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureSession();
    }

    protected void logout() {
        SessionManager.getInstance().clear();
        goToLogin("Sesión cerrada correctamente.");
    }

    protected void goToLogin(String mensaje) {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (mensaje != null) i.putExtra(LoginActivity.EXTRA_MENSAJE, mensaje);
        startActivity(i);
        finish();
    }
}

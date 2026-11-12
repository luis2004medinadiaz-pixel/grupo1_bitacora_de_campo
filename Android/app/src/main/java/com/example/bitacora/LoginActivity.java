package com.example.bitacora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitacora.data.local.SessionManager;
import com.example.bitacora.data.repository.AuthRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_MENSAJE = "mensaje";

    private TextInputLayout tilCorreo, tilPassword;
    private TextInputEditText edtCorreo, edtPassword;
    private Button btnLogin;
    private ProgressBar progress;
    private TextView txtMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.getInstance().isLoggedIn()) {
            goMain();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        txtMensaje = findViewById(R.id.txtMensaje);

        String mensaje = getIntent().getStringExtra(EXTRA_MENSAJE);
        if (mensaje != null) {
            txtMensaje.setText(mensaje);
            txtMensaje.setVisibility(View.VISIBLE);
        }

        btnLogin.setOnClickListener(v -> intentarLogin());
        findViewById(R.id.btnIrRegistro).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void intentarLogin() {
        tilCorreo.setError(null);
        tilPassword.setError(null);

        String correo = text(edtCorreo);
        String password = text(edtPassword);

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Escribe un correo válido");
            ok = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Escribe tu contraseña");
            ok = false;
        }
        if (!ok) return;

        setLoading(true);
        new AuthRepository().login(correo, password, error -> {
            setLoading(false);
            if (error != null) {
                txtMensaje.setText(error);
                txtMensaje.setVisibility(View.VISIBLE);
            } else {
                goMain();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void goMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}

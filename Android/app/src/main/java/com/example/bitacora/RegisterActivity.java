package com.example.bitacora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitacora.data.repository.AuthRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout tilCorreo, tilPassword, tilConfirmar;
    private TextInputEditText edtCorreo, edtPassword, edtConfirmar;
    private Button btnRegistrar;
    private ProgressBar progress;
    private TextView txtMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);

        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmar = findViewById(R.id.edtConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progress = findViewById(R.id.progress);
        txtMensaje = findViewById(R.id.txtMensaje);

        btnRegistrar.setOnClickListener(v -> intentarRegistro());
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    private void intentarRegistro() {
        tilCorreo.setError(null);
        tilPassword.setError(null);
        tilConfirmar.setError(null);

        String correo = text(edtCorreo);
        String password = text(edtPassword);
        String confirmar = text(edtConfirmar);

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Escribe un correo válido");
            ok = false;
        }
        if (password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            tilPassword.setError("De 8 a 72 caracteres, con al menos una letra y un número");
            ok = false;
        }
        if (!password.equals(confirmar)) {
            tilConfirmar.setError("Las contraseñas no coinciden");
            ok = false;
        }
        if (!ok) return;

        setLoading(true);
        AuthRepository repo = new AuthRepository();
        repo.registro(correo, password, error -> {
            if (error != null) {
                setLoading(false);
                txtMensaje.setText(error);
                txtMensaje.setVisibility(View.VISIBLE);
                return;
            }
            repo.login(correo, password, loginError -> {
                setLoading(false);
                if (loginError != null) {
                    txtMensaje.setText(loginError);
                    txtMensaje.setVisibility(View.VISIBLE);
                } else {
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!loading);
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}

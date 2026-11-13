package com.example.bitacora;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitacora.data.local.SessionManager;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.repository.RegistroRepository;

public class DetailActivity extends BaseAuthActivity {
    private int id;
    private RegistroDto current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ensureSession()) return;
        setContentView(R.layout.activity_detail);

        id = getIntent().getIntExtra("id", -1);
        TextView t = findViewById(R.id.txtTitulo);
        TextView d = findViewById(R.id.txtDesc);
        TextView f = findViewById(R.id.txtFecha);
        ImageView img = findViewById(R.id.imgFoto);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        new RegistroRepository().getById(id, (data, err) -> {
            if (err != null) { Toast.makeText(this, err, Toast.LENGTH_LONG).show(); finish(); return; }
            current = data;
            SessionManager session = SessionManager.getInstance();
            boolean esAdmin = session.isAdmin();
            boolean esDueno = data.usuarioId != null && data.usuarioId == session.getUserId();
            btnDelete.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
            btnEdit.setVisibility(esAdmin || esDueno ? View.VISIBLE : View.GONE);
            t.setText(data.titulo);
            d.setText(data.descripcion);
            f.setText(data.fechaCreacion);
            if (data.fotoBase64 != null && !data.fotoBase64.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(data.fotoBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    img.setImageBitmap(bmp);
                    img.setVisibility(View.VISIBLE);
                } catch (Exception e) { img.setVisibility(View.GONE); }
            } else { img.setVisibility(View.GONE); }
        });

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, FormActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        });

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este registro?")
                .setPositiveButton("Sí", (di, w) -> new RegistroRepository().delete(id, err -> {
                    if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    else { Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show(); finish(); }
                }))
                .setNegativeButton("No", null)
                .show());
    }
}
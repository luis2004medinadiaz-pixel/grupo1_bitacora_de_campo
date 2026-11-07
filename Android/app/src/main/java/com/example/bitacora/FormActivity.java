package com.example.bitacora;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.repository.RegistroRepository;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FormActivity extends AppCompatActivity {
    private static final int REQ_CAMERA = 100;
    private static final int REQ_VOICE = 101;

    private EditText edtTitulo, edtDesc;
    private ImageView imgFoto;
    private String fotoBase64 = null;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDesc = findViewById(R.id.edtDesc);
        imgFoto = findViewById(R.id.imgFoto);
        Button btnFoto = findViewById(R.id.btnFoto);
        Button btnVoz = findViewById(R.id.btnVoz);
        Button btnSave = findViewById(R.id.btnSave);

        editId = getIntent().getIntExtra("id", -1);
        if (editId != -1) {
            new RegistroRepository().getById(editId, (data, err) -> {
                if (err != null) { Toast.makeText(this, err, Toast.LENGTH_LONG).show(); finish(); return; }
                edtTitulo.setText(data.titulo);
                edtDesc.setText(data.descripcion);
                fotoBase64 = data.fotoBase64;
                if (fotoBase64 != null) {
                    byte[] b = Base64.decode(fotoBase64, Base64.DEFAULT);
                    imgFoto.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                    imgFoto.setVisibility(View.VISIBLE);
                }
            });
        }

        btnFoto.setOnClickListener(v -> {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, REQ_CAMERA);
            else Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
        });

        btnVoz.setOnClickListener(v -> {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...");
            try { startActivityForResult(i, REQ_VOICE); }
            catch (Exception e) { Toast.makeText(this, "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show(); }
        });

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String titulo = edtTitulo.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (titulo.length() < 3 || titulo.length() > 100) {
            edtTitulo.setError("El título debe tener entre 3 y 100 caracteres");
            return;
        }
        if (desc.isEmpty() || desc.length() > 500) {
            edtDesc.setError("La descripción es obligatoria (máx 500)");
            return;
        }

        RegistroDto body = new RegistroDto();
        body.titulo = titulo;
        body.descripcion = desc;
        body.fotoBase64 = fotoBase64;

        if (editId == -1) {
            new RegistroRepository().create(body, (data, err) -> {
                if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                else { Toast.makeText(this, "Creado", Toast.LENGTH_SHORT).show(); finish(); }
            });
        } else {
            new RegistroRepository().update(editId, body, (data, err) -> {
                if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                else { Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show(); finish(); }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        if (requestCode == REQ_CAMERA) {
            Bundle extras = data.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");
            Bitmap resized = resizeBitmap(bmp, 1024);
            imgFoto.setImageBitmap(resized);
            imgFoto.setVisibility(View.VISIBLE);
            fotoBase64 = bitmapToBase64(resized);
        } else if (requestCode == REQ_VOICE) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                edtDesc.setText(results.get(0));
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bmp, int maxSize) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        if (w <= maxSize && h <= maxSize) return bmp;
        float ratio = Math.min((float) maxSize / w, (float) maxSize / h);
        return Bitmap.createScaledBitmap(bmp, Math.round(w * ratio), Math.round(h * ratio), true);
    }

    private String bitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
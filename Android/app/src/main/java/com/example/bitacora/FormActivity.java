package com.example.bitacora;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.repository.RegistroRepository;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class FormActivity extends BaseAuthActivity {
    private static final int REQ_CAMERA = 100;
    private static final int REQ_VOICE = 101;
    private static final String KEY_PHOTO_PATH = "pending_photo_path";

    private EditText edtTitulo, edtDesc;
    private ImageView imgFoto;
    private String fotoBase64 = null;
    private int editId = -1;
    private Uri pendingPhotoUri = null;
    private File pendingPhotoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ensureSession()) return;
        setContentView(R.layout.activity_form);

        if (savedInstanceState != null) {
            String p = savedInstanceState.getString(KEY_PHOTO_PATH);
            if (p != null) pendingPhotoFile = new File(p);
        }

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
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA}, 200);
                return;
            }
            launchCamera();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCamera() {
        try {
            File photoFile = File.createTempFile("bitacora_", ".jpg", getCacheDir());
            pendingPhotoFile = photoFile;
            pendingPhotoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, pendingPhotoUri);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(i, REQ_CAMERA);
            } else {
                pendingPhotoUri = null;
                Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            pendingPhotoUri = null;
            Log.e("FormActivity", "No se pudo preparar la cámara", e);
            Toast.makeText(this, "No se pudo preparar la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingPhotoFile != null) {
            outState.putString(KEY_PHOTO_PATH, pendingPhotoFile.getAbsolutePath());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQ_CAMERA) {
            try {
                Bitmap bmp = null;
                if (pendingPhotoFile != null && pendingPhotoFile.exists() && pendingPhotoFile.length() > 0) {
                    try { bmp = decodeSampledBitmap(pendingPhotoFile, 1024); } catch (Exception ignored) { }
                }
                if (bmp == null && pendingPhotoUri != null) {
                    try { bmp = decodeSampledBitmap(pendingPhotoUri, 1024); } catch (Exception ignored) { }
                }
                if (bmp == null) {
                    Uri dataUri = (data == null) ? null : data.getData();
                    if (dataUri != null) {
                        try { bmp = decodeSampledBitmap(dataUri, 1024); } catch (Exception ignored) { }
                    }
                    if (bmp == null) {
                        Bundle extras = (data == null) ? null : data.getExtras();
                        if (extras != null) bmp = (Bitmap) extras.get("data");
                    }
                }
                if (bmp == null) { Toast.makeText(this, "La cámara no devolvió imagen", Toast.LENGTH_SHORT).show(); return; }
                Bitmap resized = resizeBitmap(bmp, 1024);
                imgFoto.setImageBitmap(resized);
                imgFoto.setVisibility(View.VISIBLE);
                fotoBase64 = bitmapToBase64(resized);
            } catch (Exception e) {
                Log.e("FormActivity", "Error al procesar foto", e);
                Toast.makeText(this, "Error al procesar la foto: " + e.getClass().getSimpleName() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                pendingPhotoUri = null;
                pendingPhotoFile = null;
            }
        } else if (requestCode == REQ_VOICE && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                edtDesc.setText(results.get(0));
            }
        }
    }

    private Bitmap decodeSampledBitmap(Uri uri, int maxSize) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, bounds);
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        int sample = 1;
        while (bounds.outWidth / sample > maxSize * 2 || bounds.outHeight / sample > maxSize * 2) {
            sample *= 2;
        }
        opts.inSampleSize = sample;

        try (InputStream is = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }

    private Bitmap decodeSampledBitmap(File file, int maxSize) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        int sample = 1;
        while (bounds.outWidth / sample > maxSize * 2 || bounds.outHeight / sample > maxSize * 2) {
            sample *= 2;
        }
        opts.inSampleSize = sample;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
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
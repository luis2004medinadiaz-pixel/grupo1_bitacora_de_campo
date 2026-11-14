package com.example.bitacora;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bitacora.data.local.SessionManager;
import com.example.bitacora.data.remote.ApiClient;
import com.example.bitacora.data.remote.ApiErrors;
import com.example.bitacora.data.remote.ComparativaApi;
import com.example.bitacora.data.remote.RegistroDto;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Pantalla de laboratorio: consulta GET /api/registros con Volley, OkHttp y Retrofit
 * y muestra cuántos registros llegaron, cuánto tardó y qué error ocurrió (si ocurrió).
 */
public class ComparativaActivity extends BaseAuthActivity {

    private static final String TAG = "Comparativa";
    // Mismo tiempo de espera para las tres librerías, así la comparación es justa.
    private static final int TIMEOUT_MS = 5000;

    private RadioGroup grupoEscenario;
    private CheckBox chkToken;
    private TextView txtResultado;

    private RequestQueue colaVolley;       // Volley
    private OkHttpClient clienteOkHttp;    // OkHttp (y también lo usa Retrofit)
    private ComparativaApi apiRetrofit;    // Retrofit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ensureSession()) return;
        setContentView(R.layout.activity_comparativa);

        grupoEscenario = findViewById(R.id.grupoEscenario);
        chkToken = findViewById(R.id.chkToken);
        txtResultado = findViewById(R.id.txtResultado);

        colaVolley = Volley.newRequestQueue(this);

        clienteOkHttp = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();

        apiRetrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(clienteOkHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ComparativaApi.class);

        findViewById(R.id.btnVolley).setOnClickListener(v -> consultarVolley());
        findViewById(R.id.btnOkHttp).setOnClickListener(v -> consultarOkHttp());
        findViewById(R.id.btnRetrofit).setOnClickListener(v -> consultarRetrofit());
        findViewById(R.id.btnLimpiar).setOnClickListener(v -> txtResultado.setText("Aún no hay resultados."));
    }

    // ------------------------------------------------------------------ VOLLEY
    // Sus callbacks YA llegan en el hilo principal: se puede tocar la UI directamente.
    private void consultarVolley() {
        String url = ApiClient.BASE_URL + rutaElegida();
        String token = encabezadoToken();
        long inicio = SystemClock.elapsedRealtime();

        JsonArrayRequest peticion = new JsonArrayRequest(Request.Method.GET, url, null,
                respuesta -> mostrarOk("Volley", respuesta.length(), inicio),
                error -> mostrarError("Volley", mensajeVolley(error), error)) {
            // Aquí se agrega el token JWT a la petición.
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> encabezados = new HashMap<>();
                if (token != null) encabezados.put("Authorization", token);
                return encabezados;
            }
        };

        // Tiempo de espera 5 s, 1 reintento, multiplicador 1 (el reintento espera lo mismo).
        peticion.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, 1, 1f));
        colaVolley.add(peticion);
    }

    private String mensajeVolley(VolleyError error) {
        if (error.networkResponse != null) {            // el servidor sí respondió (401, 500...)
            return ApiErrors.message(error.networkResponse.statusCode);
        }
        if (error instanceof TimeoutError) return "Tiempo de espera agotado.";
        if (error instanceof NoConnectionError) return "Sin conexión con el servidor.";
        return "Error inesperado.";
    }

    // ------------------------------------------------------------------ OKHTTP
    // Sus callbacks llegan en un hilo secundario: hay que usar runOnUiThread para tocar la UI.
    private void consultarOkHttp() {
        String url = ApiClient.BASE_URL + rutaElegida();
        String token = encabezadoToken();
        long inicio = SystemClock.elapsedRealtime();

        okhttp3.Request.Builder constructor = new okhttp3.Request.Builder().url(url);
        if (token != null) constructor.header("Authorization", token);
        okhttp3.Request peticion = constructor.build();

        clienteOkHttp.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // No hubo respuesta: sin red, servidor apagado o tiempo agotado.
                runOnUiThread(() -> mostrarError("OkHttp", mensajeDeFallo(e), e));
            }

            @Override
            public void onResponse(Call call, Response respuesta) {
                try (respuesta) {
                    if (!respuesta.isSuccessful()) {    // 401, 500...
                        String mensaje = ApiErrors.message(respuesta.code());
                        runOnUiThread(() -> mostrarError("OkHttp", mensaje, null));
                        return;
                    }
                    // Gson convierte el texto JSON en un arreglo de objetos.
                    RegistroDto[] lista = new Gson().fromJson(respuesta.body().string(), RegistroDto[].class);
                    runOnUiThread(() -> mostrarOk("OkHttp", lista.length, inicio));
                } catch (Exception e) {
                    runOnUiThread(() -> mostrarError("OkHttp", mensajeDeFallo(e), e));
                }
            }
        });
    }

    // ---------------------------------------------------------------- RETROFIT
    // Sus callbacks llegan en el hilo principal (Retrofit lo hace por nosotros en Android).
    private void consultarRetrofit() {
        long inicio = SystemClock.elapsedRealtime();

        apiRetrofit.getRegistros(rutaElegida(), encabezadoToken())
                .enqueue(new retrofit2.Callback<List<RegistroDto>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<RegistroDto>> call,
                                           retrofit2.Response<List<RegistroDto>> respuesta) {
                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            mostrarOk("Retrofit", respuesta.body().size(), inicio);
                        } else {
                            mostrarError("Retrofit", ApiErrors.message(respuesta.code()), null);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<RegistroDto>> call, Throwable t) {
                        mostrarError("Retrofit", mensajeDeFallo(t), t);
                    }
                });
    }

    // ------------------------------------------------------------------ AYUDAS
    private String rutaElegida() {
        int id = grupoEscenario.getCheckedRadioButtonId();
        if (id == R.id.radioLento) return "api/pruebas/lento";
        if (id == R.id.radioError500) return "api/pruebas/error";
        return "api/registros";
    }

    /** Sin token (casilla desmarcada) la API responde 401. */
    private String encabezadoToken() {
        if (!chkToken.isChecked()) return null;
        return "Bearer " + SessionManager.getInstance().getToken();
    }

    /** Para OkHttp y Retrofit: el servidor nunca respondió. */
    private String mensajeDeFallo(Throwable t) {
        if (t instanceof SocketTimeoutException) return "Tiempo de espera agotado.";
        if (t instanceof IOException) return "Sin conexión con el servidor.";
        return "Respuesta inesperada del servidor.";
    }

    private void mostrarOk(String libreria, int cantidad, long inicio) {
        long ms = SystemClock.elapsedRealtime() - inicio;
        Log.d(TAG, libreria + " OK: " + cantidad + " registros en " + ms + " ms");
        agregarLinea(libreria + ": " + cantidad + " registros en " + ms + " ms");
    }

    private void mostrarError(String libreria, String mensaje, Throwable causa) {
        Log.e(TAG, libreria + " ERROR: " + mensaje + (causa != null ? " | " + causa : ""));
        agregarLinea(libreria + " ERROR: " + mensaje);
    }

    /** Agrega el resultado arriba de la lista, para ver varias ejecuciones seguidas. */
    private void agregarLinea(String linea) {
        String actual = txtResultado.getText().toString();
        if (actual.startsWith("Aún no")) actual = "";
        txtResultado.setText(linea + "\n" + actual);
    }
}

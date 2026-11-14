package com.example.bitacora;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bitacora.data.local.SessionManager;
import com.example.bitacora.data.remote.ApiClient;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.repository.RegistroRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Laboratorio: la misma consulta (GET) hecha con Volley, OkHttp y Retrofit.
 * Mide el tiempo, muestra cuántos registros llegaron y qué error recibe el código y el usuario.
 * Se filtra por TAG "COMPARATIVA" en Logcat para las capturas.
 */
public class ComparativaActivity extends BaseAuthActivity {
    private static final String TAG = "COMPARATIVA";
    // Menor que los 20 s del endpoint lento: la petición debe agotarse y fallar, nunca llegar a OK.
    private static final int TIMEOUT_MS = 15000;

    private static final String[] ESCENARIOS = {
            "1. Normal: GET /api/registros",
            "2. Tiempo agotado: /api/prueba/lento (20 s)",
            "3. Error 500: /api/prueba/error500",
            "4. 401 forzado: /api/prueba/no-autorizado"
    };
    private static final String[] RUTAS = {
            "api/registros",
            "api/prueba/lento?segundos=20",
            "api/prueba/error500",
            "api/prueba/no-autorizado"
    };

    private Spinner spnEscenario;
    private CheckBox chkToken;
    private TextView txtResultado, txtPromedios;

    private final Map<String, List<Long>> tiempos = new LinkedHashMap<>();
    private final SimpleDateFormat horaFmt = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private final Gson gson = new Gson();

    private RequestQueue volleyQueue;
    private OkHttpClient okClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ensureSession()) return;
        setContentView(R.layout.activity_comparativa);

        spnEscenario = findViewById(R.id.spnEscenario);
        chkToken = findViewById(R.id.chkToken);
        txtResultado = findViewById(R.id.txtResultado);
        txtPromedios = findViewById(R.id.txtPromedios);

        spnEscenario.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ESCENARIOS));

        tiempos.put("Volley", new ArrayList<>());
        tiempos.put("OkHttp", new ArrayList<>());
        tiempos.put("Retrofit", new ArrayList<>());
        actualizarPromedios();

        // Volley: cola de peticiones propia.
        volleyQueue = Volley.newRequestQueue(getApplicationContext());

        // OkHttp "directo": cliente propio, SIN interceptores (el token se agrega a mano en cada Request).
        okClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS) // mismo límite total (15 s) que Volley y Retrofit
                .build();

        findViewById(R.id.btnVolley).setOnClickListener(v -> pruebaVolley());
        findViewById(R.id.btnOkHttp).setOnClickListener(v -> pruebaOkHttp());
        findViewById(R.id.btnRetrofit).setOnClickListener(v -> pruebaRetrofit());
        findViewById(R.id.btnReiniciar).setOnClickListener(v -> {
            for (List<Long> l : tiempos.values()) l.clear();
            txtResultado.setText("");
            actualizarPromedios();
        });
    }

    // ------------------------------------------------------------------ helpers

    private String escenario() { return ESCENARIOS[spnEscenario.getSelectedItemPosition()]; }

    private String ruta() { return RUTAS[spnEscenario.getSelectedItemPosition()]; }

    private String url() { return ApiClient.BASE_URL + ruta(); }

    /** null = no enviar token (simula "token ausente"). Solo aplica a Volley y OkHttp, que arman el encabezado a mano. */
    private String tokenParaEnviar() {
        return chkToken.isChecked() ? SessionManager.getInstance().getToken() : null;
    }

    private static long msDesde(long t0) { return (System.nanoTime() - t0) / 1_000_000L; }

    private static String hiloActual() {
        return Looper.myLooper() == Looper.getMainLooper()
                ? "principal (main)"
                : "secundario (" + Thread.currentThread().getName() + ")";
    }

    // ------------------------------------------------------------------ 1) VOLLEY

    private void pruebaVolley() {
        final long t0 = System.nanoTime();
        final String token = tokenParaEnviar();
        log("Volley", "Consultando " + ruta() + " ...");

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url(), null,
                // Éxito: Volley entrega la respuesta en el HILO PRINCIPAL.
                response -> exito("Volley", response.length(), msDesde(t0), hiloActual()),
                // Error: también en el hilo principal.
                error -> fallo("Volley", describirVolley(error), msDesde(t0), hiloActual())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> h = new HashMap<>();
                h.put("Accept", "application/json");
                if (token != null) h.put("Authorization", "Bearer " + token);
                return h;
            }
        };
        // 15 s por intento + 1 reintento = ~30 s hasta el fallo; el endpoint lento tarda 20 s,
        // así que debe terminar en TimeoutError (nunca en OK).
        req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 0f));
        req.setShouldCache(false);
        volleyQueue.add(req);
    }

    private String[] describirVolley(VolleyError e) {
        int code = e.networkResponse != null ? e.networkResponse.statusCode : -1;
        if (e instanceof TimeoutError) {
            return new String[]{"TimeoutError", "El servidor tardó demasiado. Intenta de nuevo."};
        }
        if (e instanceof NoConnectionError) {
            return new String[]{"NoConnectionError", "Sin conexión con el servidor. Revisa tu internet."};
        }
        if (e instanceof AuthFailureError) {
            return new String[]{"AuthFailureError (HTTP " + code + ")",
                    code == 403 ? "No tienes permiso para realizar esta acción."
                            : "Tu sesión expiró o el token no es válido."};
        }
        if (e instanceof ServerError) {
            return new String[]{"ServerError (HTTP " + code + ")", "El servidor tuvo un problema. Intenta más tarde."};
        }
        if (e instanceof ParseError) {
            return new String[]{"ParseError", "La respuesta del servidor no tiene el formato esperado."};
        }
        if (e instanceof NetworkError) {
            return new String[]{"NetworkError", "Error de red. Intenta de nuevo."};
        }
        return new String[]{e.getClass().getSimpleName(), "Ocurrió un error inesperado."};
    }

    // ------------------------------------------------------------------ 2) OKHTTP

    private void pruebaOkHttp() {
        final long t0 = System.nanoTime();
        final String token = tokenParaEnviar();
        log("OkHttp", "Consultando " + ruta() + " ...");

        okhttp3.Request.Builder rb = new okhttp3.Request.Builder()
                .url(url())
                .get()
                .header("Accept", "application/json");
        if (token != null) rb.header("Authorization", "Bearer " + token);

        // enqueue(): asíncrono. Las respuestas llegan en un HILO SECUNDARIO: hay que usar runOnUiThread.
        okClient.newCall(rb.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final long ms = msDesde(t0);
                final String hilo = hiloActual();
                final String[] desc = describirOkHttp(e);
                runOnUiThread(() -> fallo("OkHttp", desc, ms, hilo));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String hilo = hiloActual();
                final int code = response.code();
                try (ResponseBody body = response.body()) {
                    String json = body != null ? body.string() : "";
                    if (!response.isSuccessful()) {
                        final long ms = msDesde(t0);
                        final String[] desc = describirHttp(code);
                        runOnUiThread(() -> fallo("OkHttp", desc, ms, hilo));
                        return;
                    }
                    // Conversión manual del JSON a objetos con Gson.
                    Type tipoLista = new TypeToken<List<RegistroDto>>() {}.getType();
                    List<RegistroDto> lista = gson.fromJson(json, tipoLista);
                    final int n = lista != null ? lista.size() : 0;
                    final long ms = msDesde(t0);
                    runOnUiThread(() -> exito("OkHttp", n, ms, hilo));
                } catch (JsonSyntaxException ex) {
                    final long ms = msDesde(t0);
                    runOnUiThread(() -> fallo("OkHttp",
                            new String[]{"JsonSyntaxException", "La respuesta del servidor no tiene el formato esperado."},
                            ms, hilo));
                }
            }
        });
    }

    private String[] describirOkHttp(IOException e) {
        if (e instanceof InterruptedIOException) {
            // cubre SocketTimeoutException y el timeout de callTimeout
            return new String[]{e.getClass().getSimpleName(), "El servidor tardó demasiado. Intenta de nuevo."};
        }
        if (e instanceof UnknownHostException || e instanceof ConnectException || e instanceof SocketException) {
            return new String[]{e.getClass().getSimpleName(), "Sin conexión con el servidor. Revisa tu internet."};
        }
        return new String[]{e.getClass().getSimpleName(), "Ocurrió un error inesperado de red."};
    }

    private String[] describirHttp(int code) {
        if (code == 401) return new String[]{"HTTP 401", "Tu sesión expiró o el token no es válido."};
        if (code == 403) return new String[]{"HTTP 403", "No tienes permiso para realizar esta acción."};
        if (code >= 500) return new String[]{"HTTP " + code, "El servidor tuvo un problema. Intenta más tarde."};
        return new String[]{"HTTP " + code, "No se pudo completar la consulta."};
    }

    // ------------------------------------------------------------------ 3) RETROFIT

    private void pruebaRetrofit() {
        final long t0 = System.nanoTime();
        log("Retrofit", "Consultando " + ruta() + " ...");
        if (!chkToken.isChecked()) {
            log("Retrofit", "Nota: el interceptor de la app agrega el token siempre; el check solo aplica a Volley/OkHttp.");
        }

        // Se reutiliza el repositorio de la tarea 2.3 (ApiClient + AuthInterceptor + Gson).
        // El callback de Retrofit llega en el HILO PRINCIPAL.
        new RegistroRepository().getAllFrom(ruta(), (data, error) -> {
            long ms = msDesde(t0);
            if (error != null) {
                fallo("Retrofit", new String[]{"mensaje del repositorio", error}, ms, hiloActual());
            } else {
                exito("Retrofit", data.size(), ms, hiloActual());
            }
        });
        // Con un 401 real, el AuthInterceptor borra la sesión y la app regresa al login (comportamiento de la tarea 2.4).
    }

    // ------------------------------------------------------------------ resultados

    private void exito(String lib, int cantidad, long ms, String hilo) {
        tiempos.get(lib).add(ms);
        String linea = "OK | " + cantidad + " registros | " + ms + " ms | hilo: " + hilo;
        log(lib, linea);
        Log.i(TAG, "[" + lib + "] escenario=" + escenario() + " resultado=" + linea);
        actualizarPromedios();
    }

    /** desc[0] = error que recibe el CÓDIGO; desc[1] = mensaje que ve el USUARIO. */
    private void fallo(String lib, String[] desc, long ms, String hilo) {
        String linea = "FALLO | código recibe: " + desc[0] + " | usuario ve: \"" + desc[1] + "\" | " + ms + " ms | hilo: " + hilo;
        log(lib, linea);
        Log.w(TAG, "[" + lib + "] escenario=" + escenario() + " resultado=" + linea);
        Toast.makeText(this, lib + ": " + desc[1], Toast.LENGTH_LONG).show();
    }

    private void log(String lib, String mensaje) {
        txtResultado.append("[" + horaFmt.format(new Date()) + "] " + lib + " → " + mensaje + "\n");
    }

    private void actualizarPromedios() {
        StringBuilder sb = new StringBuilder("Promedio de respuestas exitosas:\n");
        for (Map.Entry<String, List<Long>> e : tiempos.entrySet()) {
            List<Long> l = e.getValue();
            long suma = 0;
            for (long v : l) suma += v;
            sb.append(e.getKey()).append(": ");
            if (l.isEmpty()) sb.append("sin datos");
            else sb.append(suma / l.size()).append(" ms (").append(l.size()).append(" ejecuciones: ").append(l).append(")");
            sb.append("\n");
        }
        txtPromedios.setText(sb.toString().trim());
    }
}

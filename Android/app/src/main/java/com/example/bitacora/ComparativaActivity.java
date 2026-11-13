package com.example.bitacora;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bitacora.data.remote.ApiClient;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.data.local.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

public class ComparativaActivity extends AppCompatActivity {
    private static final String BASE_URL = ApiClient.BASE_URL.endsWith("/")
            ? ApiClient.BASE_URL.substring(0, ApiClient.BASE_URL.length() - 1)
            : ApiClient.BASE_URL;

    private static final String PATH = "/api/registros";

    private String token = "PEGA_AQUI_TU_JWT";

    private static final String TAG = "Comparativa";

    private TextView tvResultado;
    private OkHttpClient okClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        tvResultado = findViewById(R.id.tvResultado);

        try {
            String t = SessionManager.getInstance().getToken();
            if (t != null && !t.isEmpty()) token = t;
        } catch (Exception ignored) { }

        okClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        findViewById(R.id.btnVolley).setOnClickListener(v -> consultarVolley());
        findViewById(R.id.btnOkHttp).setOnClickListener(v -> consultarOkHttp());
        findViewById(R.id.btnRetrofit).setOnClickListener(v -> consultarRetrofit());
    }

    private void consultarVolley() {
        tvResultado.setText("Volley: consultando...");
        final long inicio = android.os.SystemClock.elapsedRealtime();

        JsonArrayRequest request = new JsonArrayRequest(
                com.android.volley.Request.Method.GET,
                BASE_URL + PATH,
                null,
                respuesta -> {
                    long ms = android.os.SystemClock.elapsedRealtime() - inicio;
                    tvResultado.setText("Volley: " + respuesta.length() + " registros, " + ms + " ms");
                },
                error -> {
                    Log.e(TAG, "Volley: " + error.getClass().getSimpleName()
                            + " code=" + (error.networkResponse != null ? error.networkResponse.statusCode : "null"));
                    tvResultado.setText("Volley: " + mensajeVolley(error));
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> h = new java.util.HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 1f));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private String mensajeVolley(VolleyError e) {
        if (e instanceof com.android.volley.NoConnectionError) return "Sin conexión con el servidor";
        if (e instanceof com.android.volley.TimeoutError) return "Tiempo de espera agotado";
        if (e instanceof com.android.volley.AuthFailureError) return "No autorizado (401): sesión vencida";
        if (e instanceof com.android.volley.ServerError)
            return "Error del servidor (" + (e.networkResponse != null ? e.networkResponse.statusCode : "") + ")";
        if (e instanceof com.android.volley.ParseError) return "Respuesta con formato inesperado";
        return "Error: " + e.getMessage();
    }

    private void consultarOkHttp() {
        tvResultado.setText("OkHttp: consultando...");
        final long inicio = android.os.SystemClock.elapsedRealtime();

        Request request = new Request.Builder()
                .url(BASE_URL + PATH)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "OkHttp falla: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                mostrar("OkHttp: " + mensajeIO(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response r = response) {
                    long ms = android.os.SystemClock.elapsedRealtime() - inicio;
                    if (!r.isSuccessful()) {
                        Log.e(TAG, "OkHttp HTTP " + r.code());
                        mostrar("OkHttp: " + mensajeHttp(r.code()));
                        return;
                    }
                    try {
                        Type tipo = new TypeToken<List<RegistroDto>>() {}.getType();
                        List<RegistroDto> lista = new Gson().fromJson(r.body().string(), tipo);
                        mostrar("OkHttp: " + (lista != null ? lista.size() : 0) + " registros, " + ms + " ms");
                    } catch (Exception e) {
                        Log.e(TAG, "OkHttp JSON invalido", e);
                        mostrar("OkHttp: respuesta con formato inesperado");
                    }
                }
            }
        });
    }

    private void mostrar(String texto) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) tvResultado.setText(texto);
        });
    }

    private String mensajeIO(IOException e) {
        if (e instanceof java.net.SocketTimeoutException) return "Tiempo de espera agotado";
        if (e instanceof java.net.UnknownHostException) return "Sin conexión a internet";
        if (e instanceof java.net.ConnectException) return "No se pudo conectar con el servidor";
        return "Error de red: " + e.getMessage();
    }

    private String mensajeHttp(int code) {
        if (code == 401) return "No autorizado (401): sesión vencida";
        if (code == 500) return "Error interno del servidor (500)";
        return "Error HTTP " + code;
    }

    private void consultarRetrofit() {
        tvResultado.setText("Retrofit: consultando...");
        final long inicio = android.os.SystemClock.elapsedRealtime();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + "/")
                .client(new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .addInterceptor(chain -> {
                            okhttp3.Request req = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(req);
                        })
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        com.example.bitacora.data.remote.RegistroApi api =
                retrofit.create(com.example.bitacora.data.remote.RegistroApi.class);

        api.getAll().enqueue(new retrofit2.Callback<List<RegistroDto>>() {
            @Override
            public void onResponse(retrofit2.Call<List<RegistroDto>> call, retrofit2.Response<List<RegistroDto>> response) {
                long ms = android.os.SystemClock.elapsedRealtime() - inicio;
                if (!response.isSuccessful()) {
                    tvResultado.setText("Retrofit: " + mensajeHttp(response.code()));
                    return;
                }
                List<RegistroDto> lista = response.body();
                tvResultado.setText("Retrofit: " + (lista != null ? lista.size() : 0) + " registros, " + ms + " ms");
            }

            @Override
            public void onFailure(retrofit2.Call<List<RegistroDto>> call, Throwable t) {
                Log.e(TAG, "Retrofit falla: " + t.getClass().getSimpleName());
                if (t instanceof IOException) {
                    tvResultado.setText("Retrofit: " + mensajeIO((IOException) t));
                } else {
                    tvResultado.setText("Retrofit: error inesperado (" + t.getMessage() + ")");
                }
            }
        });
    }
}

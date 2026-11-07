package com.example.bitacora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitacora.data.remote.RegistroDto;
import com.example.bitacora.ui.RegistroAdapter;
import com.example.bitacora.ui.UiState;
import com.example.bitacora.viewmodel.RegistroViewModel;

public class MainActivity extends AppCompatActivity {
    private RegistroViewModel vm;
    private RegistroAdapter adapter;
    private ProgressBar progress;
    private TextView txtEmpty, txtError;
    private View btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtError = findViewById(R.id.txtError);
        btnRetry = findViewById(R.id.btnRetry);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistroAdapter(r -> {
            Intent i = new Intent(this, DetailActivity.class);
            i.putExtra("id", r.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, FormActivity.class)));

        vm = new ViewModelProvider(this).get(RegistroViewModel.class);
        vm.getState().observe(this, this::render);
        vm.getActionMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        btnRetry.setOnClickListener(v -> vm.load());
        vm.load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.load();
    }

    private void render(UiState<java.util.List<RegistroDto>> s) {
        progress.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        switch (s.status) {
            case LOADING:
                progress.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                adapter.setItems(s.data);
                break;
            case EMPTY:
                txtEmpty.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                txtError.setText(s.message);
                txtError.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
                break;
        }
    }
}
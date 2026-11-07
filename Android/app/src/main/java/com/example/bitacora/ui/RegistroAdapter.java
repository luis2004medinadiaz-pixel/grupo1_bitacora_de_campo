package com.example.bitacora.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitacora.R;
import com.example.bitacora.data.remote.RegistroDto;
import java.util.ArrayList;
import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.VH> {
    public interface OnClick { void onClick(RegistroDto r); }
    private final List<RegistroDto> items = new ArrayList<>();
    private final OnClick onClick;

    public RegistroAdapter(OnClick onClick) { this.onClick = onClick; }

    public void setItems(List<RegistroDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RegistroDto r = items.get(position);
        holder.titulo.setText(r.titulo);
        holder.desc.setText(r.descripcion);
        holder.itemView.setOnClickListener(v -> onClick.onClick(r));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView titulo, desc;
        VH(View v) {
            super(v);
            titulo = v.findViewById(R.id.txtTitulo);
            desc = v.findViewById(R.id.txtDesc);
        }
    }
}
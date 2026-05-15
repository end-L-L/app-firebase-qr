package com.curso.asistenciaqr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.models.Asistencia;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AsistenciaAdapter extends RecyclerView.Adapter<AsistenciaAdapter.ViewHolder> {

    private List<Asistencia> lista;

    public AsistenciaAdapter(List<Asistencia> lista) {
        this.lista = lista;
    }

    public void actualizar(List<Asistencia> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asistencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia a = lista.get(position);
        holder.tvNombre.setText(a.getAlumnoNombre());
        holder.tvEmail.setText(a.getAlumnoEmail());

        // Formatear timestamp a hora legible
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(a.getTimestamp() * 1000));
        holder.tvHora.setText(hora);
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvHora;
        ViewHolder(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tv_nombre_alumno);
            tvEmail  = v.findViewById(R.id.tv_email_alumno);
            tvHora   = v.findViewById(R.id.tv_hora);
        }
    }
}
package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.senai.get_in.R;
import com.senai.get_in.model.VisitanteLocal;
import java.util.List;

public class VisitanteAdapter extends RecyclerView.Adapter<VisitanteAdapter.ViewHolder> {

    private List<VisitanteLocal> visitantes;

    public VisitanteAdapter(List<VisitanteLocal> visitantes) {
        this.visitantes = visitantes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitanteLocal v = visitantes.get(position);
        holder.tvNome.setText(v.getNome());
        holder.tvEmpresa.setText(v.getEmpresa());
        holder.tvSetor.setText(v.getSetor());
        holder.tvStatus.setText(v.getStatus());
        holder.tvHora.setText(v.getEntrada() != null ? v.getEntrada().substring(Math.max(0, v.getEntrada().length() - 8)) : "--:--");
        
        if ("Dentro".equalsIgnoreCase(v.getStatus())) {
            holder.tvStatus.setTextColor(0xFF4CAF50); // Verde
        } else {
            holder.tvStatus.setTextColor(0xFF757575); // Cinza
        }
    }

    @Override
    public int getItemCount() {
        return visitantes.size();
    }

    public void updateList(List<VisitanteLocal> newList) {
        this.visitantes = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvEmpresa, tvSetor, tvStatus, tvHora;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvSetor = itemView.findViewById(R.id.tvSetor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHora = itemView.findViewById(R.id.tvHora);
        }
    }
}

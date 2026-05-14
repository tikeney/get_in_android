package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.R;
import com.senai.get_in.model.Historico;

import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

    private List<Historico> listaHistorico;

    public HistoricoAdapter(List<Historico> listaHistorico) {
        this.listaHistorico = listaHistorico;
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        Historico historico = listaHistorico.get(position);
        holder.tvNome.setText(historico.getNome());
        holder.tvEmpresa.setText(historico.getEmpresa());
        holder.tvSetor.setText(historico.getSetor());
        holder.tvStatus.setText(historico.getStatus());
        holder.tvHora.setText(historico.getHora());
        holder.tvDuracao.setText(historico.getDuracao());
        holder.ivFoto.setImageResource(historico.getFotoResId());

        // Ajuste de cor do status baseado no texto
        int color;
        String status = historico.getStatus().toLowerCase();
        if (status.contains("permitido") || status.contains("fábrica")) {
            color = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
        } else if (status.contains("negado")) {
            color = holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
        } else {
            color = holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
        }
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return listaHistorico.size();
    }

    public void updateList(List<Historico> newList) {
        this.listaHistorico = newList;
        notifyDataSetChanged();
    }

    static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNome, tvEmpresa, tvSetor, tvStatus, tvHora, tvDuracao;

        public HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvSetor = itemView.findViewById(R.id.tvSetor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvDuracao = itemView.findViewById(R.id.tvDuracao);
        }
    }
}

package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.senai.get_in.R;
import com.senai.get_in.model.Notificacao;
import java.util.List;

public class NotificacoesListAdapter extends RecyclerView.Adapter<NotificacoesListAdapter.NotificacaoViewHolder> {

    private List<Notificacao> listaNotificacoes;

    public NotificacoesListAdapter(List<Notificacao> listaNotificacoes) {
        this.listaNotificacoes = listaNotificacoes;
    }

    @NonNull
    @Override
    public NotificacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacoes, parent, false);
        return new NotificacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoViewHolder holder, int position) {
        Notificacao notificacao = listaNotificacoes.get(position);
        holder.tvTitulo.setText(notificacao.getTitulo());
        holder.tvDescricao.setText(notificacao.getDescricao());
        holder.tvHora.setText(notificacao.getHora());
    }

    @Override
    public int getItemCount() {
        return listaNotificacoes != null ? listaNotificacoes.size() : 0;
    }

    public void updateList(List<Notificacao> newList) {
        this.listaNotificacoes = newList;
        notifyDataSetChanged();
    }

    public static class NotificacaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao, tvHora;

        public NotificacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescricao = itemView.findViewById(R.id.tvDesccricao);
            tvHora = itemView.findViewById(R.id.tvHora);
        }
    }
}

package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.R;
import com.senai.get_in.model.Requisicao;

import java.util.List;

public class RequisicaoAdapter extends RecyclerView.Adapter<RequisicaoAdapter.ViewHolder> {

    private List<Requisicao> requisicoes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAprovarClick(Requisicao requisicao);
        void onNegarClick(Requisicao requisicao);
    }

    public RequisicaoAdapter(List<Requisicao> requisicoes, OnItemClickListener listener) {
        this.requisicoes = requisicoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requisicao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Requisicao req = requisicoes.get(position);
        holder.txtNome.setText(req.getNomeUsuario() != null ? req.getNomeUsuario() : "Desconhecido");
        holder.txtDocumento.setText(req.getCpfUsuario() != null ? req.getCpfUsuario() : "Sem CPF");
        holder.txtHorario.setText(req.getDataRequisicao() != null ? req.getDataRequisicao() : "--:--");
        holder.txtLocal.setText(req.getNomeDepartamento() != null ? req.getNomeDepartamento() : "N/A");
        holder.txtEmpresa.setText(req.getEmpresa() != null ? req.getEmpresa() : "Não informada");
        holder.txtMotivo.setText(req.getMotivo() != null ? req.getMotivo() : "Não informado");
        holder.txtSetorParaAcesso.setText(req.getNomeDepartamento() != null ? req.getNomeDepartamento() : "N/A");
        holder.txtDescricao.setText(req.getDescricao() != null ? req.getDescricao() : "Sem descrição.");

        holder.btnAceitar.setOnClickListener(v -> listener.onAprovarClick(req));
        holder.btnNegar.setOnClickListener(v -> listener.onNegarClick(req));
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public void updateList(List<Requisicao> newList) {
        this.requisicoes = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtDocumento, txtHorario, txtLocal, txtEmpresa, txtMotivo, txtSetorParaAcesso, txtDescricao;
        View btnAceitar, btnNegar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDocumento = itemView.findViewById(R.id.txtDocumento);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtLocal = itemView.findViewById(R.id.txtLocal);
            txtEmpresa = itemView.findViewById(R.id.txtEmpresa);
            txtMotivo = itemView.findViewById(R.id.txtMotivo);
            txtSetorParaAcesso = itemView.findViewById(R.id.txtSetorParaAcesso);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnNegar = itemView.findViewById(R.id.btnNegar);
        }
    }
}

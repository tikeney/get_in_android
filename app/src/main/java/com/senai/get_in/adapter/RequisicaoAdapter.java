package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.senai.get_in.R;
import com.senai.get_in.model.Requisicao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        
        // Informações básicas
        holder.txtNome.setText(req.getUsuarioNome() != null ? req.getUsuarioNome() : "Desconhecido");
        holder.txtDocumento.setText(req.getUsuarioCpf() != null ? req.getUsuarioCpf() : "---.---.------");
        
        // Separação de Data e Hora
        formatarDataHora(holder, req.getDataRequisicao());
        
        // Chips (Pills)
        holder.chipMotivo.setText(req.getTipoRequisicao() != null ? req.getTipoRequisicao() : "Requisição");

        if (req.getEmpresaVisitante() != null && !req.getEmpresaVisitante().isEmpty()) {
            holder.chipEmpresa.setText(req.getEmpresaVisitante());
            holder.chipEmpresa.setVisibility(View.VISIBLE);
        } else {
            holder.chipEmpresa.setVisibility(View.GONE);
        }

        holder.chipSetor.setText(req.getDepartamentoNome() != null ? req.getDepartamentoNome() : "Geral");

        // Descrição Responsiva
        String descricao = "";
        if (req.getValidadeVisita() != null && !req.getValidadeVisita().isEmpty()) {
            descricao = "Válido até: " + req.getValidadeVisita();
        } else if (req.getDescricao() != null && !req.getDescricao().isEmpty()) {
            descricao = req.getDescricao();
        }

        if (descricao.isEmpty()) {
            holder.txtDescricao.setVisibility(View.GONE);
            holder.bgDescricao.setVisibility(View.GONE);
        } else {
            holder.txtDescricao.setText(descricao);
            holder.txtDescricao.setVisibility(View.VISIBLE);
            holder.bgDescricao.setVisibility(View.VISIBLE);
        }

        holder.btnAceitar.setOnClickListener(v -> listener.onAprovarClick(req));
        holder.btnNegar.setOnClickListener(v -> listener.onNegarClick(req));
    }

    private void formatarDataHora(ViewHolder holder, String dataRaw) {
        if (dataRaw == null || dataRaw.isEmpty()) {
            holder.txtDia.setText("--/--");
            holder.txtHorario.setText("--:--");
            return;
        }

        try {
            // Tenta detectar o formato (ISO ou simples)
            SimpleDateFormat inputFormat;
            if (dataRaw.contains("T")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            }

            Date date = inputFormat.parse(dataRaw);
            if (date != null) {
                SimpleDateFormat sdfDia = new SimpleDateFormat("dd/MM", Locale.getDefault());
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                
                holder.txtDia.setText(sdfDia.format(date));
                holder.txtHorario.setText(sdfHora.format(date));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback: Split básico se falhar o parse formal
        try {
            String[] partes = dataRaw.split("[ T]"); // Split por espaço ou 'T'
            if (partes.length >= 2) {
                holder.txtDia.setText(partes[0]);
                holder.txtHorario.setText(partes[1].substring(0, Math.min(partes[1].length(), 5)));
            } else {
                holder.txtDia.setText(dataRaw);
                holder.txtHorario.setText("--:--");
            }
        } catch (Exception e) {
            holder.txtDia.setText(dataRaw);
            holder.txtHorario.setText("--:--");
        }
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
        TextView txtNome, txtDocumento, txtDia, txtHorario, txtDescricao;
        Chip chipEmpresa, chipMotivo, chipSetor;
        View btnAceitar, btnNegar, bgDescricao;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDocumento = itemView.findViewById(R.id.txtDocumento);
            txtDia = itemView.findViewById(R.id.txtDia);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            bgDescricao = itemView.findViewById(R.id.bgDescricao);
            
            chipEmpresa = itemView.findViewById(R.id.chipEmpresa);
            chipMotivo = itemView.findViewById(R.id.chipMotivo);
            chipSetor = itemView.findViewById(R.id.chipSetor);
            
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnNegar = itemView.findViewById(R.id.btnNegar);
        }
    }
}

package com.senai.get_in.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.senai.get_in.R;
import com.senai.get_in.model.LogAcesso;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    private List<LogAcesso> logs;

    public LogAdapter(List<LogAcesso> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogAcesso log = logs.get(position);
        holder.tvNome.setText(log.getUsuarioNome());
        holder.tvSetor.setText(log.getDepartamentoUsuario());
        holder.tvEmpresa.setText(log.getLocalDispositivo());
        
        String entrada = formatarData(log.getDataEntrada());
        holder.tvHora.setText(entrada != null ? entrada : "--:--");
        
        if (log.getDataSaida() != null) {
            holder.tvStatus.setText("Saída registrada");
            holder.tvStatus.setTextColor(0xFF4CAF50); // Verde
        } else {
            holder.tvStatus.setText("No local");
            holder.tvStatus.setTextColor(0xFF2196F3); // Azul
        }
    }

    private String formatarData(String dataRaw) {
        if (dataRaw == null) return null;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dataRaw);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dataRaw;
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void updateList(List<LogAcesso> newList) {
        this.logs = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvSetor, tvEmpresa, tvStatus, tvHora;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvSetor = itemView.findViewById(R.id.tvSetor);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHora = itemView.findViewById(R.id.tvHora);
        }
    }
}

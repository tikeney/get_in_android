package com.senai.get_in.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.senai.get_in.R;
import com.senai.get_in.databinding.ItemEquipeBinding;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.UsuarioDetalhado;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EquipeAdapter extends RecyclerView.Adapter<EquipeAdapter.EquipeViewHolder> {

    private List<UsuarioDetalhado> equipe;
    private List<LogAcesso> logsHoje = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UsuarioDetalhado usuario);
    }

    public EquipeAdapter(List<UsuarioDetalhado> equipe, OnItemClickListener listener) {
        this.equipe = equipe;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEquipeBinding binding = ItemEquipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EquipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipeViewHolder holder, int position) {
        UsuarioDetalhado usuario = equipe.get(position);
        
        LogAcesso ultimoLog = null;
        if (logsHoje != null) {
            for (LogAcesso log : logsHoje) {
                if (log.getUsuarioNome() != null && log.getUsuarioNome().equalsIgnoreCase(usuario.getNome())) {
                    ultimoLog = log;
                    break;
                }
            }
        }
        
        holder.bind(usuario, ultimoLog, listener);
    }

    @Override
    public int getItemCount() {
        return equipe.size();
    }

    public void updateList(List<UsuarioDetalhado> newList, List<LogAcesso> currentLogs) {
        this.equipe = newList;
        this.logsHoje = currentLogs;
        notifyDataSetChanged();
    }

    static class EquipeViewHolder extends RecyclerView.ViewHolder {
        private final ItemEquipeBinding binding;

        public EquipeViewHolder(ItemEquipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UsuarioDetalhado usuario, LogAcesso log, OnItemClickListener listener) {
            binding.tvNomeEquipe.setText(usuario.getNome());
            binding.tvCargoEquipe.setText(usuario.getCargo() + " — " + 
                (usuario.getDepartamentoNome() != null ? usuario.getDepartamentoNome() : "Geral"));

            boolean estaPresente = false;
            String local = "Ausente";
            String tempo = "";
            int corStatus = Color.parseColor("#F44336"); // Vermelho (Ausente)
            String textoStatus = "AUSENTE";

            if (log != null) {
                boolean temSaida = log.getDataSaida() != null && !log.getDataSaida().isEmpty();
                estaPresente = !temSaida;
                
                if (estaPresente) {
                    local = log.getLocalDispositivo() != null ? log.getLocalDispositivo() : "Área Interna";
                    String localLower = local.toLowerCase();
                    
                    if (localLower.contains("refeitorio") || localLower.contains("ambulatorio") || 
                        localLower.contains("descanso") || localLower.contains("copa")) {
                        corStatus = Color.parseColor("#FFC107"); // Amarelo (Áreas específicas)
                        textoStatus = "EM PAUSA";
                    } else {
                        corStatus = Color.parseColor("#4CAF50"); // Verde (Presente)
                        textoStatus = "PRESENTE";
                    }
                    
                    tempo = "desde " + formatarHora(log.getDataEntrada());
                } else {
                    local = "Última saída: " + formatarHora(log.getDataSaida());
                }
            }

            binding.tvLocalizacaoEquipe.setText(local);
            binding.tvStatusBadge.setText(textoStatus);
            binding.cardStatusEquipe.setCardBackgroundColor(ColorStateList.valueOf(corStatus));
            binding.tvTempoLocalizacao.setText(estaPresente ? tempo : "");
            
            // Controle de visibilidade para reduzir redundância
            int visibilidade = estaPresente ? View.VISIBLE : View.GONE;
            binding.layoutLocalizacao.setVisibility(visibilidade);
            binding.tvTempoLocalizacao.setVisibility(visibilidade);

            // Marcador de Presença (Círculo na foto)
            binding.viewPresencaStatus.setBackgroundTintList(ColorStateList.valueOf(corStatus));

            if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(usuario.getFotoPerfil())
                        .placeholder(R.drawable.outline_person_24)
                        .into(binding.ivFotoEquipe);
            } else {
                binding.ivFotoEquipe.setImageResource(R.drawable.outline_person_24);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(usuario));
        }

        private String formatarHora(String dataStr) {
            if (dataStr == null || dataStr.isEmpty()) return "--:--";
            try {
                // Tenta diversos formatos comuns
                String[] formatos = {"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "HH:mm:ss", "HH:mm"};
                for (String f : formatos) {
                    try {
                        SimpleDateFormat parser = new SimpleDateFormat(f, Locale.getDefault());
                        Date date = parser.parse(dataStr);
                        if (date != null) {
                            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
                        }
                    } catch (Exception ignored) {}
                }
                return dataStr; // Retorna original se não conseguir parsear
            } catch (Exception e) {
                return "--:--";
            }
        }
    }
}

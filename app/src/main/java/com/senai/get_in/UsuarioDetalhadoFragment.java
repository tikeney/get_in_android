package com.senai.get_in;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.senai.get_in.api.LogRepository;
import com.senai.get_in.api.UsuarioRepository;
import com.senai.get_in.databinding.FragmentUsuarioDetalhadoBinding;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioDetalhadoResponse;
import com.senai.get_in.utils.ToastUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioDetalhadoFragment extends Fragment {

    private FragmentUsuarioDetalhadoBinding binding;
    private UsuarioRepository usuarioRepository;
    private LogRepository logRepository;
    private int usuarioId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioId = getArguments().getInt("usuario_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUsuarioDetalhadoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        usuarioRepository = new UsuarioRepository(requireContext());
        logRepository = new LogRepository(requireContext());
        
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        carregarDados();
    }

    private void carregarDados() {
        usuarioRepository.getUsuarioPorId(usuarioId, new Callback<UsuarioDetalhadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Response<UsuarioDetalhadoResponse> response) {
                if (isAdded() && binding != null && response.isSuccessful() && response.body() != null) {
                    exibirDados(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Throwable t) {
                if (isAdded()) ToastUtils.showError(getContext(), "Erro ao carregar detalhes");
            }
        });

        logRepository.getLogs(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (isAdded() && binding != null && response.isSuccessful() && response.body() != null) {
                    buscarUltimaAtividade(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {}
        });
    }

    private void exibirDados(UsuarioDetalhado usuario) {
        binding.tvNomeDetalhe.setText(usuario.getNome());
        binding.tvCargoDetalhe.setText(usuario.getCargo() + " — " + usuario.getDepartamentoNome());
        binding.tvEmailDetalhe.setText(usuario.getEmail());
        binding.tvCelularDetalhe.setText(usuario.getCelular());
        binding.tvCpfDetalhe.setText(usuario.getCpf());

        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
            Glide.with(this).load(usuario.getFotoPerfil()).placeholder(R.drawable.outline_person_24).into(binding.ivFotoDetalhe);
        }
    }

    private void buscarUltimaAtividade(List<LogAcesso> logs) {
        if (logs == null || binding == null) return;
        
        String nomeUsuario = binding.tvNomeDetalhe.getText().toString();
        LogAcesso ultimoLog = null;
        
        for (LogAcesso log : logs) {
            if (log.getUsuarioNome() != null && log.getUsuarioNome().equalsIgnoreCase(nomeUsuario)) {
                ultimoLog = log;
                break;
            }
        }

        if (ultimoLog != null) {
            boolean estaPresente = ultimoLog.getDataSaida() == null || ultimoLog.getDataSaida().isEmpty();
            String local = ultimoLog.getLocalDispositivo() != null ? ultimoLog.getLocalDispositivo() : "Área Interna";
            
            int corStatus = Color.parseColor("#F44336");
            String textoStatus = "AUSENTE";
            
            if (estaPresente) {
                String localLower = local.toLowerCase();
                if (localLower.contains("refeitorio") || localLower.contains("ambulatorio") || localLower.contains("copa")) {
                    corStatus = Color.parseColor("#FFC107");
                    textoStatus = "EM PAUSA";
                } else {
                    corStatus = Color.parseColor("#4CAF50");
                    textoStatus = "PRESENTE";
                }
                binding.tvUltimoLocalDetalhe.setText("Localizado em: " + local);
                binding.tvUltimoHorarioDetalhe.setText("Entrada às " + ultimoLog.getDataEntrada());
            } else {
                binding.tvUltimoLocalDetalhe.setText("Última saída registrada");
                binding.tvUltimoHorarioDetalhe.setText("Horário: " + ultimoLog.getDataSaida() + " via " + local);
            }

            binding.tvStatusTextoDetalhe.setText(textoStatus);
            binding.badgeStatusDetalhe.setCardBackgroundColor(ColorStateList.valueOf(corStatus));
            binding.viewStatusBadgeDetalhe.setBackgroundTintList(ColorStateList.valueOf(corStatus));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

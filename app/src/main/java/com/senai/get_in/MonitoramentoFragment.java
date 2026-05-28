package com.senai.get_in;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.senai.get_in.adapter.MonitoramentoAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.databinding.FragmentMonitoramentoBinding;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.model.VisitanteLocal;
import com.senai.get_in.model.VisitanteLocalResponse;
import com.senai.get_in.utils.SearchableFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoramentoFragment extends Fragment {

    private static final String TAG = "MonitoramentoFragment";
    private static final long REFRESH_INTERVAL = 30000; // 30 segundos

    private FragmentMonitoramentoBinding binding;
    private long countVisitantes = 0;
    private long countFuncionarios = 0;
    
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            atualizarDashboard();
            // Agenda a próxima execução
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMonitoramentoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTabs();
        setupSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Inicia o ciclo de atualização automática quando a tela está visível
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Para a atualização ao sair da tela para economizar bateria e processamento
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void setupTabs() {
        MonitoramentoAdapter adapter = new MonitoramentoAdapter(
                getChildFragmentManager(),
                getViewLifecycleOwner().getLifecycle()
        );

        binding.viewPagerMonitoramento.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayoutMonitoramento, binding.viewPagerMonitoramento, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Pendentes"); break;
                case 1: tab.setText("Histórico"); break;
                case 2: tab.setText("Equipe"); break;
            }
        }).attach();

        binding.viewPagerMonitoramento.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.etSharedBusca.setText("");
            }
        });
    }

    private void setupSearch() {
        binding.etSharedBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                Fragment currentFragment = getChildFragmentManager()
                        .findFragmentByTag("f" + binding.viewPagerMonitoramento.getCurrentItem());
                
                if (currentFragment instanceof SearchableFragment) {
                    ((SearchableFragment) currentFragment).onSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarDashboard() {
        // 1. Busca Visitantes Ativos
        RetrofitClient.getApiService(requireContext()).getVisitantesLocal().enqueue(new Callback<VisitanteLocalResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitanteLocalResponse> call, @NonNull Response<VisitanteLocalResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<VisitanteLocal> lista = response.body().getDados();
                    if (lista != null) {
                        countVisitantes = lista.stream().filter(v -> "Dentro".equalsIgnoreCase(v.getStatus())).count();
                        updateHeaderUI();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<VisitanteLocalResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro visitantes: " + t.getMessage());
            }
        });

        // 2. Busca Logs para calcular Funcionários
        RetrofitClient.getApiService(requireContext()).getLogs().enqueue(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<LogAcesso> logs = response.body().getData();
                    if (logs != null) {
                        long totalNoLocal = logs.stream().filter(l -> l.getDataSaida() == null || l.getDataSaida().isEmpty()).count();
                        countFuncionarios = Math.max(0, totalNoLocal - countVisitantes);
                        updateHeaderUI();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro logs: " + t.getMessage());
            }
        });
    }

    private void updateHeaderUI() {
        if (binding == null) return;
        binding.tvContadorVisitantes.setText(String.valueOf(countVisitantes));
        binding.tvContadorFuncionarios.setText(String.valueOf(countFuncionarios));
        binding.tvContadorTotal.setText(String.valueOf(countVisitantes + countFuncionarios));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.senai.get_in;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.senai.get_in.adapter.MonitoramentoAdapter;
import com.senai.get_in.api.LogRepository;
import com.senai.get_in.databinding.FragmentMonitoramentoBinding;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.utils.SearchableFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoramentoFragment extends Fragment {

    private FragmentMonitoramentoBinding binding;
    private LogRepository logRepository;

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

        logRepository = new LogRepository(requireContext());
        setupTabs();
        setupSearch();
        atualizarEstatisticas();
    }

    private void setupTabs() {
        MonitoramentoAdapter adapter = new MonitoramentoAdapter(
                getChildFragmentManager(),
                getViewLifecycleOwner().getLifecycle()
        );

        binding.viewPagerMonitoramento.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayoutMonitoramento, binding.viewPagerMonitoramento, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Requisições"); break;
                case 1: tab.setText("Histórico"); break;
                case 2: tab.setText("Minha Equipe"); break;
            }
        }).attach();

        binding.viewPagerMonitoramento.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setToolbarTitle("Atividade");
                }
                // Limpa a busca ao trocar de aba (opcional, para evitar confusão)
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
                // Notifica o fragmento atual sobre a nova busca
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

    private void atualizarEstatisticas() {
        logRepository.getLogs(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (isAdded() && binding != null && response.isSuccessful() && response.body() != null) {
                    int total = response.body().getData() != null ? response.body().getData().size() : 0;
                    long naFabrica = response.body().getData() != null ? 
                            response.body().getData().stream().filter(l -> l.getDataSaida() == null || l.getDataSaida().isEmpty()).count() : 0;
                    
                    binding.tvContadorHoje.setText(String.valueOf(total));
                    binding.tvContadorNaFabrica.setText(String.valueOf(naFabrica));
                    binding.tvContadorNegados.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.senai.get_in;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.senai.get_in.adapter.LogAdapter;
import com.senai.get_in.api.LogRepository;
import com.senai.get_in.databinding.FragmentHistoricoBinding;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.utils.NetworkUtils;
import com.senai.get_in.utils.SearchableFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment implements SearchableFragment {

    private static final String TAG = "HistoricoFragment";
    private static final long REFRESH_INTERVAL = 30000;

    private FragmentHistoricoBinding binding;
    private LogAdapter adapter;
    private LogRepository repository;
    private List<LogAcesso> listaCompleta = new ArrayList<>();
    private List<LogAcesso> listaFiltrada = new ArrayList<>();
    private String currentQuery = "";

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadData(true);
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoricoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new LogRepository(requireContext());
        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();
        
        loadData(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void setupSwipeRefresh() {
        binding.swipeHistorico.setOnRefreshListener(() -> loadData(false));
        binding.swipeHistorico.setColorSchemeResources(R.color.primary);
    }

    private void loadData(boolean isBackgroundRefresh) {
        if (!NetworkUtils.isOnline(getContext())) {
            binding.swipeHistorico.setRefreshing(false);
            return;
        }

        if (!isBackgroundRefresh && listaCompleta.isEmpty()) setLoading(true);

        repository.getHistoricoPortaria(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<LogAcesso> logs = response.body().getData();
                    if (logs != null) {
                        listaCompleta.clear();
                        listaCompleta.addAll(logs);
                        filterList();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter(listaFiltrada);
        binding.recyclerHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistorico.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> filterList());
    }

    @Override
    public void onSearch(String query) {
        this.currentQuery = query.toLowerCase().trim();
        filterList();
    }

    private void filterList() {
        if (listaCompleta == null || binding == null) return;
        
        int checkedChipId = binding.chipGroupFiltros.getCheckedChipId();
        listaFiltrada.clear();

        for (LogAcesso log : listaCompleta) {
            String nome = log.getUsuarioNome() != null ? log.getUsuarioNome().toLowerCase() : "";
            String empresa = log.getLocalDispositivo() != null ? log.getLocalDispositivo().toLowerCase() : "";

            // Filtro de Busca
            boolean matchesSearch = currentQuery.isEmpty() || nome.contains(currentQuery) || empresa.contains(currentQuery);
            
            // Filtro de Chip
            boolean matchesChip = true;
            if (checkedChipId == R.id.chipSaiu) {
                matchesChip = log.getDataSaida() != null;
            } else if (checkedChipId == R.id.chipNaFabrica) {
                matchesChip = log.getDataSaida() == null;
            }

            if (matchesSearch && matchesChip) {
                listaFiltrada.add(log);
            }
        }

        adapter.updateList(listaFiltrada);
        binding.layoutVazio.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerHistorico.setVisibility(listaFiltrada.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        if (loading) {
            binding.shimmerHistorico.setVisibility(View.VISIBLE);
            binding.shimmerHistorico.startShimmer();
            binding.recyclerHistorico.setVisibility(View.GONE);
        } else {
            binding.shimmerHistorico.stopShimmer();
            binding.shimmerHistorico.setVisibility(View.GONE);
            binding.recyclerHistorico.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

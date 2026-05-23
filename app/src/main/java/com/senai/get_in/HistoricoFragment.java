package com.senai.get_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.util.Log;

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
import com.senai.get_in.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment implements SearchableFragment {

    private static final String TAG = "HistoricoFragment";
    private FragmentHistoricoBinding binding;
    private LogAdapter adapter;
    private LogRepository repository;
    private List<LogAcesso> listaCompleta = new ArrayList<>();
    private List<LogAcesso> listaFiltrada = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

    private void setupSwipeRefresh() {
        binding.swipeHistorico.setOnRefreshListener(() -> loadData(true));
        binding.swipeHistorico.setColorSchemeResources(R.color.primary);
    }

    private void loadData(boolean isRefreshing) {
        if (!NetworkUtils.isOnline(getContext())) {
            binding.swipeHistorico.setRefreshing(false);
            ToastUtils.showError(getContext(), "Sem conexão com a internet.");
            return;
        }

        if (!isRefreshing) setLoading(true);
        binding.layoutVazio.setVisibility(View.GONE);

        repository.getLogs(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body().getData();
                    if (listaCompleta == null) listaCompleta = new ArrayList<>();
                    
                    filterList();
                    binding.recyclerHistorico.scheduleLayoutAnimation();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar histórico");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
                Log.e(TAG, "Erro: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    private void setLoading(boolean loading) {
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

    private void setupRecyclerView() {
        adapter = new LogAdapter(new ArrayList<>());
        binding.recyclerHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistorico.setAdapter(adapter);
        
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        binding.recyclerHistorico.setLayoutAnimation(animation);
    }

    private void setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterList();
        });
    }

    @Override
    public void onSearch(String query) {
        this.currentQuery = query.toLowerCase().trim();
        filterList();
    }

    private void filterList() {
        if (listaCompleta == null || binding == null) return;
        
        int checkedChipId = binding.chipGroupFiltros.getCheckedChipId();

        listaFiltrada = listaCompleta.stream()
                .filter(l -> (l.getUsuarioNome() != null && l.getUsuarioNome().toLowerCase().contains(currentQuery)) || 
                             (l.getDepartamentoUsuario() != null && l.getDepartamentoUsuario().toLowerCase().contains(currentQuery)))
                .filter(l -> {
                    boolean isNaFabrica = l.getDataSaida() == null || l.getDataSaida().isEmpty();
                    if (checkedChipId == R.id.chipNaFabrica) {
                        return isNaFabrica;
                    } else if (checkedChipId == R.id.chipSaiu) {
                        return !isNaFabrica;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        adapter.updateList(listaFiltrada);

        if (listaFiltrada.isEmpty()) {
            binding.layoutVazio.setVisibility(View.VISIBLE);
            binding.recyclerHistorico.setVisibility(View.GONE);
        } else {
            binding.layoutVazio.setVisibility(View.GONE);
            binding.recyclerHistorico.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.senai.get_in;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.senai.get_in.adapter.RequisicaoAdapter;
import com.senai.get_in.api.RequisicaoRepository;
import com.senai.get_in.databinding.FragmentHistoricoBinding;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.AccessManager;
import com.senai.get_in.utils.NetworkUtils;
import com.senai.get_in.utils.SearchableFragment;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment implements RequisicaoAdapter.OnItemClickListener, SearchableFragment {

    private static final String TAG = "HistoricoFragment";
    private static final long REFRESH_INTERVAL = 30000;

    private FragmentHistoricoBinding binding;
    private RequisicaoAdapter adapter;
    private RequisicaoRepository repository;
    private List<Requisicao> listaCompleta = new ArrayList<>();
    private List<Requisicao> listaFiltrada = new ArrayList<>();
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
        repository = new RequisicaoRepository(requireContext());
        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();
        
        // Carregamento inicial do banco de dados local
        loadLocalData();
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

    private void loadLocalData() {
        TokenManager tokenManager = new TokenManager(requireContext());
        UsuarioDetalhado user = tokenManager.getUserData();

        RequisicaoRepository.LocalCallback<List<Requisicao>> localCallback = data -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        listaCompleta.clear();
                        listaCompleta.addAll(data);
                        filterList();
                    }
                });
            }
        };

        if (AccessManager.isSupervisor(user) && user.getIdSetor() > 0) {
            repository.getHistoricoLocalPorSetor(user.getIdSetor(), localCallback);
        } else {
            repository.getHistoricoLocal(localCallback);
        }
    }

    private void loadData(boolean isBackgroundRefresh) {
        if (!NetworkUtils.isOnline(getContext())) {
            binding.swipeHistorico.setRefreshing(false);
            return;
        }

        if (!isBackgroundRefresh && listaCompleta.isEmpty()) setLoading(true);

        TokenManager tokenManager = new TokenManager(requireContext());
        UsuarioDetalhado user = tokenManager.getUserData();

        Callback<RequisicaoResponse> callback = new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(@NonNull Call<RequisicaoResponse> call, @NonNull Response<RequisicaoResponse> response) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Requisicao> todas = response.body().getData();
                    if (todas != null) {
                        listaCompleta.clear();
                        // O Histórico mostra tudo que NÃO é pendente
                        for (Requisicao r : todas) {
                            String status = r.getStatus();
                            if (status != null && !status.equalsIgnoreCase("pendente")) {
                                listaCompleta.add(r);
                            }
                        }
                        filterList();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisicaoResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
            }
        };

        if (AccessManager.isSupervisor(user) && user.getIdSetor() > 0) {
            repository.getRequisicoesPorSetor(user.getIdSetor(), callback);
        } else {
            repository.getRequisicoes(callback);
        }
    }

    private void setupRecyclerView() {
        adapter = new RequisicaoAdapter(listaFiltrada, this);
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

        for (Requisicao r : listaCompleta) {
            String nome = r.getUsuarioNome() != null ? r.getUsuarioNome().toLowerCase() : "";
            String empresa = r.getEmpresa() != null ? r.getEmpresa().toLowerCase() : "";
            String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";

            // Filtro de Busca
            boolean matchesSearch = currentQuery.isEmpty() || nome.contains(currentQuery) || empresa.contains(currentQuery);
            
            // Filtro de Chip (Permitidos / Negados)
            boolean matchesStatus = true;
            if (checkedChipId == R.id.chipPermitidos) {
                matchesStatus = status.contains("aprovado") || status.contains("liberado");
            } else if (checkedChipId == R.id.chipNegado) {
                matchesStatus = status.contains("recusado") || status.contains("negado");
            }

            if (matchesSearch && matchesStatus) {
                listaFiltrada.add(r);
            }
        }

        adapter.notifyDataSetChanged();
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
    public void onAprovarClick(Requisicao requisicao) {
        atualizarStatus(requisicao, "aprovado");
    }

    @Override
    public void onNegarClick(Requisicao requisicao) {
        atualizarStatus(requisicao, "recusado");
    }

    private void atualizarStatus(Requisicao requisicao, String novoStatus) {
        setLoading(true);
        Requisicao update = new Requisicao();
        update.setStatus(novoStatus);

        repository.atualizarStatus(requisicao.getId(), update, new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Decisão atualizada!");
                    loadData(false);
                } else {
                    setLoading(false);
                    ToastUtils.showError(getContext(), "Erro ao atualizar");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

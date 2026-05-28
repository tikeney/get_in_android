package com.senai.get_in;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment implements RequisicaoAdapter.OnItemClickListener, SearchableFragment {

    private static final String TAG = "HistoricoFragment";
    private static final long REFRESH_INTERVAL = 30000; // 30 segundos

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        binding.swipeHistorico.setOnRefreshListener(() -> loadData(true));
        binding.swipeHistorico.setColorSchemeResources(R.color.primary);
    }

    private void loadData(boolean isBackgroundRefresh) {
        if (!NetworkUtils.isOnline(getContext())) {
            binding.swipeHistorico.setRefreshing(false);
            if (!isBackgroundRefresh) ToastUtils.showError(getContext(), "Sem conexão com a internet.");
            return;
        }

        if (!isBackgroundRefresh && listaFiltrada.isEmpty()) setLoading(true);
        binding.layoutVazio.setVisibility(View.GONE);

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
                    listaCompleta.clear();
                    if (todas != null) {
                        // Filtra apenas o que JÁ FOI decidido (Aprovado ou Recusado)
                        listaCompleta.addAll(todas.stream()
                                .filter(r -> r.getStatus() != null && !"pendente".equalsIgnoreCase(r.getStatus()))
                                .collect(Collectors.toList()));
                    }
                    filterList();
                } else {
                    if (!isBackgroundRefresh) ToastUtils.showError(getContext(), "Erro ao carregar histórico");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisicaoResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeHistorico.setRefreshing(false);
                Log.e(TAG, "Erro: " + t.getMessage());
                if (!isBackgroundRefresh) ToastUtils.showError(getContext(), "Falha na conexão");
            }
        };

        if (AccessManager.isSupervisor(user) && user.getIdSetor() > 0) {
            repository.getRequisicoesPorSetor(user.getIdSetor(), callback);
        } else {
            repository.getRequisicoes(callback);
        }
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
        adapter = new RequisicaoAdapter(listaFiltrada, this);
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

        listaFiltrada.clear();
        listaFiltrada.addAll(listaCompleta.stream()
                .filter(r -> (r.getUsuarioNome() != null && r.getUsuarioNome().toLowerCase().contains(currentQuery)) || 
                             (r.getEmpresa() != null && r.getEmpresa().toLowerCase().contains(currentQuery)))
                .filter(r -> {
                    if (checkedChipId == R.id.chipPermitidos) {
                        return "aprovado".equalsIgnoreCase(r.getStatus());
                    } else if (checkedChipId == R.id.chipNegado) {
                        return "recusado".equalsIgnoreCase(r.getStatus());
                    }
                    return true;
                })
                .collect(Collectors.toList()));

        adapter.notifyDataSetChanged();

        if (listaFiltrada.isEmpty()) {
            binding.layoutVazio.setVisibility(View.VISIBLE);
            binding.recyclerHistorico.setVisibility(View.GONE);
        } else {
            binding.layoutVazio.setVisibility(View.GONE);
            binding.recyclerHistorico.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAprovarClick(Requisicao requisicao) {
        mostrarConfirmacao(requisicao, "aprovado");
    }

    @Override
    public void onNegarClick(Requisicao requisicao) {
        mostrarConfirmacao(requisicao, "recusado");
    }

    private void mostrarConfirmacao(Requisicao requisicao, String novoStatus) {
        String msg = "Deseja alterar o status desta requisição para " + novoStatus.toUpperCase() + "?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Alterar Decisão")
                .setMessage(msg)
                .setPositiveButton("Confirmar", (dialog, which) -> atualizarStatus(requisicao, novoStatus))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarStatus(Requisicao requisicao, String novoStatus) {
        setLoading(true);
        Requisicao update = new Requisicao();
        update.setStatus(novoStatus);

        repository.atualizarStatus(requisicao.getId(), update, new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Status atualizado com sucesso!");
                    loadData(false);
                } else {
                    setLoading(false);
                    ToastUtils.showError(getContext(), "Erro ao atualizar status");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded()) return;
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

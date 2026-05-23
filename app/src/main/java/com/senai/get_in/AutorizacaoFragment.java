package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.senai.get_in.adapter.RequisicaoAdapter;
import com.senai.get_in.api.RequisicaoRepository;
import com.senai.get_in.databinding.FragmentAutorizacaoBinding;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;
import com.senai.get_in.utils.NetworkUtils;
import com.senai.get_in.utils.SearchableFragment;
import com.senai.get_in.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutorizacaoFragment extends Fragment implements RequisicaoAdapter.OnItemClickListener, SearchableFragment {

    private static final String TAG = "AutorizacaoFragment";
    private FragmentAutorizacaoBinding binding;
    private RequisicaoAdapter adapter;
    private RequisicaoRepository repository;
    private List<Requisicao> listaCompleta = new ArrayList<>();
    private List<Requisicao> listaFiltrada = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAutorizacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repository = new RequisicaoRepository(requireContext());
        
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        binding.containerAutorizacao.startAnimation(slideUp);

        setupRecyclerView();
        carregarRequisicoes();
    }

    private void setupRecyclerView() {
        adapter = new RequisicaoAdapter(listaFiltrada, this);
        binding.recyclerRequisicao.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerRequisicao.setAdapter(adapter);
        
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        binding.recyclerRequisicao.setLayoutAnimation(animation);
    }

    private void carregarRequisicoes() {
        if (!NetworkUtils.isOnline(getContext())) {
            ToastUtils.showError(getContext(), "Sem conexão com a internet.");
            return;
        }

        setLoading(true);
        binding.layoutVazioAutorizacao.setVisibility(View.GONE);

        repository.getRequisicoes(new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(@NonNull Call<RequisicaoResponse> call, @NonNull Response<RequisicaoResponse> response) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Requisicao> todas = response.body().getData();
                    listaCompleta.clear();
                    if (todas != null) {
                        for (Requisicao r : todas) {
                            if (r.getStatus() != null && "pendente".equalsIgnoreCase(r.getStatus())) {
                                listaCompleta.add(r);
                            }
                        }
                    }
                    filterList();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar dados (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisicaoResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
                ToastUtils.showError(getContext(), "Sem conexão com o servidor");
            }
        });
    }

    @Override
    public void onSearch(String query) {
        this.currentQuery = query.toLowerCase().trim();
        filterList();
    }

    private void filterList() {
        if (listaCompleta == null || binding == null) return;

        listaFiltrada.clear();
        listaFiltrada.addAll(listaCompleta.stream()
                .filter(r -> (r.getUsuarioNome() != null && r.getUsuarioNome().toLowerCase().contains(currentQuery)) ||
                             (r.getEmpresa() != null && r.getEmpresa().toLowerCase().contains(currentQuery)))
                .collect(Collectors.toList()));

        adapter.notifyDataSetChanged();
        binding.recyclerRequisicao.scheduleLayoutAnimation();
        binding.layoutVazioAutorizacao.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.shimmerAutorizacao.setVisibility(View.VISIBLE);
            binding.shimmerAutorizacao.startShimmer();
            binding.recyclerRequisicao.setVisibility(View.GONE);
        } else {
            binding.shimmerAutorizacao.stopShimmer();
            binding.shimmerAutorizacao.setVisibility(View.GONE);
            binding.recyclerRequisicao.setVisibility(View.VISIBLE);
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
        if (!NetworkUtils.isOnline(getContext())) {
            ToastUtils.showError(getContext(), "Sem conexão para realizar esta ação.");
            return;
        }

        if (binding == null) return;
        setLoading(true);
        
        Requisicao update = new Requisicao();
        update.setStatus(novoStatus);

        repository.atualizarStatus(requisicao.getId(), update, new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Sucesso!");
                    carregarRequisicoes();
                } else {
                    setLoading(false);
                    ToastUtils.showError(getContext(), "Erro ao atualizar");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                ToastUtils.showError(getContext(), "Falha na rede");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

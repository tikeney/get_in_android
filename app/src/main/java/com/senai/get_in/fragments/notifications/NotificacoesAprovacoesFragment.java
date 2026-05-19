package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.senai.get_in.R;
import com.senai.get_in.adapter.RequisicaoAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.RequisicaoResponse;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesAprovacoesFragment extends Fragment {

    private static final String TAG = "NotificacoesAprovacoes";
    private RecyclerView rvRequisicoes;
    private RequisicaoAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacoes_aprovacoes, container, false);

        rvRequisicoes = view.findViewById(R.id.rvRequisicoes);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tokenManager = new TokenManager(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();
        carregarRequisicoes();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RequisicaoAdapter(new ArrayList<>(), new RequisicaoAdapter.OnItemClickListener() {
            @Override
            public void onAprovarClick(Requisicao requisicao) {
                atualizarStatusRequisicao(requisicao, "aprovado");
            }

            @Override
            public void onNegarClick(Requisicao requisicao) {
                atualizarStatusRequisicao(requisicao, "recusado");
            }
        });
        rvRequisicoes.setAdapter(adapter);
        
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        rvRequisicoes.setLayoutAnimation(animation);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::carregarRequisicoes);
            swipeRefresh.setColorSchemeResources(R.color.blue);
        }
    }

    private void carregarRequisicoes() {
        String token = tokenManager.getToken();
        if (token == null) {
            Log.e(TAG, "Token não encontrado!");
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        if (progressBar != null && (swipeRefresh == null || !swipeRefresh.isRefreshing())) {
            progressBar.setVisibility(View.VISIBLE);
            rvRequisicoes.setVisibility(View.INVISIBLE);
        }

        RetrofitClient.getApiService().getRequisicoes("Bearer " + token).enqueue(new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(@NonNull Call<RequisicaoResponse> call, @NonNull Response<RequisicaoResponse> response) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                rvRequisicoes.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Requisicao> listaRequisicoes = response.body().getData();
                    List<Requisicao> pendentes = new ArrayList<>();
                    
                    if (listaRequisicoes != null) {
                        for (Requisicao r : listaRequisicoes) {
                            if (r.getStatus() != null && "pendente".equalsIgnoreCase(r.getStatus())) {
                                pendentes.add(r);
                            }
                        }
                    }
                    
                    adapter.updateList(pendentes);
                    rvRequisicoes.scheduleLayoutAnimation();
                    
                    if (pendentes.isEmpty()) {
                        ToastUtils.showInfo(getContext(), "Nenhuma requisição pendente.");
                    }
                } else {
                    ToastUtils.showError(getContext(), "Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisicaoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                rvRequisicoes.setVisibility(View.VISIBLE);
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
                ToastUtils.showError(getContext(), "Erro de conexão");
            }
        });
    }

    private void atualizarStatusRequisicao(Requisicao requisicao, String novoStatus) {
        String token = tokenManager.getToken();
        if (token == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        String statusOriginal = requisicao.getStatus();
        requisicao.setStatus(novoStatus);

        RetrofitClient.getApiService().atualizarStatus("Bearer " + token, requisicao.getId(), requisicao).enqueue(new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Sucesso!");
                    carregarRequisicoes();
                } else {
                    requisicao.setStatus(statusOriginal);
                    ToastUtils.showError(getContext(), "Erro ao atualizar");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                requisicao.setStatus(statusOriginal);
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }
}

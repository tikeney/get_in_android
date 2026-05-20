package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AutorizacaoFragment extends Fragment implements RequisicaoAdapter.OnItemClickListener {

    private static final String TAG = "AutorizacaoFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View container;
    private RequisicaoAdapter adapter;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_autorizacao, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerRequisicao);
        progressBar = view.findViewById(R.id.progressBarAutorizacao);
        this.container = view.findViewById(R.id.containerAutorizacao);
        
        tokenManager = new TokenManager(requireContext());
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (container != null) {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            container.startAnimation(slideUp);
        }

        setupRecyclerView();
        carregarRequisicoes();
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            adapter = new RequisicaoAdapter(listaRequisicoes, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
        }
    }

    private void carregarRequisicoes() {
        progressBar.setVisibility(View.VISIBLE);
        
        RetrofitClient.getApiService(requireContext()).getRequisicoes().enqueue(new Callback<RequisicaoResponse>() {
            @Override
            public void onResponse(@NonNull Call<RequisicaoResponse> call, @NonNull Response<RequisicaoResponse> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Requisicao> todas = response.body().getData();
                    List<Requisicao> pendentes = new ArrayList<>();
                    
                    if (todas != null) {
                        for (Requisicao r : todas) {
                            if ("pendente".equalsIgnoreCase(r.getStatus())) {
                                pendentes.add(r);
                            }
                        }
                    }
                    
                    listaRequisicoes.clear();
                    listaRequisicoes.addAll(pendentes);
                    adapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar requisições");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisicaoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Erro: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão com o servidor");
            }
        });
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
        progressBar.setVisibility(View.VISIBLE);
        
        Requisicao update = new Requisicao();
        update.setStatus(novoStatus);

        RetrofitClient.getApiService(requireContext()).atualizarStatus(requisicao.getId(), update)
                .enqueue(new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Requisição " + novoStatus);
                    carregarRequisicoes();
                } else {
                    progressBar.setVisibility(View.GONE);
                    ToastUtils.showError(getContext(), "Erro ao atualizar status");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }
}
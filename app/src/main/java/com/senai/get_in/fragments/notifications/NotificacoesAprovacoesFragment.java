package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.R;
import com.senai.get_in.adapter.RequisicaoAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesAprovacoesFragment extends Fragment {

    private RecyclerView rvRequisicoes;
    private RequisicaoAdapter adapter;
    private ProgressBar progressBar;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacoes_aprovacoes, container, false);

        rvRequisicoes = view.findViewById(R.id.rvRequisicoes);
        progressBar = view.findViewById(R.id.progressBar);
        tokenManager = new TokenManager(requireContext());

        setupRecyclerView();
        carregarRequisicoes();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RequisicaoAdapter(listaRequisicoes, new RequisicaoAdapter.OnItemClickListener() {
            @Override
            public void onAprovarClick(Requisicao requisicao) {
                atualizarStatusRequisicao(requisicao, "aprovado");
            }

            @Override
            public void onNegarClick(Requisicao requisicao) {
                atualizarStatusRequisicao(requisicao, "negado");
            }
        });
        rvRequisicoes.setAdapter(adapter);
    }

    private void carregarRequisicoes() {
        String token = tokenManager.getToken();
        if (token == null) return;

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getRequisicoes("Bearer " + token).enqueue(new Callback<List<Requisicao>>() {
            @Override
            public void onResponse(Call<List<Requisicao>> call, Response<List<Requisicao>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listaRequisicoes = response.body();
                    List<Requisicao> pendentes = new ArrayList<>();
                    for (Requisicao r : listaRequisicoes) {
                        if ("pendente".equalsIgnoreCase(r.getStatus())) {
                            pendentes.add(r);
                        }
                    }
                    adapter.updateList(pendentes);
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Requisicao>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarStatusRequisicao(Requisicao requisicao, String novoStatus) {
        String token = tokenManager.getToken();
        if (token == null) return;

        progressBar.setVisibility(View.VISIBLE);
        requisicao.setStatus(novoStatus);

        RetrofitClient.getApiService().atualizarStatus("Bearer " + token, requisicao.getId(), requisicao).enqueue(new Callback<Requisicao>() {
            @Override
            public void onResponse(Call<Requisicao> call, Response<Requisicao> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Status atualizado: " + novoStatus, Toast.LENGTH_SHORT).show();
                    carregarRequisicoes();
                } else {
                    Toast.makeText(getContext(), "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Requisicao> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

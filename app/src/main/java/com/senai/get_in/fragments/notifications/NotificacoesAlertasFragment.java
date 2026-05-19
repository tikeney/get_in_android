package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.R;
import com.senai.get_in.adapter.NotificacoesListAdapter;
import com.senai.get_in.model.Notificacao;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesAlertasFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtSemNotificacoes;
    private ProgressBar progressBar;
    private List<Notificacao> listaNotificacoes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacoes_alertas, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistorico);
        txtSemNotificacoes = view.findViewById(R.id.txtSemNotificacoes);
        progressBar = view.findViewById(R.id.progressBarAlertas);

        configurarRecyclerView();
        carregarNotificacoes();

        return view;
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        
        // Adiciona animação de entrada na lista
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
    }

    private void carregarNotificacoes() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        
        // Simulando um delay de rede para mostrar o ProgressBar e a fluidez
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            
            progressBar.setVisibility(View.GONE);
            
            // Dados temporários para teste de alertas
            listaNotificacoes.clear();
            listaNotificacoes.add(new Notificacao("Alerta de Segurança", "Tentativa de acesso não autorizado detectada.", "14:00"));
            listaNotificacoes.add(new Notificacao("Portaria", "Visitante aguardando liberação no portão A.", "14:30"));

            if (listaNotificacoes.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                txtSemNotificacoes.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                txtSemNotificacoes.setVisibility(View.GONE);
                NotificacoesListAdapter adapter = new NotificacoesListAdapter(listaNotificacoes);
                recyclerView.setAdapter(adapter);
                recyclerView.scheduleLayoutAnimation();
            }
        }, 800);
    }
}

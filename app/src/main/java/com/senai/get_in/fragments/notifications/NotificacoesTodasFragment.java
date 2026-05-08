package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class NotificacoesTodasFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtSemNotificacoes;
    private List<Notificacao> listaNotificacoes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacoes_todas, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistorico);
        txtSemNotificacoes = view.findViewById(R.id.txtSemNotificacoes);

        configurarRecyclerView();
        carregarNotificacoes();

        return view;
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    private void carregarNotificacoes() {
        // Dados temporários para teste
        // Para testar a mensagem de "sem notificações", basta comentar as linhas abaixo
        listaNotificacoes.add(new Notificacao("Entrada Autorizada", "O visitante João Silva teve sua entrada autorizada.", "10:30"));
        listaNotificacoes.add(new Notificacao("Novo Cadastro", "Um novo visitante solicitou cadastro no sistema.", "11:15"));
        listaNotificacoes.add(new Notificacao("Alerta de Segurança", "Tentativa de acesso não autorizado detectada.", "14:00"));

        if (listaNotificacoes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            txtSemNotificacoes.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            txtSemNotificacoes.setVisibility(View.GONE);
            NotificacoesListAdapter adapter = new NotificacoesListAdapter(listaNotificacoes);
            recyclerView.setAdapter(adapter);
        }
    }
}

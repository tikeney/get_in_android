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

public class NotificacoesAprovacoesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtSemNotificacoes;
    private List<Notificacao> listaNotificacoes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacoes_aprovacoes, container, false);

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
        // Para este fragmento de "Aprovações", deixaremos a lista vazia para testar a mensagem de estado vazio
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

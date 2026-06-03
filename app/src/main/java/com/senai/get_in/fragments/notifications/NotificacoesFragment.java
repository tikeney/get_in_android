package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.senai.get_in.R;
import com.senai.get_in.adapter.NotificacoesAdapter;
import com.senai.get_in.adapter.NotificacoesListAdapter;
import com.senai.get_in.databinding.FragmentNotificacoesBinding;
import com.senai.get_in.model.Notificacao;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesFragment extends Fragment {

    private FragmentNotificacoesBinding binding;
    private NotificacoesListAdapter adapter;
    private final List<Notificacao> listaCompleta = new ArrayList<>();
    private final List<Notificacao> listaFiltrada = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificacoesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        NotificacoesAdapter adapter = new NotificacoesAdapter(
                getChildFragmentManager(),
                getViewLifecycleOwner().getLifecycle()
        );

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Todas"); break;
                case 1: tab.setText("Alertas"); break;
                case 2: tab.setText("Sistema"); break;
            }
        }).attach();

//        // Sincroniza o título da Toolbar com a aba selecionada
//        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                if (getActivity() instanceof MainActivity) {
//                    String title = "Notificações";
//                    switch (position) {
//                        case 0: title = "Todas as Notificações"; break;
//                        case 1: title = "Alertas de Segurança"; break;
//                        case 2: title = "Notificações de Sistema"; break;
//                    }
//                    ((MainActivity) getActivity()).setToolbarTitle(title);
//                }
//            }
//        });
        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();
        
        loadMockData();
    }

    private void setupRecyclerView() {
        adapter = new NotificacoesListAdapter(listaFiltrada);
        binding.recyclerNotificacoes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerNotificacoes.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> filterList());
    }

    private void setupSwipeRefresh() {
        binding.swipeNotificacoes.setOnRefreshListener(this::loadMockData);
        binding.swipeNotificacoes.setColorSchemeResources(R.color.primary);
    }

    private void loadMockData() {
        binding.swipeNotificacoes.setRefreshing(true);
        
        // Simulação de notificações de sistema (Mock)
        listaCompleta.clear();
        listaCompleta.add(new Notificacao("Sistema Online", "O servidor central está operando normalmente.", "08:00"));
        listaCompleta.add(new Notificacao("Atualização Disponível", "Uma nova versão do Get In está pronta para instalação.", "09:30"));
        listaCompleta.add(new Notificacao("Alerta de Backup", "Backup diário concluído com sucesso.", "00:05"));
        listaCompleta.add(new Notificacao("Manutenção Agendada", "O sistema passará por manutenção no domingo às 02h.", "Ontem"));

        filterList();
        binding.swipeNotificacoes.setRefreshing(false);
    }

    private void filterList() {
        if (binding == null) return;
        
        // Atualmente todas as notificações são do sistema. 
        // Filtros podem ser implementados aqui conforme a model Notificacao evoluir.
        listaFiltrada.clear();
        listaFiltrada.addAll(listaCompleta);

        adapter.updateList(listaFiltrada);
        binding.layoutVazio.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

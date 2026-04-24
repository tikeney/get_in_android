package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.senai.get_in.R;
import com.senai.get_in.adapter.NotificacoesAdapter;

public class NotificacoesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notificacoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Pega as referências dos componentes do layout
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // 2. Cria o adapter passando:
        //    - getChildFragmentManager(): gerenciador de fragments FILHO
        //      (use getChildFragmentManager() dentro de Fragment, nunca getParentFragmentManager())
        //    - getViewLifecycleOwner().getLifecycle(): ciclo de vida correto para fragments
        NotificacoesAdapter adapter = new NotificacoesAdapter(
                getChildFragmentManager(),
                getViewLifecycleOwner().getLifecycle()
        );

        // 3. Conecta o adapter ao ViewPager
        viewPager.setAdapter(adapter);

        // 4. TabLayoutMediator: conecta o TabLayout ao ViewPager2
        //    Sem isso, as abas não sincronizam com o deslize
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Define o texto de cada aba pelo índice
            switch (position) {
                case 0:
                    tab.setText("Todas");
                    break;
                case 1:
                    tab.setText("Aprovações");
                    break;
                case 2:
                    tab.setText("Alertas");
                    break;
                case 3:
                    tab.setText("Sistema");
                    break;
            }
        }).attach(); // .attach() é obrigatório para ativar a sincronização
    }
}
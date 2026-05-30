package com.senai.get_in.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.senai.get_in.MainActivity;
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
    }
}

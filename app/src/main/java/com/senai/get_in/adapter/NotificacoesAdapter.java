
package com.senai.get_in.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.senai.get_in.fragments.notifications.NotificacoesAlertasFragment;
import com.senai.get_in.fragments.notifications.NotificacoesAprovacoesFragment;
import com.senai.get_in.fragments.notifications.NotificacoesSistemasFragment;
import com.senai.get_in.fragments.notifications.NotificacoesTodasFragment;

public class NotificacoesAdapter extends FragmentStateAdapter {

    // FragmentManager: gerencia os fragments dentro do ViewPager
    // Lifecycle: controla quando os fragments são criados/destruídos
    public NotificacoesAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    // Retorna quantas abas existem
    @Override
    public int getItemCount() {
        return 4; // Pendentes e Lidas
    }

    // Retorna qual Fragment corresponde a cada posição (0, 1, 2...)
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NotificacoesTodasFragment();
            case 1:
                return new NotificacoesAprovacoesFragment();
            case 2:
                return new NotificacoesAlertasFragment();
            case 3:
                return new NotificacoesSistemasFragment();
            default:
                return new NotificacoesTodasFragment();
        }
    }
}

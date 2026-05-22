package com.senai.get_in.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.senai.get_in.fragments.notifications.NotificacoesAlertasFragment;
import com.senai.get_in.fragments.notifications.NotificacoesSistemasFragment;
import com.senai.get_in.fragments.notifications.NotificacoesTodasFragment;

public class NotificacoesAdapter extends FragmentStateAdapter {

    public NotificacoesAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NotificacoesTodasFragment();
            case 1:
                return new NotificacoesAlertasFragment();
            case 2:
                return new NotificacoesSistemasFragment();
            default:
                return new NotificacoesTodasFragment();
        }
    }
}

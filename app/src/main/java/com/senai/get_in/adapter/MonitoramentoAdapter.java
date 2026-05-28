package com.senai.get_in.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.senai.get_in.AutorizacaoFragment;
import com.senai.get_in.EquipeFragment;
import com.senai.get_in.HistoricoFragment;

public class MonitoramentoAdapter extends FragmentStateAdapter {

    public MonitoramentoAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AutorizacaoFragment();
            case 1: return new HistoricoFragment();
            case 2: return new EquipeFragment();
            default: return new AutorizacaoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

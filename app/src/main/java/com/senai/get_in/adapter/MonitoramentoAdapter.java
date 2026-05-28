package com.senai.get_in.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.senai.get_in.AutorizacaoFragment;
import com.senai.get_in.EquipeFragment;
import com.senai.get_in.HistoricoFragment;
import com.senai.get_in.model.UsuarioDetalhado;

import java.util.ArrayList;
import java.util.List;

public class MonitoramentoAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();

    public MonitoramentoAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        
        fragments.add(new AutorizacaoFragment());
        titles.add("Pendentes");
        
        fragments.add(new HistoricoFragment());
        titles.add("Histórico");
        
        fragments.add(new EquipeFragment());
        titles.add("Equipe");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
    
    public String getTitle(int position) {
        return titles.get(position);
    }
}

package com.senai.get_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.R;
import java.util.ArrayList;

public class AutorizacaoFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_autorizacao, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerRequisicao);
        progressBar = view.findViewById(R.id.progressBarAutorizacao);
        this.container = view.findViewById(R.id.containerAutorizacao);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (container != null) {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            container.startAnimation(slideUp);
        }

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            // Adicionando animação de layout
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
            
            // Aqui viria a lógica de carregamento, similar aos outros fragments
        }
    }
}
package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.adapter.VisitanteAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.VisitanteLocal;
import com.senai.get_in.model.VisitanteLocalResponse;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitantesFragment extends Fragment {

    private static final String TAG = "VisitantesFragment";
    private RecyclerView recycler;
    private VisitanteAdapter adapter;
    private ProgressBar progressBar;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitantes, container, false);

        recycler = view.findViewById(R.id.recyclerVisitantes);
        progressBar = view.findViewById(R.id.progressBarVisitantes);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new VisitanteAdapter(new ArrayList<>());
            recycler.setAdapter(adapter);
            
            // Adiciona animação de entrada na lista
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
            recycler.setLayoutAnimation(animation);
        }

        tokenManager = new TokenManager(requireContext());
        carregarVisitantes();
    }

    private void carregarVisitantes() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (recycler != null) recycler.setVisibility(View.INVISIBLE);
        }

        RetrofitClient.getApiService(requireContext()).getVisitantesLocal().enqueue(new Callback<VisitanteLocalResponse>() {
            @Override
            public void onResponse(Call<VisitanteLocalResponse> call, Response<VisitanteLocalResponse> response) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recycler != null) recycler.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<VisitanteLocal> lista = response.body().getDados();
                    if (lista != null && adapter != null) {
                        adapter.updateList(lista);
                        recycler.scheduleLayoutAnimation();
                    }
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar visitantes");
                }
            }

            @Override
            public void onFailure(Call<VisitanteLocalResponse> call, Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recycler != null) recycler.setVisibility(View.VISIBLE);
                Log.e(TAG, "Erro: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }
}

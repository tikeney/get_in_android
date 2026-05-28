package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.senai.get_in.adapter.VisitanteAdapter;
import com.senai.get_in.api.VisitanteRepository;
import com.senai.get_in.databinding.FragmentVisitantesBinding;
import com.senai.get_in.model.VisitanteLocal;
import com.senai.get_in.model.VisitanteLocalResponse;
import com.senai.get_in.utils.NetworkUtils;
import com.senai.get_in.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitantesFragment extends Fragment {

    private static final String TAG = "VisitantesFragment";
    private FragmentVisitantesBinding binding;
    private VisitanteAdapter adapter;
    private VisitanteRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVisitantesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repository = new VisitanteRepository(requireContext());
        setupRecycler();
        setupSwipeRefresh();
        carregarVisitantes(false);
    }

    private void setupSwipeRefresh() {
        binding.swipeVisitantes.setOnRefreshListener(() -> carregarVisitantes(true));
        binding.swipeVisitantes.setColorSchemeResources(R.color.primary);
    }

    private void setupRecycler() {
        binding.recyclerVisitantes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VisitanteAdapter(new ArrayList<>());
        binding.recyclerVisitantes.setAdapter(adapter);
        
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        binding.recyclerVisitantes.setLayoutAnimation(animation);
    }

    private void carregarVisitantes(boolean isRefreshing) {
        if (!NetworkUtils.isOnline(getContext())) {
            binding.swipeVisitantes.setRefreshing(false);
            ToastUtils.showError(getContext(), "Sem conexão com a internet.");
            if (adapter.getItemCount() == 0) {
                binding.layoutVazioVisitantes.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (!isRefreshing) setLoading(true);
        binding.layoutVazioVisitantes.setVisibility(View.GONE);

        repository.getVisitantesNoLocal(new Callback<VisitanteLocalResponse>() {
            @Override
            public void onResponse(Call<VisitanteLocalResponse> call, Response<VisitanteLocalResponse> response) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeVisitantes.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<VisitanteLocal> lista = response.body().getDados();
                    if (lista != null && !lista.isEmpty()) {
                        adapter.updateList(lista);
                        binding.recyclerVisitantes.setVisibility(View.VISIBLE);
                        binding.recyclerVisitantes.scheduleLayoutAnimation();
                        binding.layoutVazioVisitantes.setVisibility(View.GONE);
                    } else {
                        binding.recyclerVisitantes.setVisibility(View.GONE);
                        binding.layoutVazioVisitantes.setVisibility(View.VISIBLE);
                    }
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar visitantes");
                }
            }

            @Override
            public void onFailure(Call<VisitanteLocalResponse> call, Throwable t) {
                if (!isAdded() || binding == null) return;
                setLoading(false);
                binding.swipeVisitantes.setRefreshing(false);
                Log.e(TAG, "Erro: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.shimmerVisitantes.setVisibility(View.VISIBLE);
            binding.shimmerVisitantes.startShimmer();
            binding.recyclerVisitantes.setVisibility(View.GONE);
        } else {
            binding.shimmerVisitantes.stopShimmer();
            binding.shimmerVisitantes.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

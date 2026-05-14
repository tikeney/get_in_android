package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.senai.get_in.adapter.VisitanteAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.VisitanteLocal;
import com.senai.get_in.model.VisitanteLocalResponse;
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
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitantes, container, false);

        recycler = view.findViewById(R.id.recyclerVisitantes);
        if (recycler == null) {
            // Se o layout não tiver o ID, vamos criar um dinamicamente ou ajustar o layout
            // Mas assumindo que vamos ajustar o layout fragment_visitantes.xml
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recycler = view.findViewById(R.id.recyclerVisitantes);
        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new VisitanteAdapter(new ArrayList<>());
            recycler.setAdapter(adapter);
        }

        tokenManager = new TokenManager(requireContext());
        carregarVisitantes();
    }

    private void carregarVisitantes() {
        String token = tokenManager.getToken();
        if (token == null) return;

        RetrofitClient.getApiService().getVisitantesLocal("Bearer " + token).enqueue(new Callback<VisitanteLocalResponse>() {
            @Override
            public void onResponse(Call<VisitanteLocalResponse> call, Response<VisitanteLocalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VisitanteLocal> lista = response.body().getDados();
                    if (lista != null && adapter != null) {
                        adapter.updateList(lista);
                    }
                }
            }

            @Override
            public void onFailure(Call<VisitanteLocalResponse> call, Throwable t) {
                Log.e(TAG, "Erro: " + t.getMessage());
            }
        });
    }
}

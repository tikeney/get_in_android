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

import com.senai.get_in.adapter.LogAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment {

    private static final String TAG = "HistoricoFragment";
    private RecyclerView recycler;
    private LogAdapter adapter;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        recycler = view.findViewById(R.id.recyclerHistorico);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new LogAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        tokenManager = new TokenManager(requireContext());
        
        carregarLogs();

        return view;
    }

    private void carregarLogs() {
        String token = tokenManager.getToken();
        if (token == null) return;

        RetrofitClient.getApiService().getLogs("Bearer " + token).enqueue(new Callback<LogResponse>() {
            @Override
            public void onResponse(Call<LogResponse> call, Response<LogResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LogAcesso> logs = response.body().getData();
                    if (logs != null) {
                        adapter.updateList(logs);
                    }
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogResponse> call, Throwable t) {
                Log.e(TAG, "Falha: " + t.getMessage());
                Toast.makeText(getContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

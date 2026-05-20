package com.senai.get_in;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.senai.get_in.adapter.LogAdapter;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment {

    private static final String TAG = "HistoricoFragment";
    private RecyclerView recyclerHistorico;
    private LogAdapter adapter;
    private List<LogAcesso> listaCompleta = new ArrayList<>();
    private List<LogAcesso> listaFiltrada = new ArrayList<>();
    private TextInputEditText etBusca;
    private ChipGroup chipGroupFiltros;
    private LinearLayout layoutVazio;
    private TextView tvContadorHoje, tvContadorNaFabrica, tvContadorNegados;
    private ProgressBar progressBar;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupSearch();
        
        tokenManager = new TokenManager(requireContext());
        loadData();

        return view;
    }

    private void initViews(View view) {
        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        etBusca = view.findViewById(R.id.etBusca);
        chipGroupFiltros = view.findViewById(R.id.chipGroupFiltros);
        layoutVazio = view.findViewById(R.id.layoutVazio);
        tvContadorHoje = view.findViewById(R.id.tvContadorHoje);
        tvContadorNaFabrica = view.findViewById(R.id.tvContadorNaFabrica);
        tvContadorNegados = view.findViewById(R.id.tvContadorNegados);
        progressBar = view.findViewById(R.id.progressBarHistorico);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerHistorico.setVisibility(View.GONE);
        layoutVazio.setVisibility(View.GONE);

        RetrofitClient.getApiService(requireContext()).getLogs().enqueue(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body().getData();
                    if (listaCompleta == null) listaCompleta = new ArrayList<>();
                    
                    updateCounters();
                    filterList();
                    
                    recyclerHistorico.setVisibility(View.VISIBLE);
                    recyclerHistorico.scheduleLayoutAnimation();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao carregar histórico");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Erro: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter(new ArrayList<>());
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHistorico.setAdapter(adapter);
        
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerHistorico.setLayoutAnimation(animation);
    }

    private void setupFilters() {
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterList();
        });
    }

    private void setupSearch() {
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList() {
        if (listaCompleta == null) return;
        
        String query = etBusca.getText().toString().toLowerCase().trim();
        int checkedChipId = chipGroupFiltros.getCheckedChipId();

        listaFiltrada = listaCompleta.stream()
                .filter(l -> (l.getUsuarioNome() != null && l.getUsuarioNome().toLowerCase().contains(query)) || 
                             (l.getDepartamentoUsuario() != null && l.getDepartamentoUsuario().toLowerCase().contains(query)))
                .filter(l -> {
                    boolean isNaFabrica = l.getDataSaida() == null || l.getDataSaida().isEmpty();
                    if (checkedChipId == R.id.chipNaFabrica) {
                        return isNaFabrica;
                    } else if (checkedChipId == R.id.chipSaiu) {
                        return !isNaFabrica;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        adapter.updateList(listaFiltrada);

        if (listaFiltrada.isEmpty()) {
            layoutVazio.setVisibility(View.VISIBLE);
            recyclerHistorico.setVisibility(View.GONE);
        } else {
            layoutVazio.setVisibility(View.GONE);
            recyclerHistorico.setVisibility(View.VISIBLE);
        }
    }

    private void updateCounters() {
        if (listaCompleta == null) return;

        int total = listaCompleta.size();
        long naFabrica = listaCompleta.stream()
                .filter(l -> l.getDataSaida() == null || l.getDataSaida().isEmpty())
                .count();

        tvContadorHoje.setText(String.valueOf(total));
        tvContadorNaFabrica.setText(String.valueOf(naFabrica));
        // Nota: O contador de negados não existe diretamente nos Logs de Acesso da View
        tvContadorNegados.setText("0"); 
    }
}
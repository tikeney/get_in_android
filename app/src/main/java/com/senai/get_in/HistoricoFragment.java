package com.senai.get_in;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.senai.get_in.adapter.HistoricoAdapter;
import com.senai.get_in.model.Historico;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricoFragment extends Fragment {

    private RecyclerView recyclerHistorico;
    private HistoricoAdapter adapter;
    private List<Historico> listaCompleta;
    private List<Historico> listaFiltrada;
    private TextInputEditText etBusca;
    private ChipGroup chipGroupFiltros;
    private LinearLayout layoutVazio;
    private TextView tvContadorHoje, tvContadorNaFabrica, tvContadorNegados;
    private ProgressBar progressBar;

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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        recyclerHistorico.setVisibility(View.GONE);
        layoutVazio.setVisibility(View.GONE);

        // Simulando carregamento assíncrono para UX
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            
            setupData();
            updateCounters();
            filterList();
            
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            recyclerHistorico.setVisibility(View.VISIBLE);
            recyclerHistorico.scheduleLayoutAnimation();
        }, 600);
    }

    private void setupData() {
        listaCompleta = new ArrayList<>();
        listaCompleta.add(new Historico("João Silva", "Tech Corp", "TI", "Na fábrica", "08:30", "2h 15min", R.mipmap.ic_launcher_round));
        listaCompleta.add(new Historico("Maria Santos", "Limpeza Ltda", "Serviços", "Saiu", "09:15", "1h 00min", R.mipmap.ic_launcher_round));
        listaCompleta.add(new Historico("Ricardo Oliveira", "Logística S.A", "Expedição", "Negado", "10:00", "-", R.mipmap.ic_launcher_round));
        listaCompleta.add(new Historico("Ana Souza", "Senai", "Educação", "Saiu", "10:30", "45min", R.mipmap.ic_launcher_round));
        listaCompleta.add(new Historico("Carlos Lima", "Pintura & Cia", "Manutenção", "Na fábrica", "11:00", "15min", R.mipmap.ic_launcher_round));
        listaCompleta.add(new Historico("Fernanda Costa", "Alimentos S.A", "RH", "Na fábrica", "14:00", "1h 30min", R.mipmap.ic_launcher_round));

        listaFiltrada = new ArrayList<>(listaCompleta);
    }

    private void setupRecyclerView() {
        adapter = new HistoricoAdapter(new ArrayList<>());
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
                .filter(h -> (h.getNome().toLowerCase().contains(query) || 
                             h.getSetor().toLowerCase().contains(query) || 
                             h.getEmpresa().toLowerCase().contains(query)))
                .filter(h -> {
                    if (checkedChipId == R.id.chipPermitidos) {
                        return h.getStatus().equalsIgnoreCase("Na fábrica") || h.getStatus().equalsIgnoreCase("Saiu") || h.getStatus().equalsIgnoreCase("Permitido");
                    } else if (checkedChipId == R.id.chipNegado) {
                        return h.getStatus().equalsIgnoreCase("Negado");
                    } else if (checkedChipId == R.id.chipSaiu) {
                        return h.getStatus().equalsIgnoreCase("Saiu");
                    } else if (checkedChipId == R.id.chipNaFabrica) {
                        return h.getStatus().equalsIgnoreCase("Na fábrica");
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

        int hoje = listaCompleta.size();
        long naFabrica = listaCompleta.stream().filter(h -> h.getStatus().equalsIgnoreCase("Na fábrica")).count();
        long negados = listaCompleta.stream().filter(h -> h.getStatus().equalsIgnoreCase("Negado")).count();

        tvContadorHoje.setText(String.valueOf(hoje));
        tvContadorNaFabrica.setText(String.valueOf(naFabrica));
        tvContadorNegados.setText(String.valueOf(negados));
    }
}

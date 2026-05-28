package com.senai.get_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.senai.get_in.adapter.EquipeAdapter;
import com.senai.get_in.api.LogRepository;
import com.senai.get_in.api.UsuarioRepository;
import com.senai.get_in.databinding.FragmentEquipeBinding;
import com.senai.get_in.model.LogAcesso;
import com.senai.get_in.model.LogResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioResponse;
import com.senai.get_in.utils.AccessManager;
import com.senai.get_in.utils.SearchableFragment;
import com.senai.get_in.utils.TokenManager;
import com.senai.get_in.utils.ToastUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipeFragment extends Fragment implements EquipeAdapter.OnItemClickListener, SearchableFragment {

    private FragmentEquipeBinding binding;
    private EquipeAdapter adapter;
    private UsuarioRepository repository;
    private LogRepository logRepository;
    private List<UsuarioDetalhado> listaCompleta = new ArrayList<>();
    private List<LogAcesso> logsHoje = new ArrayList<>();
    private UsuarioDetalhado currentUser;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEquipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        TokenManager tokenManager = new TokenManager(requireContext());
        currentUser = tokenManager.getUserData();
        repository = new UsuarioRepository(requireContext());
        logRepository = new LogRepository(requireContext());
        
        setupRecyclerView();
        setupSwipeRefresh();
        loadData(false);
    }

    private void setupRecyclerView() {
        adapter = new EquipeAdapter(new ArrayList<>(), this);
        binding.recyclerEquipe.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEquipe.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeEquipe.setOnRefreshListener(() -> loadData(true));
    }

    private void loadData(boolean isRefreshing) {
        if (!isRefreshing) setLoading(true);
        
        logRepository.getLogs(new Callback<LogResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogResponse> call, @NonNull Response<LogResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logsHoje = response.body().getData();
                }
                carregarUsuarios();
            }

            @Override
            public void onFailure(@NonNull Call<LogResponse> call, @NonNull Throwable t) {
                carregarUsuarios();
            }
        });
    }

    private void carregarUsuarios() {
        repository.getUsuarios(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (isAdded() && binding != null) {
                    setLoading(false);
                    binding.swipeEquipe.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        listaCompleta = response.body().getData();
                        filterList();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                if (isAdded() && binding != null) {
                    setLoading(false);
                    binding.swipeEquipe.setRefreshing(false);
                    ToastUtils.showError(getContext(), "Erro ao carregar equipe");
                }
            }
        });
    }

    @Override
    public void onSearch(String query) {
        this.currentQuery = query.toLowerCase().trim();
        filterList();
    }

    private void filterList() {
        if (listaCompleta == null) return;

        final String meuSetor = currentUser != null ? currentUser.getDepartamentoNome() : null;
        final boolean isAdmin = AccessManager.isAdmin(currentUser);

        List<UsuarioDetalhado> filtrada = listaCompleta.stream()
                .filter(u -> isAdmin || meuSetor == null || (u.getDepartamentoNome() != null && u.getDepartamentoNome().equalsIgnoreCase(meuSetor)))
                .filter(u -> u.getNome().toLowerCase().contains(currentQuery) || (u.getCargo() != null && u.getCargo().toLowerCase().contains(currentQuery)))
                .collect(Collectors.toList());

        adapter.updateList(filtrada, logsHoje);
        binding.layoutVazioEquipe.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.shimmerEquipe.setVisibility(View.VISIBLE);
            binding.shimmerEquipe.startShimmer();
            binding.recyclerEquipe.setVisibility(View.GONE);
        } else {
            binding.shimmerEquipe.stopShimmer();
            binding.shimmerEquipe.setVisibility(View.GONE);
            binding.recyclerEquipe.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(UsuarioDetalhado usuario) {
        Bundle bundle = new Bundle();
        bundle.putInt("usuario_id", usuario.getId());
        Navigation.findNavController(requireView()).navigate(R.id.nav_usuario_detalhado, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

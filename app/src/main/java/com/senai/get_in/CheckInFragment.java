package com.senai.get_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckInFragment extends Fragment {

    private static final String TAG = "CheckInFragment";
    private TextInputEditText etNome, etCPF, etTelefone, etEmpresa, etMotivo;
    private ChipGroup chipGroupSetores;
    private Button btnFinalizar;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        etNome = view.findViewById(R.id.etNome);
        etCPF = view.findViewById(R.id.etCPF);
        etTelefone = view.findViewById(R.id.etTelefone);
        etEmpresa = view.findViewById(R.id.etEmpresa);
        etMotivo = view.findViewById(R.id.etMotivo);
        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        btnFinalizar = view.findViewById(R.id.btnLogin); // O ID no XML é btnLogin para finalizar

        tokenManager = new TokenManager(requireContext());

        btnFinalizar.setOnClickListener(v -> realizarCheckIn());

        return view;
    }

    private void realizarCheckIn() {
        String nome = etNome.getText().toString().trim();
        String cpf = etCPF.getText().toString().trim();
        String empresa = etEmpresa.getText().toString().trim();
        String motivo = etMotivo.getText().toString().trim();

        if (nome.isEmpty() || cpf.isEmpty()) {
            Toast.makeText(getContext(), "Nome e CPF são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedChipId = chipGroupSetores.getCheckedChipId();
        String setor = "";
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupSetores.findViewById(selectedChipId);
            setor = selectedChip.getText().toString();
        }

        // Criar objeto de requisição para o visitante
        Requisicao req = new Requisicao();
        req.setUsuarioNome(nome);
        req.setUsuarioCpf(cpf);
        req.setEmpresaVisitante(empresa);
        req.setMotivo(motivo);
        req.setDepartamentoNome(setor);
        // Note: A API pode exigir IDs de usuário/setor reais, mas aqui simulamos com os dados do form
        
        String token = tokenManager.getToken();
        if (token == null) return;

        btnFinalizar.setEnabled(false);
        btnFinalizar.setText("Processando...");

        RetrofitClient.getApiService().criarRequisicaoVisitante("Bearer " + token, req).enqueue(new Callback<Requisicao>() {
            @Override
            public void onResponse(Call<Requisicao> call, Response<Requisicao> response) {
                btnFinalizar.setEnabled(true);
                btnFinalizar.setText("Finalizar Check-in");
                
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Check-in realizado com sucesso!", Toast.LENGTH_LONG).show();
                    limparCampos();
                } else {
                    Toast.makeText(getContext(), "Erro ao realizar check-in: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Requisicao> call, Throwable t) {
                btnFinalizar.setEnabled(true);
                btnFinalizar.setText("Finalizar Check-in");
                Log.e(TAG, "Falha: " + t.getMessage());
                Toast.makeText(getContext(), "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limparCampos() {
        etNome.setText("");
        etCPF.setText("");
        etTelefone.setText("");
        etEmpresa.setText("");
        etMotivo.setText("");
        chipGroupSetores.clearCheck();
    }
}

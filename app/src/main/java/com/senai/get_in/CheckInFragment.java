package com.senai.get_in;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.senai.get_in.api.RequisicaoRepository;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.databinding.FragmentCheckInBinding;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.Setor;
import com.senai.get_in.utils.MascaraUtils;
import com.senai.get_in.utils.NetworkUtils;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckInFragment extends Fragment implements MainActivity.NfcTagListener {

    private static final String TAG = "CheckInFragment";
    private FragmentCheckInBinding binding;
    private RequisicaoRepository repository;

    // Utilitários e Estado
    private TokenManager tokenManager;
    private Uri fotoUri = null;
    private String codigoTagVinculada = null;

    // Launchers de resultado
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<String> permissaoCameraLauncher;
    private ActivityResultLauncher<String> permissaoArmazenamentoLauncher;

    private Uri cameraImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        repository = new RequisicaoRepository(requireContext());

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && cameraImageUri != null) {
                definirFoto(cameraImageUri);
            }
        });

        galeriaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    definirFoto(uri);
                }
            }
        });

        permissaoCameraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) abrirCamera();
            else ToastUtils.showInfo(getContext(), "Permissão de câmera negada.");
        });

        permissaoArmazenamentoLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) abrirGaleria();
            else ToastUtils.showInfo(getContext(), "Permissão de armazenamento negada.");
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        binding.containerCheckIn.startAnimation(slideUp);

        configurarMascaras();
        configurarFoto();
        configurarCracha();
        configurarBotaoFinalizar();

        carregarSetores();
    }

    @Override
    public void onTagRead(String tagId) {
        if (binding == null) return;
        codigoTagVinculada = tagId;
        binding.tvTituloCracha.setText("Crachá Vinculado");
        binding.tvSubtituloCracha.setText("Código: " + tagId);
        ToastUtils.showSuccess(getContext(), "Crachá lido com sucesso!");
    }

    private void carregarSetores() {
        if (!NetworkUtils.isOnline(getContext())) {
            return;
        }

        RetrofitClient.getApiService(requireContext()).getSetores().enqueue(new Callback<List<Setor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Setor>> call, @NonNull Response<List<Setor>> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    preencherChipsSetores(response.body());
                } else {
                    Log.e(TAG, "Erro ao carregar setores: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Setor>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Falha ao carregar setores: " + t.getMessage());
            }
        });
    }

    private void preencherChipsSetores(List<Setor> setores) {
        if (binding == null) return;

        binding.chipGroupSetores.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Setor setor : setores) {
            Chip chip = (Chip) inflater.inflate(R.layout.layout_chip_filtro, binding.chipGroupSetores, false);
            chip.setText(setor.getNome());
            chip.setTag(setor.getId());
            chip.setCheckable(true);
            binding.chipGroupSetores.addView(chip);
        }
    }

    private void configurarCracha() {
        binding.btnAdicionarCracha.setOnClickListener(v ->
                ToastUtils.showInfo(getContext(), "Aproxime o crachá para vincular"));
    }

    private void setProgressBar(boolean loading) {
        if (binding == null) return;
        if (loading) {
            binding.btnFinalizarCheckIn.setEnabled(false);
            binding.btnFinalizarCheckIn.setAlpha(0.5f);
            binding.progressBarCheckIn.setVisibility(View.VISIBLE);
        } else {
            binding.btnFinalizarCheckIn.setEnabled(true);
            binding.btnFinalizarCheckIn.setAlpha(1.0f);
            binding.progressBarCheckIn.setVisibility(View.GONE);
        }
    }

    private void realizarCheckIn() {
        if (!NetworkUtils.isOnline(getContext())) {
            ToastUtils.showError(getContext(), "Sem conexão para realizar o check-in.");
            return;
        }

        if (binding == null) return;
        String nome = binding.etNome.getText().toString().trim();
        String cpf = binding.etCPF.getText().toString().trim();
        String empresa = binding.etEmpresa.getText().toString().trim();
        String motivo = binding.etMotivo.getText().toString().trim();

        Integer idSetor = null;
        int selectedChipId = binding.chipGroupSetores.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = binding.chipGroupSetores.findViewById(selectedChipId);
            if (selectedChip != null && selectedChip.getTag() instanceof Integer) {
                idSetor = (Integer) selectedChip.getTag();
            }
        }

        Requisicao req = new Requisicao();
        req.setUsuarioNome(nome);
        req.setUsuarioCpf(cpf);
        req.setEmpresaVisitante(empresa);
        req.setMotivo(motivo);
        req.setIdSetor(idSetor);
        req.setCodigoTag(codigoTagVinculada);

        setProgressBar(true);

        repository.criarRequisicaoVisitante(req, new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded() || binding == null) return;
                setProgressBar(false);
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Check-in realizado com sucesso!");
                    limparFormulario();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao realizar check-in: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                setProgressBar(false);
                Log.e(TAG, "Falha: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    private boolean validarFormulario() {
        if (binding == null) return false;
        boolean valido = true;

        if (binding.etNome.getText().toString().trim().isEmpty()) {
            binding.inputNome.setError("Informe o nome completo");
            valido = false;
        } else {
            binding.inputNome.setError(null);
        }

        String cpfLimpo = binding.etCPF.getText().toString().replaceAll("[^\\d]", "");
        if (cpfLimpo.length() < 11) {
            binding.inputCPF.setError("CPF incompleto");
            valido = false;
        } else {
            binding.inputCPF.setError(null);
        }

        if (binding.etEmpresa.getText().toString().trim().isEmpty()) {
            binding.inputEmpresa.setError("Informe a empresa");
            valido = false;
        } else {
            binding.inputEmpresa.setError(null);
        }

        if (binding.chipGroupSetores.getCheckedChipId() == View.NO_ID) {
            ToastUtils.showInfo(getContext(), "Selecione um setor.");
            valido = false;
        }

        if (codigoTagVinculada == null) {
            ToastUtils.showInfo(getContext(), "É obrigatório vincular um crachá!");
            valido = false;
        }

        return valido;
    }

    private void configurarMascaras() {
        binding.etCPF.addTextChangedListener(MascaraUtils.aplicar(binding.etCPF, "###.###.###-##"));
        binding.etTelefone.addTextChangedListener(MascaraUtils.aplicar(binding.etTelefone, "(##) #####-####"));
    }

    private void configurarFoto() {
        binding.btnAdicionarFoto.setOnClickListener(v -> mostrarOpcoesFoto());
    }

    private void mostrarOpcoesFoto() {
        String[] opcoes = {"Tirar foto", "Escolher da galeria"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Adicionar foto")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) verificarPermissaoCamera();
                    else verificarPermissaoGaleria();
                })
                .show();
    }

    private void verificarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
        } else {
            permissaoCameraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void verificarPermissaoGaleria() {
        String permissao = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), permissao)
                == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            permissaoArmazenamentoLauncher.launch(permissao);
        }
    }

    private void abrirCamera() {
        try {
            File fotoFile = criarArquivoTemporario();
            cameraImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    fotoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            ToastUtils.showError(getContext(), "Erro ao criar arquivo de foto.");
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private File criarArquivoTemporario() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return File.createTempFile("FOTO_" + timestamp, ".jpg", requireContext().getExternalFilesDir(null));
    }

    private void definirFoto(Uri uri) {
        if (binding == null) return;
        fotoUri = uri;
        binding.ivIconePerfil.clearColorFilter();
        binding.ivIconePerfil.setImageURI(uri);
        binding.ivIconePerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
        binding.tvTituloFoto.setText("Foto adicionada");
        binding.tvSubtituloFoto.setText("Toque para trocar");
    }

    private void configurarBotaoFinalizar() {
        binding.btnFinalizarCheckIn.setOnClickListener(v -> {
            if (validarFormulario()) realizarCheckIn();
        });
    }

    private void limparFormulario() {
        if (binding == null) return;
        binding.etNome.setText("");
        binding.etCPF.setText("");
        binding.etTelefone.setText("");
        binding.etEmpresa.setText("");
        binding.etMotivo.setText("");
        binding.chipGroupSetores.clearCheck();
        fotoUri = null;
        binding.ivIconePerfil.setImageResource(R.drawable.outline_person_24);
        binding.ivIconePerfil.setScaleType(ImageView.ScaleType.FIT_CENTER);
        binding.ivIconePerfil.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tvTituloFoto.setText("Foto do Visitante");
        binding.tvSubtituloFoto.setText("Clique para capturar ou selecionar");
        codigoTagVinculada = null;
        binding.tvTituloCracha.setText("Vincular Crachá");
        binding.tvSubtituloCracha.setText("Aproxime o crachá do leitor RFID");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.senai.get_in;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.Setor;
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

    // Views
    private ConstraintLayout btnAdicionarFoto;
    private ImageView ivIconePerfil;
    private TextView tvTituloFoto, tvSubtituloFoto;

    private TextInputEditText etNome, etCPF, etTelefone, etEmpresa, etMotivo;
    private TextInputLayout inputNome, inputCPF, inputTelefone, inputEmpresa, inputMotivo;

    private ConstraintLayout btnAdicionarCracha;
    private TextView tvTituloCracha, tvSubtituloCracha;

    private ChipGroup chipGroupSetores;
    private Button btnFinalizar;
    private ProgressBar progressBar;
    private View containerCheckIn;

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
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);
        inicializarViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Aplica animação de entrada
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        containerCheckIn.startAnimation(slideUp);
        
        configurarMascaras();
        configurarFoto();
        configurarCracha();
        configurarBotaoFinalizar();
        
        carregarSetores();
    }

    @Override
    public void onTagRead(String tagId) {
        codigoTagVinculada = tagId;
        tvTituloCracha.setText("Crachá Vinculado");
        tvSubtituloCracha.setText("Código: " + tagId);
        ToastUtils.showSuccess(getContext(), "Crachá lido com sucesso!");
    }

    private void inicializarViews(View view) {
        containerCheckIn = view.findViewById(R.id.containerCheckIn);
        btnAdicionarFoto = view.findViewById(R.id.btnAdicionarFoto);
        ivIconePerfil = view.findViewById(R.id.iv_icone_perfil);
        tvTituloFoto = view.findViewById(R.id.tv_titulo_foto);
        tvSubtituloFoto = view.findViewById(R.id.tv_subtitulo_foto);

        inputNome = view.findViewById(R.id.inputNome);
        inputCPF = view.findViewById(R.id.inputCPF);
        inputTelefone = view.findViewById(R.id.inputTelefone);
        inputEmpresa = view.findViewById(R.id.inputEmpresa);
        inputMotivo = view.findViewById(R.id.inputMotivo);

        etNome = view.findViewById(R.id.etNome);
        etCPF = view.findViewById(R.id.etCPF);
        etTelefone = view.findViewById(R.id.etTelefone);
        etEmpresa = view.findViewById(R.id.etEmpresa);
        etMotivo = view.findViewById(R.id.etMotivo);

        btnAdicionarCracha = view.findViewById(R.id.btnAdicionarCracha);
        tvTituloCracha = view.findViewById(R.id.tv_titulo_cracha);
        tvSubtituloCracha = view.findViewById(R.id.tv_subtitulo_cracha);

        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        btnFinalizar = view.findViewById(R.id.btnFinalizarCheckIn);
        progressBar = view.findViewById(R.id.progressBarCheckIn);
    }

    private void carregarSetores() {
        RetrofitClient.getApiService(requireContext()).getSetores().enqueue(new Callback<List<Setor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Setor>> call, @NonNull Response<List<Setor>> response) {
                if (!isAdded()) return;
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
        if (chipGroupSetores == null) return;
        chipGroupSetores.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Setor setor : setores) {
            Chip chip = (Chip) inflater.inflate(R.layout.layout_chip_filtro, chipGroupSetores, false);
            chip.setText(setor.getNome());
            chip.setTag(setor.getId());
            chip.setCheckable(true);
            chipGroupSetores.addView(chip);
        }
    }

    private void configurarCracha() {
        btnAdicionarCracha.setOnClickListener(v -> {
            ToastUtils.showInfo(getContext(), "Aproxime o crachá para vincular");
        });
    }

    private void setProgressBar(boolean loading) {
        if (loading) {
            btnFinalizar.setEnabled(false);
            btnFinalizar.setAlpha(0.5f);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnFinalizar.setEnabled(true);
            btnFinalizar.setAlpha(1.0f);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void realizarCheckIn() {
        String nome = etNome.getText().toString().trim();
        String cpf = etCPF.getText().toString().trim();
        String empresa = etEmpresa.getText().toString().trim();
        String motivo = etMotivo.getText().toString().trim();

        int selectedChipId = chipGroupSetores.getCheckedChipId();
        Integer idSetor = null;
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupSetores.findViewById(selectedChipId);
            idSetor = (Integer) selectedChip.getTag();
        }

        Requisicao req = new Requisicao();
        req.setUsuarioNome(nome);
        req.setUsuarioCpf(cpf);
        req.setEmpresaVisitante(empresa);
        req.setMotivo(motivo);
        req.setIdSetor(idSetor);
        req.setCodigoTag(codigoTagVinculada);
        
        setProgressBar(true);

        RetrofitClient.getApiService(requireContext()).criarRequisicaoVisitante(req).enqueue(new Callback<Requisicao>() {
            @Override
            public void onResponse(@NonNull Call<Requisicao> call, @NonNull Response<Requisicao> response) {
                if (!isAdded()) return;
                setProgressBar(false);
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(getContext(), "Check-in realizado com sucesso!");
                    limparFormulario();
                } else {
                    ToastUtils.showError(getContext(), "Erro ao realizar check-in: " + response.code());
                    Log.e(TAG, "Erro response: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisicao> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setProgressBar(false);
                Log.e(TAG, "Falha: " + t.getMessage());
                ToastUtils.showError(getContext(), "Falha na conexão");
            }
        });
    }

    private boolean validarFormulario() {
        boolean valido = true;

        if (etNome.getText().toString().trim().isEmpty()) {
            inputNome.setError("Informe o nome completo");
            valido = false;
        } else inputNome.setError(null);

        String cpfLimpo = etCPF.getText().toString().replaceAll("[^\\d]", "");
        if (cpfLimpo.length() < 11) { 
            inputCPF.setError("CPF incompleto");
            valido = false;
        } else inputCPF.setError(null);

        if (etEmpresa.getText().toString().trim().isEmpty()) {
            inputEmpresa.setError("Informe a empresa");
            valido = false;
        } else inputEmpresa.setError(null);

        if (chipGroupSetores.getCheckedChipId() == View.NO_ID) {
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
        etCPF.addTextChangedListener(new MascaraTextWatcher(etCPF, "###.###.###-##"));
        etTelefone.addTextChangedListener(new MascaraTextWatcher(etTelefone, "(##) #####-####"));
    }

    private void configurarFoto() {
        btnAdicionarFoto.setOnClickListener(v -> mostrarOpcoesFoto());
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamera();
        else permissaoCameraLauncher.launch(Manifest.permission.CAMERA);
    }

    private void verificarPermissaoGaleria() {
        String permissao = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), permissao) == PackageManager.PERMISSION_GRANTED) abrirGaleria();
        else permissaoArmazenamentoLauncher.launch(permissao);
    }

    private void abrirCamera() {
        try {
            File fotoFile = criarArquivoTemporario();
            cameraImageUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", fotoFile);
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
        fotoUri = uri;
        ivIconePerfil.setImageURI(uri);
        ivIconePerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
        tvTituloFoto.setText("Foto adicionada");
        tvSubtituloFoto.setText("Toque para trocar");
    }

    private void configurarBotaoFinalizar() {
        btnFinalizar.setOnClickListener(v -> {
            if (validarFormulario()) realizarCheckIn();
        });
    }

    private void limparFormulario() {
        etNome.setText("");
        etCPF.setText("");
        etTelefone.setText("");
        etEmpresa.setText("");
        etMotivo.setText("");
        chipGroupSetores.clearCheck();
        fotoUri = null;
        ivIconePerfil.setImageResource(R.drawable.outline_person_24);
        ivIconePerfil.setScaleType(ImageView.ScaleType.FIT_CENTER);
        tvTituloFoto.setText("Adicionar foto do visitante");
        tvSubtituloFoto.setText("Toque para abrir a câmera");
        codigoTagVinculada = null;
        tvTituloCracha.setText("Vincular crachá");
        tvSubtituloCracha.setText("Toque e aproxime o cracha");
    }

    private static class MascaraTextWatcher implements TextWatcher {
        private final TextInputEditText campo;
        private final String mascara;
        private boolean editando = false;
        MascaraTextWatcher(TextInputEditText campo, String mascara) { this.campo = campo; this.mascara = mascara; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (editando) return;
            editando = true;
            String digits = s.toString().replaceAll("[^\\d]", "");
            StringBuilder sb = new StringBuilder();
            int di = 0;
            for (int i = 0; i < mascara.length() && di < digits.length(); i++) {
                char mc = mascara.charAt(i);
                if (mc == '#') sb.append(digits.charAt(di++));
                else sb.append(mc);
            }
            campo.setText(sb.toString());
            if (sb.length() > 0) campo.setSelection(sb.length());
            editando = false;
        }
    }
}
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.senai.get_in.model.Empresa;
import com.senai.get_in.model.EmpresaResponse;
import com.senai.get_in.model.Requisicao;
import com.senai.get_in.model.Setor;
import com.senai.get_in.model.SetorResponse;
import com.senai.get_in.model.VisitanteLocal;
import com.senai.get_in.model.VisitanteLocalResponse;
import com.senai.get_in.utils.MascaraUtils;
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

    private TextInputEditText etNome, etCPF, etTelefone;
    private AutoCompleteTextView etEmpresa, etMotivo, etSetorResponsavel;
    private TextInputLayout inputNome, inputCPF, inputTelefone, inputEmpresa, inputMotivo, inputSetorResponsavel;

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

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        containerCheckIn.startAnimation(slideUp);

        configurarMascaras();
        configurarFoto();
        configurarCracha();
        configurarBotaoFinalizar();
        configurarDropdowns();

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
        inputEmpresa = view.findViewById(R.id.dropdownEmpresa);
        inputMotivo = view.findViewById(R.id.dropdownMotivoDeVisita);

        etNome = view.findViewById(R.id.etNome);
        etCPF = view.findViewById(R.id.etCPF);
        etTelefone = view.findViewById(R.id.etTelefone);
        etEmpresa = view.findViewById(R.id.autoCompleteEmpresa);
        etMotivo = view.findViewById(R.id.autoCompleteMotivoDeVisita);
        etSetorResponsavel = view.findViewById(R.id.autoCompleteSetorResponsavel);

        inputSetorResponsavel = view.findViewById(R.id.dropdownSetorResponsavel);

        btnAdicionarCracha = view.findViewById(R.id.btnAdicionarCracha);
        tvTituloCracha = view.findViewById(R.id.tv_titulo_cracha);
        tvSubtituloCracha = view.findViewById(R.id.tv_subtitulo_cracha);

        chipGroupSetores = view.findViewById(R.id.chipGroupSetores);
        btnFinalizar = view.findViewById(R.id.btnFinalizarCheckIn);
        progressBar = view.findViewById(R.id.progressBarCheckIn);
    }

    private void carregarSetores() {
        RetrofitClient.getApiService(requireContext()).getSetores().enqueue(new Callback<SetorResponse>() {
            @Override
            public void onResponse(@NonNull Call<SetorResponse> call, @NonNull Response<SetorResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Setor> setores = response.body().getData();
                    preencherChipsSetores(setores);
                    preencherDropdownSetores(setores);
                } else {
                    Log.e(TAG, "Erro ao carregar setores: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SetorResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Falha ao carregar setores: " + t.getMessage());
            }
        });
    }

    private void preencherDropdownSetores(List<Setor> setores) {
        if (etSetorResponsavel == null) return;
        String[] nomesSetores = new String[setores.size()];
        for (int i = 0; i < setores.size(); i++) {
            nomesSetores[i] = setores.get(i).getNome();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, nomesSetores);
        etSetorResponsavel.setAdapter(adapter);
    }

    private void configurarDropdowns() {
        // Configurar Motivo (Fixo)
        String[] motivos = {"Visita", "Entrega", "Manutenção", "Reunião", "Outro"};
        ArrayAdapter<String> adapterMotivo = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, motivos);
        etMotivo.setAdapter(adapterMotivo);

        // Forçar exibição ao clicar
        etMotivo.setOnClickListener(v -> etMotivo.showDropDown());
        etMotivo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etMotivo.showDropDown();
        });

        // Configurar Setor Responsável
        etSetorResponsavel.setOnClickListener(v -> etSetorResponsavel.showDropDown());
        etSetorResponsavel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etSetorResponsavel.showDropDown();
        });

        // Carregar Empresas do Banco de Dados via View de Empresas
        RetrofitClient.getApiService(requireContext()).getEmpresas().enqueue(new Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call, @NonNull Response<EmpresaResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getDados() != null) {
                    List<Empresa> listaEmpresas = response.body().getDados();
                    String[] empresas = new String[listaEmpresas.size()];
                    for (int i = 0; i < listaEmpresas.size(); i++) {
                        empresas[i] = listaEmpresas.get(i).getNome();
                    }

                    ArrayAdapter<String> adapterEmpresa = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, empresas);
                    etEmpresa.setAdapter(adapterEmpresa);

                    // Forçar exibição ao clicar para Empresa
                    etEmpresa.setOnClickListener(v -> etEmpresa.showDropDown());
                    etEmpresa.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) etEmpresa.showDropDown();
                    });
                } else {
                    Log.e(TAG, "Erro na resposta de empresas: " + response.code() + " ou corpo nulo");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao carregar empresas: " + t.getMessage());
            }
        });
    }

    private void preencherChipsSetores(List<Setor> setores) {
        if (chipGroupSetores == null) return;

        // Remove tudo antes de adicionar para evitar duplicatas em recarregamentos
        chipGroupSetores.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Setor setor : setores) {
            Chip chip = (Chip) inflater.inflate(R.layout.layout_chip_filtro, chipGroupSetores, false);
            chip.setText(setor.getNome());
            chip.setTag(setor.getId());       // tag = Integer do ID do setor
            chip.setCheckable(true);
            chipGroupSetores.addView(chip);
        }
    }

    private void configurarCracha() {
        btnAdicionarCracha.setOnClickListener(v ->
                ToastUtils.showInfo(getContext(), "Aproxime o crachá para vincular"));
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

        // Lê o chip selecionado e obtém o ID do setor a partir da tag
        Integer idSetor = null;
        int selectedChipId = chipGroupSetores.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupSetores.findViewById(selectedChipId);
            if (selectedChip != null && selectedChip.getTag() instanceof Integer) {
                idSetor = (Integer) selectedChip.getTag();
            }
        }

        // Se não selecionou chip, mas selecionou no dropdown, tenta encontrar o ID correspondente
        if (idSetor == null && etSetorResponsavel.getText().toString() != null) {
            String setorSelecionado = etSetorResponsavel.getText().toString();
            // Como não temos o ID diretamente no dropdown de String, teríamos que buscar na lista original
            // Para simplificar e garantir o funcionamento, vamos focar no dropdown funcionando visualmente primeiro
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
        } else {
            inputNome.setError(null);
        }

        String cpfLimpo = etCPF.getText().toString().replaceAll("[^\\d]", "");
        if (cpfLimpo.length() < 11) {
            inputCPF.setError("CPF incompleto");
            valido = false;
        } else {
            inputCPF.setError(null);
        }

        if (etEmpresa.getText().toString().trim().isEmpty()) {
            inputEmpresa.setError("Informe a empresa");
            valido = false;
        } else {
            inputEmpresa.setError(null);
        }

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
        etCPF.addTextChangedListener(MascaraUtils.aplicar(etCPF, "###.###.###-##"));
        etTelefone.addTextChangedListener(MascaraUtils.aplicar(etTelefone, "(##) #####-####"));
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
        fotoUri = uri;
        // Remove o tint do ícone padrão para mostrar a foto real
        ivIconePerfil.clearColorFilter();
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
        // Restaura o tint azul do ícone padrão
        ivIconePerfil.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        tvTituloFoto.setText("Foto do Visitante");
        tvSubtituloFoto.setText("Clique para capturar ou selecionar");
        codigoTagVinculada = null;
        tvTituloCracha.setText("Vincular Crachá");
        tvSubtituloCracha.setText("Aproxime o crachá do leitor RFID");
    }
}
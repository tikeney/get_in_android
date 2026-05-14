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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInFragment extends Fragment {

    // Views
    private ConstraintLayout btnAdicionarFoto;
    private ImageView ivIconePerfil;
    private TextView tvTituloFoto, tvSubtituloFoto;

    private TextInputEditText etNome, etCPF, etTelefone, etEmpresa, etMotivo;
    private TextInputLayout inputNome, inputCPF, inputTelefone, inputEmpresa, inputMotivo;

    private ConstraintLayout btnAdicionarCracha;
    private TextView tvTituloCracha, tvSubtituloCracha;

    private ChipGroup chipGroupSetores;
    private Chip chipManutencao, chipAlmoxarifado, chipProducao, chipLaboratorio, chipAdministracao;

    private Button btnFinalizar;

    // Estado
    private Uri fotoUri = null;
    private boolean crachaVinculado = false;

    // Launchers de resultado
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<String> permissaoCameraLauncher;
    private ActivityResultLauncher<String> permissaoArmazenamentoLauncher;

    private Uri cameraImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launcher da câmera
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && cameraImageUri != null) {
                        definirFoto(cameraImageUri);
                    }
                });

        // Launcher da galeria
        galeriaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            definirFoto(uri);
                        }
                    }
                });

        // Launcher de permissão da câmera
        permissaoCameraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        abrirCamera();
                    } else {
                        Toast.makeText(requireContext(),
                                "Permissão de câmera negada.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Launcher de permissão de armazenamento
        permissaoArmazenamentoLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        abrirGaleria();
                    } else {
                        Toast.makeText(requireContext(),
                                "Permissão de armazenamento negada.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarViews(view);
        configurarMascaras();
        configurarFoto();
        //configurarCracha();
        configurarBotaoFinalizar();
    }

    // ──────────────────────────────────────────
    // INICIALIZAÇÃO
    // ──────────────────────────────────────────

    private void inicializarViews(View view) {
        // Foto
        btnAdicionarFoto   = view.findViewById(R.id.btnAdicionarFoto);
        ivIconePerfil      = view.findViewById(R.id.iv_icone_perfil);
        tvTituloFoto       = view.findViewById(R.id.tv_titulo_foto);
        tvSubtituloFoto    = view.findViewById(R.id.tv_subtitulo_foto);

        // Campos
        inputNome      = view.findViewById(R.id.inputNome);
        inputCPF       = view.findViewById(R.id.inputCPF);
        inputTelefone  = view.findViewById(R.id.inputTelefone);
        inputEmpresa   = view.findViewById(R.id.inputEmpresa);
        inputMotivo    = view.findViewById(R.id.inputMotivo);

        etNome     = view.findViewById(R.id.etNome);
        etCPF      = view.findViewById(R.id.etCPF);
        etTelefone = view.findViewById(R.id.etTelefone);
        etEmpresa  = view.findViewById(R.id.etEmpresa);
        etMotivo   = view.findViewById(R.id.etMotivo);

        // Crachá
        btnAdicionarCracha = view.findViewById(R.id.btnAdicionarCracha);
        tvTituloCracha     = view.findViewById(R.id.tv_titulo_cracha);
        tvSubtituloCracha  = view.findViewById(R.id.tv_subtitulo_cracha);

        // Chips
        chipGroupSetores   = view.findViewById(R.id.chipGroupSetores);
        chipManutencao     = view.findViewById(R.id.chipManutencao);
        chipAlmoxarifado   = view.findViewById(R.id.chipAlmoxarifado);
        chipProducao       = view.findViewById(R.id.chipProducao);
        chipLaboratorio    = view.findViewById(R.id.chipLaboratorio);
        chipAdministracao  = view.findViewById(R.id.chipAdministracao);

        // Botão
        btnFinalizar = view.findViewById(R.id.btnLogin);
    }

    // ──────────────────────────────────────────
    // MÁSCARAS DE ENTRADA
    // ──────────────────────────────────────────

    private void configurarMascaras() {
        etCPF.addTextChangedListener(new MascaraTextWatcher(etCPF, "###.###.###-##"));
        etTelefone.addTextChangedListener(new MascaraTextWatcher(etTelefone, "(##) #####-####"));
    }

    /**
     * TextWatcher genérico para aplicar máscaras no formato "###.###-##".
     * Cada '#' representa um dígito.
     */
    private static class MascaraTextWatcher implements TextWatcher {
        private final TextInputEditText campo;
        private final String mascara;
        private boolean editando = false;

        MascaraTextWatcher(TextInputEditText campo, String mascara) {
            this.campo   = campo;
            this.mascara = mascara;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (editando) return;
            editando = true;

            // Remove tudo que não for dígito
            String digits = s.toString().replaceAll("[^\\d]", "");
            StringBuilder sb = new StringBuilder();
            int di = 0;

            for (int i = 0; i < mascara.length() && di < digits.length(); i++) {
                char mc = mascara.charAt(i);
                if (mc == '#') {
                    sb.append(digits.charAt(di++));
                } else {
                    sb.append(mc);
                }
            }

            campo.setText(sb.toString());
            campo.setSelection(sb.length());
            editando = false;
        }
    }

    // ──────────────────────────────────────────
    // FOTO
    // ──────────────────────────────────────────

    private void configurarFoto() {
        btnAdicionarFoto.setOnClickListener(v -> mostrarOpcoesFoto());
    }

    private void mostrarOpcoesFoto() {
        String[] opcoes = {"Tirar foto", "Escolher da galeria"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Adicionar foto")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) verificarPermissaoCamera();
                    else            verificarPermissaoGaleria();
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
        // Android 13+ usa READ_MEDIA_IMAGES; abaixo usa READ_EXTERNAL_STORAGE
        String permissao;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissao = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permissao = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

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
                    fotoFile
            );
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Erro ao criar arquivo de foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private File criarArquivoTemporario() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = requireContext().getExternalFilesDir(null);
        return File.createTempFile("FOTO_" + timestamp, ".jpg", dir);
    }

    private void definirFoto(Uri uri) {
        fotoUri = uri;
        ivIconePerfil.setImageURI(uri);
        ivIconePerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
        tvTituloFoto.setText("Foto adicionada");
        tvSubtituloFoto.setText("Toque para trocar");
    }

    // ──────────────────────────────────────────
    // CRACHÁ
    // ──────────────────────────────────────────

    //private void configurarCracha() {
    //    btnAdicionarCracha.setOnClickListener(v -> simularLeituraCracha());
    //}

    //private void simularLeituraCracha() {
        // Simulação de leitura RFID — substitua pela lógica real de NFC/RFID
    //    Toast.makeText(requireContext(),
    //            "Aproxime o crachá do leitor...", Toast.LENGTH_SHORT).show();

        // Simula sucesso após 1,5 s para fins de demonstração
    //    btnAdicionarCracha.postDelayed(() -> {
    //        crachaVinculado = true;
    //        tvTituloCracha.setText("Crachá vinculado!");
    //        tvSubtituloFoto.setText("Toque para desvincular");
    //        tvSubtituloCracha.setText("ID: RFID-00847231");
    //        Toast.makeText(requireContext(), "Crachá vinculado com sucesso!", Toast.LENGTH_SHORT).show();
    //   }, 1500);
    //}

    // ──────────────────────────────────────────
    // VALIDAÇÃO E FINALIZAÇÃO
    // ──────────────────────────────────────────

    private void configurarBotaoFinalizar() {
        btnFinalizar.setOnClickListener(v -> {
            if (validarFormulario()) {
                finalizarCheckIn();
            }
        });
    }

    private boolean validarFormulario() {
        boolean valido = true;

        // Nome
        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            inputNome.setError("Informe o nome completo");
            valido = false;
        } else {
            inputNome.setError(null);
        }

        // CPF
        String cpf = etCPF.getText() != null ? etCPF.getText().toString().trim() : "";
        if (cpf.length() < 14) { // "###.###.###-##" = 14 chars
            inputCPF.setError("CPF inválido");
            valido = false;
        } else {
            inputCPF.setError(null);
        }

        // Telefone
        String tel = etTelefone.getText() != null ? etTelefone.getText().toString().trim() : "";
        if (tel.length() < 15) { // "(##) #####-####" = 15 chars
            inputTelefone.setError("Telefone inválido");
            valido = false;
        } else {
            inputTelefone.setError(null);
        }

        // Empresa
        String empresa = etEmpresa.getText() != null ? etEmpresa.getText().toString().trim() : "";
        if (empresa.isEmpty()) {
            inputEmpresa.setError("Informe a empresa");
            valido = false;
        } else {
            inputEmpresa.setError(null);
        }

        // Motivo
        String motivo = etMotivo.getText() != null ? etMotivo.getText().toString().trim() : "";
        if (motivo.isEmpty()) {
            inputMotivo.setError("Informe o motivo da visita");
            valido = false;
        } else {
            inputMotivo.setError(null);
        }

        // Setores — pelo menos 1 selecionado
        if (obterSetoresSelecionados().isEmpty()) {
            Toast.makeText(requireContext(),
                    "Selecione pelo menos um setor permitido.", Toast.LENGTH_SHORT).show();
            valido = false;
        }

        return valido;
    }

    private List<String> obterSetoresSelecionados() {
        List<String> setores = new ArrayList<>();
        if (chipManutencao.isChecked())    setores.add("Manutenção");
        if (chipAlmoxarifado.isChecked())  setores.add("Almoxarifado");
        if (chipProducao.isChecked())      setores.add("Produção");
        if (chipLaboratorio.isChecked())   setores.add("Laboratório");
        if (chipAdministracao.isChecked()) setores.add("Administração");
        return setores;
    }

    private void finalizarCheckIn() {
        String nome    = etNome.getText().toString().trim();
        String cpf     = etCPF.getText().toString().trim();
        String tel     = etTelefone.getText().toString().trim();
        String empresa = etEmpresa.getText().toString().trim();
        String motivo  = etMotivo.getText().toString().trim();
        List<String> setores = obterSetoresSelecionados();

        // ── Aqui você conecta ao seu repositório / API ──
        // Ex.: viewModel.salvarCheckIn(nome, cpf, tel, empresa, motivo, setores, fotoUri);

        String msg = "Check-in realizado!\n"
                + nome + " | " + empresa + "\n"
                + "Setores: " + setores;

        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        limparFormulario();
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

        crachaVinculado = false;
        tvTituloCracha.setText("Vincular crachá");
        tvSubtituloCracha.setText("Toque e aproxime o cracha");
    }
}

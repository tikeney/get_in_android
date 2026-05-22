package com.senai.get_in;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.LoginRequest;
import com.senai.get_in.model.LoginResponse;
import com.senai.get_in.model.TagLoginRequest;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioDetalhadoResponse;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsuario, etSenha;
    private Button btnLogin, btnLoginRFID;
    private ProgressBar progressBarLogin;
    private TokenManager tokenManager;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private View constHeader, constForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tokenManager = new TokenManager(this);
        
        // Aplicar o tema antes de carregar a UI
        if (tokenManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Log.w(TAG, "NFC não suportado neste dispositivo.");
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE);
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        constHeader = findViewById(R.id.ConstHeader);
        constForm = findViewById(R.id.ConstForm);
        
        applyEntryAnimations();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginRFID = findViewById(R.id.btnLoginRFID);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        btnLogin.setOnClickListener(v -> validarLogin());
        btnLoginRFID.setOnClickListener(v -> {
            if (nfcAdapter == null) {
                ToastUtils.showInfo(this, "NFC não disponível");
            } else if (!nfcAdapter.isEnabled()) {
                ToastUtils.showInfo(this, "Por favor, ative o NFC");
            } else {
                ToastUtils.showInfo(this, "Aproxime o crachá do leitor");
            }
        });
    }

    private void applyEntryAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        
        constHeader.startAnimation(fadeIn);
        constForm.startAnimation(slideUp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
            NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                String tagId = bytesToHexString(tag.getId());
                Log.d(TAG, "Tag RFID detectada: " + tagId);
                realizarLoginPorTag(tagId);
            }
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void realizarLoginPorTag(String tagId) {
        setProgressBar(true);
        TagLoginRequest request = new TagLoginRequest(tagId);

        RetrofitClient.getApiService(this).loginByTag(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSucesso()) {
                        finalizarLogin(loginResponse.getToken(), converterParaUsuarioDetalhado(loginResponse.getData()));
                    } else {
                        setProgressBar(false);
                        ToastUtils.showError(LoginActivity.this, loginResponse.getMensagem());
                    }
                } else {
                    setProgressBar(false);
                    ToastUtils.showError(LoginActivity.this, "Crachá não reconhecido");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setProgressBar(false);
                Log.e(TAG, "Erro login por tag: " + t.getMessage());
                ToastUtils.showError(LoginActivity.this, "Falha na conexão");
            }
        });
    }

    private void setProgressBar(boolean loading) {
        if (loading) {
            btnLogin.setEnabled(false);
            btnLoginRFID.setEnabled(false);
            btnLogin.setText("");
            progressBarLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
            btnLoginRFID.setEnabled(true);
            btnLogin.setText("Entrar");
            progressBarLogin.setVisibility(View.GONE);
        }
    }

    private void validarLogin() {
        String email = etUsuario.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (email.isEmpty()) {
            etUsuario.setError("Digite o e-mail!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUsuario.setError("Digite um e-mail válido!");
            return;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Digite a senha!");
            return;
        }

        setProgressBar(true);

        LoginRequest loginRequest = new LoginRequest(email, senha);
        Log.d(TAG, "Tentando login para: " + email);

        RetrofitClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSucesso() && loginResponse.getData() != null) {
                        String token = loginResponse.getToken();
                        UsuarioDetalhado user = converterParaUsuarioDetalhado(loginResponse.getData());
                        
                        Log.d(TAG, "Login OK. ID Extraído: " + user.getId());

                        if (user.getId() > 0) {
                            // Busca dados completos da View imediatamente após o login
                            buscarDadosCompletosView(token, user);
                        } else {
                            Log.e(TAG, "ID do usuário é 0. Erro no parsing do JSON.");
                            setProgressBar(false);
                            ToastUtils.showError(LoginActivity.this, "Erro ao processar dados da conta.");
                        }
                    } else {
                        setProgressBar(false);
                        String msg = loginResponse.getMensagem() != null ? loginResponse.getMensagem() : "Erro ao autenticar.";
                        ToastUtils.showError(LoginActivity.this, msg);
                    }
                } else {
                    setProgressBar(false);
                    try {
                        String errorJson = response.errorBody().string();
                        Log.e(TAG, "Erro login: " + errorJson);
                        ToastUtils.showError(LoginActivity.this, "E-mail ou senha incorretos.");
                    } catch (Exception e) {
                        Log.e(TAG, "Erro no servidor: " + response.code());
                        ToastUtils.showError(LoginActivity.this, "Erro de servidor: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setProgressBar(false);
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
                ToastUtils.showError(LoginActivity.this, "Falha na conexão com o servidor.");
            }
        });
    }

    private UsuarioDetalhado converterParaUsuarioDetalhado(LoginResponse.LoginData data) {
        UsuarioDetalhado user = new UsuarioDetalhado();
        if (data != null && data.usuario != null) {
            user.setId(data.usuario.id);
            user.setNome(data.usuario.nome);
            user.setEmail(data.usuario.email);
            user.setCpf(data.usuario.cpf);
            user.setCelular(data.usuario.celular);
            user.setDataDeCriacao(data.usuario.dataDeCriacao);
            
            if (data.funcionario != null) {
                user.setCargo(data.funcionario.tipo);
            }
        }
        return user;
    }

    private void buscarDadosCompletosView(String token, UsuarioDetalhado userIncompleto) {
        // Salva o token temporariamente para permitir a chamada autenticada à View
        tokenManager.saveToken(token);
        
        Log.d(TAG, "Buscando View para ID: " + userIncompleto.getId());

        RetrofitClient.getApiService(this).getUsuarioDetalhadoPorId(userIncompleto.getId()).enqueue(new Callback<UsuarioDetalhadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Response<UsuarioDetalhadoResponse> response) {
                setProgressBar(false);
                UsuarioDetalhado userFinal = userIncompleto;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    userFinal = response.body().getData();
                    Log.d(TAG, "Dados da View obtidos com sucesso.");
                } else {
                    Log.w(TAG, "Falha ao obter dados da View (HTTP " + response.code() + "). Usando dados básicos.");
                }
                finalizarLogin(token, userFinal);
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDetalhadoResponse> call, @NonNull Throwable t) {
                setProgressBar(false);
                Log.e(TAG, "Erro ao buscar View: " + t.getMessage());
                finalizarLogin(token, userIncompleto);
            }
        });
    }

    private void finalizarLogin(String token, UsuarioDetalhado user) {
        tokenManager.saveToken(token);
        tokenManager.saveUserData(user);
        irParaMain();
    }

    private void irParaMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}

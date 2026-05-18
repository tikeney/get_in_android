package com.senai.get_in;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.LoginRequest;
import com.senai.get_in.model.LoginResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsuario, etSenha;
    private Button btnLogin;
    private ProgressBar progressBarLogin;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tokenManager = new TokenManager(this);

        // Se já estiver logado, vai direto para a Main
        if (tokenManager.getToken() != null && tokenManager.getUserData() != null) {
            irParaMain();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        btnLogin.setOnClickListener(v -> validarLogin());
    }

    private void setProgressBar(boolean loading) {
        if (loading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("");
            progressBarLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
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

        RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setProgressBar(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSucesso() && loginResponse.getData() != null && !loginResponse.getData().isEmpty()) {
                        String token = loginResponse.getToken();
                        // Pega o primeiro usuário da lista conforme o novo formato da API
                        UsuarioDetalhado user = loginResponse.getData().get(0);
                        
                        Log.d(TAG, "Login bem-sucedido. Usuário: " + user.getNome() + " | Cargo: " + user.getCargo());
                        
                        finalizarLogin(token, user);
                    } else {
                        String msg = loginResponse.getMensagem() != null ? loginResponse.getMensagem() : "Credenciais inválidas ou usuário não encontrado.";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        LoginResponse errorResp = new Gson().fromJson(errorJson, LoginResponse.class);
                        String msg = (errorResp != null && errorResp.getMensagem() != null) ? errorResp.getMensagem() : "E-mail ou senha incorretos.";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        String errorMsg = "Erro " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Corpo do erro: " + errorBody);
                            }
                        } catch (Exception ignored) {}
                        Log.e(TAG, "Erro no servidor: " + response.code());
                        Toast.makeText(LoginActivity.this, "Erro de servidor: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setProgressBar(false);
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Falha na conexão com o servidor. Verifique sua internet.", Toast.LENGTH_LONG).show();
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
        finish();
    }
}

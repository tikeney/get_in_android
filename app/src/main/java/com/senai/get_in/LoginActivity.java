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
import com.senai.get_in.model.UsuarioResponse;
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
        
        if (tokenManager.getToken() != null) {
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
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSucesso()) {
                        String token = loginResponse.getToken();
                        UsuarioDetalhado user = converterParaUsuarioDetalhado(loginResponse.getData());
                        
                        Log.d(TAG, "Login bem-sucedido. Validando perfil...");

                        if (user.getCargo() != null && !user.getCargo().isEmpty() && !user.getCargo().equalsIgnoreCase("null")) {
                            finalizarLogin(token, user);
                        } else {
                            Log.d(TAG, "Cargo ausente no login, buscando dados em /usuarios...");
                            buscarDadosCargo(token, user);
                        }
                    } else {
                        setProgressBar(false);
                        String msg = loginResponse.getMensagem() != null ? loginResponse.getMensagem() : "Erro ao autenticar.";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    setProgressBar(false);
                    try {
                        String errorJson = response.errorBody().string();
                        LoginResponse errorResp = new Gson().fromJson(errorJson, LoginResponse.class);
                        String msg = (errorResp != null && errorResp.getMensagem() != null) ? errorResp.getMensagem() : "E-mail ou senha incorretos.";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro no servidor: " + response.code());
                        Toast.makeText(LoginActivity.this, "Erro de servidor: " + response.code(), Toast.LENGTH_LONG).show();
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

    private UsuarioDetalhado converterParaUsuarioDetalhado(LoginResponse.UserData data) {
        UsuarioDetalhado user = new UsuarioDetalhado();
        if (data != null) {
            user.setId(data.getId());
            user.setNome(data.getNome());
            user.setEmail(data.getEmail());
            user.setCargo(data.getCargo());
            user.setCpf(data.getCpf());
            user.setCelular(data.getCelular());
            user.setDataDeCriacao(data.getDataDeCriacao());
        }
        return user;
    }

    private void buscarDadosCargo(String token, UsuarioDetalhado userIncompleto) {
        String authHeader = "Bearer " + token;
        RetrofitClient.getApiService().getUsuarios(authHeader).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                setProgressBar(false);
                UsuarioDetalhado userFinal = userIncompleto;
                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioDetalhado> lista = response.body().getData();
                    if (lista != null) {
                        for (UsuarioDetalhado u : lista) {
                            if (u.getEmail() != null && userIncompleto.getEmail() != null &&
                                userIncompleto.getEmail().equalsIgnoreCase(u.getEmail().trim())) {
                                userFinal = u;
                                break;
                            }
                        }
                    }
                }
                finalizarLogin(token, userFinal);
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                setProgressBar(false);
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
        finish();
    }
}

package com.senai.get_in;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.LoginRequest;
import com.senai.get_in.model.LoginResponse;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUsuario, etSenha;
    private Button btnLogin, btnLoginRFID;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tokenManager = new TokenManager(this);

        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginRFID = findViewById(R.id.btnLoginRFID);

        btnLogin.setOnClickListener(v -> {
            validarLogin();
        });

        btnLoginRFID.setOnClickListener(v -> {
            Toast.makeText(this, "Aproxime seu crachá do leitor...", Toast.LENGTH_SHORT).show();
        });
    }

    private void validarLogin() {
        String usuario = etUsuario.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (usuario.isEmpty()) {
            etUsuario.setError("Digite o e-mail!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            etUsuario.setError("Digite um e-mail válido!");
            return;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Digite a senha!");
            return;
        }

        LoginRequest loginRequest = new LoginRequest(usuario, senha);
        RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    tokenManager.saveToken(token);

                    Toast.makeText(LoginActivity.this, "Login realizado!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

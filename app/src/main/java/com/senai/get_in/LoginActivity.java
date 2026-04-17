package com.senai.get_in;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUsuario, etSenha;
    private Button  btnLogin, btnLoginRFID;
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

        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginRFID = findViewById(R.id.btnLoginRFID);

        btnLogin.setOnClickListener(v->{
            validarLogin();
        });

        btnLoginRFID.setOnClickListener(v->{
            Toast.makeText(this, "Aproxime seu crachá do leitor...", Toast.LENGTH_SHORT).show();
        });
    }
    private void validarLogin() {
        String usuario = etUsuario.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (usuario.isEmpty()) {
            etUsuario.setError("Digite o usuário!");
            return;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Digite a senha!");
            return;
        }

        if (usuario.equals("admin") && senha.equals("1234")) {
            Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

            finish();

        } else {
            Toast.makeText(this, "Usuário ou senha incorretos.", Toast.LENGTH_LONG).show();
        }
    }

}
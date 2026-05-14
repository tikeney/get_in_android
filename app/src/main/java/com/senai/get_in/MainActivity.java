package com.senai.get_in;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.TokenManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private TokenManager tokenManager;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private String cargo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        UsuarioDetalhado user = tokenManager.getUserData();

        if (user == null) {
            logout();
            return;
        }

        // Normalização do cargo para garantir detecção correta
        cargo = (user.getCargo() != null) ? user.getCargo().trim().toLowerCase() : "";
        Log.d(TAG, "Sessão iniciada - Cargo: [" + cargo + "]");

        setupUI();
        setupNavigation();
    }

    private void setupUI() {
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            appBarLayout.setPadding(0, systemBars.top, 0, 0);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHeader(tokenManager.getUserData());
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // 1. CAMADA DE SEGURANÇA: Listener de navegação em tempo real
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            
            // Se o usuário tentar acessar uma tela proibida para o cargo dele
            if (!isAdminOrGerente() && !isAllowedDestination(id)) {
                Log.w(TAG, "BLOQUEIO: Redirecionando para o perfil. Tela proibida: " + id);
                controller.navigate(R.id.nav_perfil);
                return;
            }
            
            if (getSupportActionBar() != null && destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
        });

        // 2. CONFIGURAÇÃO DINÂMICA DO GRAFO E VISIBILIDADE DOS MENUS
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        Menu menu = navigationView.getMenu();

        if (isAdminOrGerente()) {
            bottomNav.setVisibility(View.VISIBLE);
            NavigationUI.setupWithNavController(bottomNav, navController);
            navGraph.setStartDestination(R.id.nav_notificacoes);
        } else {
            // Portaria, Supervisor e Funcionário não veem a BottomNav
            bottomNav.setVisibility(View.GONE);
            
            // Portaria e Func iniciam no Perfil (conforme solicitado)
            if (isSupervisor()) {
                navGraph.setStartDestination(R.id.nav_notificacoes);
            } else {
                navGraph.setStartDestination(R.id.nav_perfil);
            }
            
            restrictMenu(menu);
        }

        navController.setGraph(navGraph);
    }

    // --- MÉTODOS DE CONTROLE DE ACESSO ---

    private boolean isAdminOrGerente() {
        return "adm".equals(cargo) || "ger".equals(cargo) || "administrador".equals(cargo) || "gerente".equals(cargo);
    }

    private boolean isPortaria() {
        return "port".equals(cargo) || "portaria".equals(cargo);
    }

    private boolean isSupervisor() {
        return "sup".equals(cargo) || "supervisor".equals(cargo);
    }

    private boolean isFuncionario() {
        return !isAdminOrGerente() && !isPortaria() && !isSupervisor();
    }

    private boolean isAllowedDestination(int id) {
        // Telas universais (Perfil e Configurações sempre permitidos)
        if (id == R.id.nav_perfil || id == R.id.menu_configuracoes) return true;
        
        if (isAdminOrGerente()) return true;

        if (isPortaria()) {
            // Portaria: Acesso a Check-in e Visitantes
            return id == R.id.nav_checkIn || id == R.id.nav_visitantes;
        }

        if (isSupervisor()) {
            return id == R.id.nav_notificacoes;
        }

        return false; // Funcionário comum só vê Perfil e Config
    }

    private void restrictMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int id = item.getItemId();

            if (item.hasSubMenu()) {
                restrictMenu(item.getSubMenu());
                
                // Esconde o menu pai se nenhum filho for visível
                boolean anyChildVisible = false;
                Menu sub = item.getSubMenu();
                for (int j = 0; j < sub.size(); j++) {
                    if (sub.getItem(j).isVisible()) {
                        anyChildVisible = true;
                        break;
                    }
                }
                item.setVisible(anyChildVisible);
            } else {
                // Sair é sempre permitido
                if (id == R.id.menu_sair) {
                    item.setVisible(true);
                } else {
                    item.setVisible(isAllowedDestination(id));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        if (!isAdminOrGerente()) {
            restrictMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sair) {
            logout();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void updateNavHeader(UsuarioDetalhado user) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null && user != null) {
            ((TextView) headerView.findViewById(R.id.tvHeaderNome)).setText(user.getNome());
            ((TextView) headerView.findViewById(R.id.tvHeaderEmail)).setText(user.getEmail());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sair) {
            logout();
        } else {
            // A navegação só ocorre se for permitida
            if (isAdminOrGerente() || isAllowedDestination(id)) {
                navController.navigate(id);
            } else {
                Toast.makeText(this, "Acesso restrito", Toast.LENGTH_SHORT).show();
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

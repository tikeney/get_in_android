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
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

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

        // Normalização do cargo
        cargo = (user.getCargo() != null) ? user.getCargo().trim().toLowerCase() : "";
        Log.d(TAG, "Sessão iniciada - Cargo detectado: [" + cargo + "]");

        setupUI();
        setupNavigation();
    }

    private void setupUI() {
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((androidx.appcompat.widget.Toolbar) toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            appBarLayout.setPadding(0, systemBars.top, 0, 0);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHeader(tokenManager.getUserData());
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment == null) {
            Log.e(TAG, "NavHostFragment não encontrado!");
            return;
        }
        
        navController = navHostFragment.getNavController();

        // Configura a navegação automática com os menus
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Intercepta cliques no NavigationView para tratar o Logout
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_sair) {
                logout();
                return true;
            }
            
            // Tenta navegar automaticamente
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        });

        // Aplica restrições de visibilidade APÓS a configuração inicial
        restrictMenu(navigationView.getMenu());
        restrictMenu(bottomNav.getMenu());

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        int startId = getStartDestinationId();
        navGraph.setStartDestination(startId);
        navController.setGraph(navGraph);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null && destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
        });
        
        // Mostra BottomNav apenas se o usuário tiver acesso a mais de 2 páginas além do perfil/config
        if (isAdmin() || isGerente() || isPortaria() || isSupervisor()) {
            bottomNav.setVisibility(View.VISIBLE);
        } else {
            bottomNav.setVisibility(View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }
    }

    // --- MÉTODOS DE CONTROLE DE ACESSO ---

    private boolean isAdmin() { 
        return cargo.contains("adm"); 
    }
    private boolean isGerente() { 
        return cargo.contains("ger") || cargo.contains("geracesso"); 
    }
    private boolean isSupervisor() { 
        return cargo.contains("sup"); 
    }
    private boolean isPortaria() { 
        return cargo.contains("port"); 
    }
    private boolean isFuncionario() { 
        return cargo.contains("func"); 
    }

    private int getStartDestinationId() {
        // Agora abre sempre no Perfil como página principal para todos
        return R.id.nav_perfil;
    }

    private boolean isAllowedDestination(int id) {
        // Itens sempre permitidos
        if (id == R.id.menu_sair || id == R.id.nav_perfil || id == R.id.menu_configuracoes || id == R.id.nav_host_fragment) {
            return true;
        }

        if (isAdmin()) {
            return true; // Adm tem acesso a tudo
        }

        if (isGerente()) {
            // Gerente tem acesso a tudo menos checkin
            return id != R.id.nav_checkIn;
        }

        if (isSupervisor()) {
            // Supervisor: Perfil, Configuração, Autorização e Notificação
            return id == R.id.nav_autorizacao || id == R.id.nav_notificacoes;
        }

        if (isPortaria()) {
            // Portaria: Checkin, Perfil, Configuração, Notificações
            return id == R.id.nav_checkIn || id == R.id.nav_notificacoes || id == R.id.nav_perfil || id == R.id.menu_configuracoes;
        }

        if (isFuncionario()) {
            // Funcionário: Perfil e Configuração (já retornados no topo)
            return false; 
        }

        return false;
    }

    private void restrictMenu(Menu menu) {
        if (menu == null) return;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int id = item.getItemId();

            if (item.hasSubMenu()) {
                restrictMenu(item.getSubMenu());
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
                item.setVisible(isAllowedDestination(id));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        restrictMenu(menu);
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

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

        // Normalização do cargo para garantir detecção correta (remove espaços e coloca em minúsculo)
        cargo = (user.getCargo() != null) ? user.getCargo().trim().toLowerCase() : "";
        Log.d(TAG, "Sessão iniciada - Cargo detectado: [" + cargo + "]");

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

        // 1. Segurança de Navegação
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            // Bloqueio proativo de telas não autorizadas
            if (!isAdminOrGerente() && !isAllowedDestination(id)) {
                Log.w(TAG, "Acesso proibido à tela " + id + ". Redirecionando para Perfil.");
                controller.navigate(R.id.nav_perfil);
            }
            if (getSupportActionBar() != null && destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
        });

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        // 2. Configuração de Visibilidade Baseada no Cargo
        if (isFuncionario()) {
            // Funcionário comum: Não vê BottomNav e inicia no Perfil
            bottomNav.setVisibility(View.GONE);
            navGraph.setStartDestination(R.id.nav_perfil);
        } else {
            // Outros cargos (Portaria, Supervisor, Admin): Veem BottomNav
            bottomNav.setVisibility(View.VISIBLE);
            NavigationUI.setupWithNavController(bottomNav, navController);
            
            // Define tela inicial padrão para quem tem acesso a notificações
            if (isAdminOrGerente() || isSupervisor() || isPortaria()) {
                navGraph.setStartDestination(R.id.nav_notificacoes);
            } else {
                navGraph.setStartDestination(R.id.nav_perfil);
            }
        }

        // Define o grafo de navegação
        navController.setGraph(navGraph);

        // 3. Aplica restrições de visibilidade nos Menus (Drawer e BottomNav)
        restrictMenu(navigationView.getMenu());
        restrictMenu(bottomNav.getMenu());
    }

    // --- MÉTODOS DE CONTROLE DE ACESSO ---

    private boolean isAdminOrGerente() {
        return cargo.contains("adm") || cargo.contains("ger") || cargo.contains("administrador") || cargo.contains("gerente");
    }

    private boolean isPortaria() {
        // Captura variações como "port", "portaria", "porteiro", "recepcao"
        return cargo.contains("port") || cargo.contains("recep");
    }

    private boolean isSupervisor() {
        return cargo.contains("sup") || cargo.contains("supervisor");
    }

    private boolean isFuncionario() {
        // Só é considerado "funcionário restrito" se NÃO possuir nenhum dos cargos privilegiados
        if (isAdminOrGerente() || isPortaria() || isSupervisor()) {
            return false;
        }
        // Fallback ou se contiver explicitamente "func"
        return true; 
    }

    private boolean isAllowedDestination(int id) {
        // Telas universais (Sempre permitidas)
        if (id == R.id.nav_perfil || id == R.id.menu_configuracoes || id == R.id.menu_sair) {
            return true;
        }
        
        // Acesso total para Admin/Gerente
        if (isAdminOrGerente()) return true;

        // Regras para Portaria: Vê universais + Check-in + Notificações
        if (isPortaria()) {
            return id == R.id.nav_checkIn || id == R.id.nav_notificacoes;
        }

        // Regras para Supervisor
        if (isSupervisor()) {
            return id == R.id.nav_notificacoes;
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
                // Define visibilidade baseada na regra de destino permitido
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sair) {
            logout();
        } else {
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

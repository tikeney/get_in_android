package com.senai.get_in;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

    private TokenManager tokenManager;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Ajustar insets para evitar sobreposição com as barras de sistema (Status Bar e Navigation Bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Aplica padding no topo do AppBarLayout para compensar a barra de status
            // Isso garante que o título da Toolbar não fique atrás dos ícones de bateria/relógio
            appBarLayout.setPadding(0, systemBars.top, 0, 0);
            
            // Aplica padding na parte inferior da bottom navigation para a barra de gestos do sistema
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            
            return insets;
        });

        tokenManager = new TokenManager(this);
        UsuarioDetalhado user = tokenManager.getUserData();

        if (user == null) {
            logout();
            return;
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Update Header with user data
        updateNavHeader(user);

        // Configuração dinâmica do Grafo de Navegação
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        String cargo = user.getCargo();
        Menu menu = navigationView.getMenu();

        // Configuração de acesso por cargo
        if ("func".equalsIgnoreCase(cargo)) {
            bottomNav.setVisibility(View.GONE);
            navGraph.setStartDestination(R.id.nav_perfil);
            menu.findItem(R.id.nav_checkIn).setVisible(false);
            menu.findItem(R.id.nav_visitantes).setVisible(false);
            menu.findItem(R.id.nav_historico).setVisible(false);
            menu.findItem(R.id.nav_notificacoes).setVisible(false);
        } else if ("port".equalsIgnoreCase(cargo)) {
            bottomNav.setVisibility(View.GONE);
            navGraph.setStartDestination(R.id.nav_checkIn);
            menu.findItem(R.id.nav_notificacoes).setVisible(false);
        } else if ("sup".equalsIgnoreCase(cargo)) {
            bottomNav.setVisibility(View.GONE);
            navGraph.setStartDestination(R.id.nav_notificacoes);
            menu.findItem(R.id.nav_checkIn).setVisible(false);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
        
        navController.setGraph(navGraph);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence label = destination.getLabel();
            if (label != null && getSupportActionBar() != null) {
                getSupportActionBar().setTitle(label);
            }
        });
    }

    private void updateNavHeader(UsuarioDetalhado user) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNome = headerView.findViewById(R.id.tvHeaderNome);
        TextView tvEmail = headerView.findViewById(R.id.tvHeaderEmail);
        
        if (user != null) {
            tvNome.setText(user.getNome());
            tvEmail.setText(user.getEmail());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_sair) {
            logout();
        } else {
            navController.navigate(id);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

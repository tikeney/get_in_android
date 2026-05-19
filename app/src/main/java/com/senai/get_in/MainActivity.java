package com.senai.get_in;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.model.AvatarResponse;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private TokenManager tokenManager;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private String cargo;
    
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    public interface NfcTagListener {
        void onTagRead(String tagId);
    }

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

        cargo = (user.getCargo() != null) ? user.getCargo().trim().toLowerCase() : "";
        Log.d(TAG, "Sessão iniciada - Usuário: " + user.getNome() + " | Cargo: [" + cargo + "]");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE);
        }

        setupUI();
        setupNavigation();
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
                dispatchTagToFragments(tagId);
            }
        }
    }

    private void dispatchTagToFragments(String tagId) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (currentFragment instanceof NfcTagListener) {
                ((NfcTagListener) currentFragment).onTagRead(tagId);
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

        final int startId = getStartDestinationId();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            
            if (!isAllowedDestination(id)) {
                Log.w(TAG, "Acesso bloqueado para tela: " + id + ". Redirecionando.");
                if (id != startId) {
                    controller.navigate(startId);
                } else if (id != R.id.nav_perfil) {
                    controller.navigate(R.id.nav_perfil);
                }
            }
            
            if (getSupportActionBar() != null && destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
        });

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        navGraph.setStartDestination(startId);
        navController.setGraph(navGraph);

        NavigationUI.setupWithNavController(bottomNav, navController);
        
        restrictMenu(navigationView.getMenu());
        restrictMenu(bottomNav.getMenu());
        
        bottomNav.setVisibility(isFuncionario() ? View.GONE : View.VISIBLE);
    }

    private boolean isAdmin() { return cargo.equals("adm") || cargo.equals("administrador"); }
    private boolean isGerente() { return cargo.equals("ger") || cargo.equals("gerente"); }
    private boolean isSupervisor() { return cargo.equals("sup") || cargo.equals("supervisor"); }
    private boolean isPortaria() { return cargo.equals("port") || cargo.equals("portaria") || cargo.equals("porteiro"); }
    private boolean isFuncionario() { return cargo.equals("func") || cargo.equals("funcionario") || cargo.isEmpty(); }

    private int getStartDestinationId() {
        if (isPortaria()) return R.id.nav_checkIn;
        if (isAdmin() || isGerente()) return R.id.nav_notificacoes;
        return R.id.nav_perfil;
    }

    private boolean isAllowedDestination(int id) {
        if (id == R.id.nav_perfil || id == R.id.menu_configuracoes || id == R.id.menu_sair) {
            return true;
        }

        if (isAdmin()) return true;
        if (isGerente()) return id != R.id.nav_checkIn;
        if (isSupervisor()) {
            return id == R.id.nav_notificacoes || id == R.id.nav_autorizacao || 
                   id == R.id.nav_historico || id == R.id.nav_visitantes;
        }
        if (isPortaria()) {
            return id == R.id.nav_checkIn || id == R.id.nav_visitantes || id == R.id.nav_notificacoes;
        }
        return false;
    }

    private void restrictMenu(Menu menu) {
        if (menu == null) return;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.hasSubMenu()) {
                restrictMenu(item.getSubMenu());
                item.setVisible(hasVisibleChildren(item.getSubMenu()));
            } else {
                item.setVisible(isAllowedDestination(item.getItemId()));
            }
        }
    }

    private boolean hasVisibleChildren(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).isVisible()) return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        restrictMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_sair) {
            logout();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void updateNavHeader(UsuarioDetalhado user) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null && user != null) {
            TextView tvNome = headerView.findViewById(R.id.tvHeaderNome);
            TextView tvEmail = headerView.findViewById(R.id.tvHeaderEmail);
            ImageView ivFoto = headerView.findViewById(R.id.ivHeaderFoto);

            tvNome.setText(user.getNome());
            tvEmail.setText(user.getEmail());

            if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
                Glide.with(this)
                        .load(user.getFotoPerfil())
                        .placeholder(R.drawable.outline_person_24)
                        .into(ivFoto);
            } else {
                RetrofitClient.getApiService().getAvatar(user.getId()).enqueue(new Callback<AvatarResponse>() {
                    @Override
                    public void onResponse(Call<AvatarResponse> call, Response<AvatarResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            String url = response.body().getData().getUrl();
                            if (url != null && !url.isEmpty() && !isDestroyed()) {
                                Glide.with(MainActivity.this)
                                        .load(url)
                                        .placeholder(R.drawable.outline_person_24)
                                        .into(ivFoto);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AvatarResponse> call, Throwable t) {
                        Log.e(TAG, "Erro ao buscar avatar via rota: " + t.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sair) {
            logout();
        } else {
            if (isAllowedDestination(id)) {
                navController.navigate(id);
            } else {
                Toast.makeText(this, "Seu acesso é restrito para esta funcionalidade.", Toast.LENGTH_SHORT).show();
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

package com.senai.get_in;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.senai.get_in.api.RetrofitClient;
import com.senai.get_in.databinding.ActivityMainBinding;
import com.senai.get_in.model.UsuarioDetalhado;
import com.senai.get_in.model.UsuarioDetalhadoResponse;
import com.senai.get_in.utils.AccessManager;
import com.senai.get_in.utils.ToastUtils;
import com.senai.get_in.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private TokenManager tokenManager;
    private NavController navController;
    private UsuarioDetalhado currentUser;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    public interface NfcTagListener {
        void onTagRead(String tagId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tokenManager = new TokenManager(this);

        if (tokenManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = tokenManager.getUserData();

        if (currentUser == null) {
            logout();
            return;
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE);
        }

        setupUI();
        applyBottomNavConfig();
        setupNavigation();
        sincronizarDadosUsuario();
    }

    private void setupUI() {
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle("");
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Aplica padding no topo da AppBar da Activity (quando visível)
            binding.appBar.setPadding(0, systemBars.top, 0, 0);

            // Ajusta a margem inferior do card do menu para flutuar acima da barra de navegação do sistema
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) binding.cardBottomNav.getLayoutParams();
            lp.bottomMargin = systemBars.bottom + (int)(24 * getResources().getDisplayMetrics().density);
            binding.cardBottomNav.setLayoutParams(lp);

            return insets;
        });

        binding.navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // MUDA A COR DO ÍCONE DO MENU (HAMBÚRGUER) DE FORMA GARANTIDA
        // Você pode trocar R.color.primary por qualquer cor do seu colors.xml
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(com.google.android.material.R.attr.colorOnSurface, getTheme()));

        // Botão Fechar no Header
        View headerView = binding.navHeader.getRoot();
        headerView.findViewById(R.id.btn_close_drawer).setOnClickListener(v ->
                binding.drawerLayout.closeDrawer(GravityCompat.START));

        // Botão Configurações no Footer
        binding.navFooter.btnFooterConfig.setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            navController.navigate(R.id.menu_configuracoes);
        });

        // Clique no Perfil (Footer)
        binding.navFooter.getRoot().setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            navController.navigate(R.id.nav_perfil);
        });

        updateNavHeader(currentUser);
    }

    public void applyBottomNavConfig() {
        if (tokenManager == null) return;
        boolean showLabels = tokenManager.shouldShowLabels();

        // Ajusta visibilidade dos textos
        binding.bottomNavigation.setLabelVisibilityMode(
                showLabels ? com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
                        : com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_UNLABELED
        );

        // Ajusta altura para ficar mais fino no modo compacto
        ViewGroup.LayoutParams params = binding.bottomNavigation.getLayoutParams();
        params.height = showLabels ? (int) (80 * getResources().getDisplayMetrics().density)
                : (int) (64 * getResources().getDisplayMetrics().density);
        binding.bottomNavigation.setLayoutParams(params);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        final int startId = AccessManager.getStartDestinationId(currentUser);

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        navGraph.setStartDestination(startId);
        navController.setGraph(navGraph);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == id) {
                return true;
            }

            // Animação de escala ao clicar
            View itemView = findViewById(id);
            if (itemView != null) {
                itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() ->
                        itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                ).start();
            }

            NavOptions options = new NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out)
                    .build();

            navController.navigate(id, null, options);
            return true;
        });

        restrictMenu(binding.navView.getMenu());
        restrictMenu(binding.bottomNavigation.getMenu());

        binding.bottomNavigation.setVisibility(View.VISIBLE);
    }

    private void updateMenuItemStyle(MenuItem item, int currentId) {
        if (item.hasSubMenu()) {
            Menu subMenu = item.getSubMenu();
            if (subMenu != null) {
                for (int i = 0; i < subMenu.size(); i++) {
                    updateMenuItemStyle(subMenu.getItem(i), currentId);
                }
            }
        } else {
            boolean isSelected = item.getItemId() == currentId;
            item.setChecked(isSelected);

            View actionView = item.getActionView();
            if (actionView instanceof ImageView) {
                actionView.setAlpha(isSelected ? 1.0f : 0.4f);
            }
        }
    }

    private void sincronizarDadosUsuario() {
        if (currentUser == null) return;

        RetrofitClient.getApiService(this).getUsuarioDetalhadoPorId(currentUser.getId()).enqueue(new Callback<UsuarioDetalhadoResponse>() {
            @Override
            public void onResponse(Call<UsuarioDetalhadoResponse> call, Response<UsuarioDetalhadoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentUser = response.body().getData();
                    tokenManager.saveUserData(currentUser);
                    updateNavHeader(currentUser);
                }
            }
            @Override
            public void onFailure(Call<UsuarioDetalhadoResponse> call, Throwable t) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
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
        if (navHostFragment != null && !navHostFragment.getChildFragmentManager().getFragments().isEmpty()) {
            Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (currentFragment instanceof NfcTagListener) {
                ((NfcTagListener) currentFragment).onTagRead(tagId);
            }
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private void restrictMenu(Menu menu) {
        if (menu == null) return;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.hasSubMenu()) {
                restrictMenu(item.getSubMenu());
                item.setVisible(hasVisibleChildren(item.getSubMenu()));
            } else {
                item.setVisible(AccessManager.isAllowedDestination(currentUser, item.getItemId()));
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
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void updateNavHeader(UsuarioDetalhado user) {
        if (user != null) {
            binding.navFooter.tvFooterNome.setText(user.getNome());
            binding.navFooter.tvFooterEmail.setText(user.getEmail());

            String inicial = (user.getNome() != null && !user.getNome().isEmpty())
                    ? user.getNome().substring(0, 1).toUpperCase() : "?";
            binding.navFooter.tvFooterInicial.setText(inicial);

            if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
                binding.navFooter.ivFooterFoto.setVisibility(View.VISIBLE);
                binding.navFooter.tvFooterInicial.setVisibility(View.GONE);

                Glide.with(this)
                        .load(user.getFotoPerfil())
                        .circleCrop()
                        .into(binding.navFooter.ivFooterFoto);
            } else {
                binding.navFooter.ivFooterFoto.setVisibility(View.GONE);
                binding.navFooter.tvFooterInicial.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        binding.drawerLayout.closeDrawer(GravityCompat.START);

        if (!AccessManager.isAllowedDestination(currentUser, id)) {
            ToastUtils.showInfo(this, "Seu acesso é restrito.");
            return true;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != id) {
                NavOptions options = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
                        .setLaunchSingleTop(true).setRestoreState(true).build();
                navController.navigate(id, null, options);
            }
        }, 280);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) binding.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

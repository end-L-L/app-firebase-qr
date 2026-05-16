package com.curso.asistenciaqr.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.AuthRepository;
import com.curso.asistenciaqr.repository.PreferenciasRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout     drawerLayout;
    private NavController    navController;
    private NavigationView   navView;
    private MaterialToolbar  toolbar;
    private AppBarConfiguration appBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String tema = new PreferenciasRepository(this).obtenerTema();
        if ("light".equals(tema)) {
            setTheme(R.style.Theme_AsistenciaQR_Light);
        } else {
            setTheme(R.style.Theme_AsistenciaQR_Dark);
        }

        setContentView(R.layout.activity_main);

        toolbar      = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toolbar.setVisibility(View.GONE);

        // ✅ Listener fijo — se define UNA sola vez aquí, nunca en configurarMenuPorRol()
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                new AuthRepository().logout();
                new PreferenciasRepository(this).limpiarRol(); // ← limpiar rol
                navView.getMenu().clear();
                drawerLayout.closeDrawers();
                navController.navigate(R.id.action_global_to_login);
                return true;
            }
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) drawerLayout.closeDrawers();
            return handled;
        });

        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            int id = dest.getId();
            if (id == R.id.loginFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbar.setVisibility(View.GONE);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toolbar.setVisibility(View.VISIBLE);
            }
        });

        // ✅ Si venimos de recreate() y había un rol guardado, restaurar menú y AppBarConfig
        String rolGuardado = new PreferenciasRepository(this).obtenerRol();
        if (rolGuardado != null) {
            configurarMenuPorRol(rolGuardado);
        }
    }

    public void configurarMenuPorRol(String rol) {
        navView.getMenu().clear();

        if ("maestro".equals(rol)) {
            navView.inflateMenu(R.menu.menu_drawer_maestro);
            appBarConfig = new AppBarConfiguration.Builder(
                    R.id.dashboardMaestroFragment,
                    R.id.ajustesFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
        } else {
            navView.inflateMenu(R.menu.menu_drawer_alumno);
            appBarConfig = new AppBarConfiguration.Builder(
                    R.id.dashboardAlumnoFragment,
                    R.id.ajustesFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
        }

        // ✅ Solo reconectar Toolbar — sin tocar el listener del navView
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig)
                || super.onSupportNavigateUp();
    }
}
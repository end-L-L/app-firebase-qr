package com.curso.asistenciaqr.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.PreferenciasRepository;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout   drawerLayout;
    private NavController  navController;
    private NavigationView navView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PreferenciasRepository(this).aplicarTema();
        setContentView(R.layout.activity_main);

        toolbar      = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Inicialmente bloquear drawer (estamos en Login)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toolbar.setVisibility(View.GONE);

        // 1. Crear una configuración base (puedes incluir los destinos principales aquí)
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardMaestroFragment, R.id.dashboardAlumnoFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        // 2. Vincular Toolbar con NavController
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            int id = dest.getId();

            if (id == R.id.loginFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbar.setVisibility(View.GONE);

            } else if (id == R.id.dashboardMaestroFragment ||
                    id == R.id.dashboardAlumnoFragment  ||
                    id == R.id.crearSesionFragment      ||
                    id == R.id.qrDisplayFragment        ||
                    id == R.id.listaAsistenciaFragment  ||
                    id == R.id.scannerQrFragment        ||
                    id == R.id.confirmacionFragment     ||
                    id == R.id.ajustesFragment) {

                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toolbar.setVisibility(View.VISIBLE);
            }
        });
    }

    // Llamado desde LoginFragment tras verificar el rol
    public void configurarMenuPorRol(String rol) {
        navView.getMenu().clear();

        if ("maestro".equals(rol)) {
            navView.inflateMenu(R.menu.menu_drawer_maestro);
        } else {
            navView.inflateMenu(R.menu.menu_drawer_alumno);
        }

        // Definir top-level destinations según el rol
        AppBarConfiguration appBarConfig;
        if ("maestro".equals(rol)) {
            appBarConfig = new AppBarConfiguration.Builder(
                    R.id.dashboardMaestroFragment,
                    R.id.ajustesFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
        } else {
            appBarConfig = new AppBarConfiguration.Builder(
                    R.id.dashboardAlumnoFragment,
                    R.id.ajustesFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
        }

        // ✅ Reconectar ambos con el NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Esto permite que el NavController maneje el clic en el icono de la hamburguesa/atrás
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp();
    }
}
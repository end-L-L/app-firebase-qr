package com.curso.asistenciaqr.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.curso.asistenciaqr.repository.AuthRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.PreferenciasRepository;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout   drawerLayout;
    private NavController  navController;
    private NavigationView navView;
    private MaterialToolbar toolbar;

    private static int destinoActual = -1; // ← variable estática que sobrevive al recreate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // aplicar tema ANTES de inflar el layout
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

        // inicialmente bloquear drawer (estamos en Login)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toolbar.setVisibility(View.GONE);

        // 1. crear una configuración base (puedes incluir los destinos principales aquí)
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardMaestroFragment, R.id.dashboardAlumnoFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        // 2. vincular Toolbar con NavController
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            destinoActual = dest.getId(); // ← guardar destino actual
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
        // si venimos de un recreate y hay sesión activa, restaurar destino
        if (savedInstanceState != null && destinoActual != -1
                && destinoActual != R.id.loginFragment) {
            navController.navigate(destinoActual);
        }
    }

    // llamado desde LoginFragment tras verificar el rol
    public void configurarMenuPorRol(String rol) {
        navView.getMenu().clear();

        if ("maestro".equals(rol)) {
            navView.inflateMenu(R.menu.menu_drawer_maestro);
        } else {
            navView.inflateMenu(R.menu.menu_drawer_alumno);
        }

        // definir top-level destinations según el rol
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

        // reconectar ambos con el NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                new AuthRepository().logout();
                navView.getMenu().clear();
                drawerLayout.closeDrawers();
                Navigation.findNavController(
                                findViewById(R.id.nav_host_fragment))
                        .navigate(R.id.action_global_to_login);
                return true;
            }
            // dejar que NavigationUI maneje el resto
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) drawerLayout.closeDrawers();
            return handled;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // esto permite que el NavController maneje el clic en el icono de la hamburguesa/atrás
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp();
    }
}
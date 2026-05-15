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

    private DrawerLayout drawerLayout;
    private NavController navController;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aplicar tema guardado antes de inflar
        new PreferenciasRepository(this).aplicarTema();

        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Pantallas top-level por rol — muestran ☰ en lugar de ←
        AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(
                R.id.dashboardMaestroFragment,
                R.id.dashboardAlumnoFragment,
                R.id.loginFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
        NavigationUI.setupWithNavController(navView, navController);

        // Control de visibilidad del Drawer según destino
        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            int id = dest.getId();
            if (id == R.id.loginFragment) {
                // Login: sin drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbar.setVisibility(View.GONE);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toolbar.setVisibility(View.VISIBLE);
            }
        });
    }

    // Llamado desde LoginFragment tras verificar el rol
    public void configurarMenuPorRol(String rol) {
        if ("maestro".equals(rol)) {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.menu_drawer_maestro);
        } else {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.menu_drawer_alumno);
        }
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout)
                || super.onSupportNavigateUp();
    }
}
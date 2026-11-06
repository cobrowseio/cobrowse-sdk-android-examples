package io.cobrowse.standalone;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.activity_main_drawer_layout);
        NavigationView navigationView = findViewById(R.id.activity_main_nav_view);
        AppBarLayout appBar = findViewById(R.id.appbar);
        View content = findViewById(R.id.layout_main);

        // Apps targeting API 35+ are always drawn edge-to-edge on Android 15+
        ViewCompat.setOnApplyWindowInsetsListener(drawer, (v, windowInsets) -> {
            Insets systemBarsInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            navigationView.setPadding(systemBarsInsets.left, 0, systemBarsInsets.right, systemBarsInsets.bottom);
            appBar.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0);
            content.setPadding(systemBarsInsets.left, 0, systemBarsInsets.right, systemBarsInsets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.layout_main_nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
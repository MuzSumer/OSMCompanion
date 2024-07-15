package expose.osm;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.views.MapView;

import osm.expose.R;

public class AppWelcome extends AppCompatActivity {

    private final int ACCOUNT_PICKER_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        checkPermissions();
        buildUI();

        //Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        //startActivityForResult(intent, ACCOUNT_PICKER_REQ_CODE);
    }


    private void buildUI() {
        setContentView(R.layout.system_coordinator);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView navigator = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navigator, navController);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == ACCOUNT_PICKER_REQ_CODE && resultCode == RESULT_OK) {

            String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            buildUI();
        }
    }

    @Override
    public void onBackPressed() {
        boolean back = false;

        if (back) super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
    }


    @SuppressLint("InlinedApi")
    private void checkPermissions() {


        boolean camera_access = noPermission(Manifest.permission.CAMERA);
        boolean fine_location = noPermission(Manifest.permission.ACCESS_FINE_LOCATION);


        if (camera_access || fine_location) {
            Toast.makeText(getApplicationContext(), "Camera and Location Access required", Toast.LENGTH_LONG).show();
        }


        if (camera_access && fine_location) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }



        if (camera_access && !fine_location) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);

        }

        if (fine_location && !camera_access) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }


        /*
        boolean coarse_location = noPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (coarse_location) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
         */







        /*

        boolean background_location = noPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if (background_location) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);

        }
         */




        /*
        String[] perms = {"android.permission.FINE_LOCATION"};
        requestPermissions(perms, 0);

        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
         */

    }

    private boolean noPermission(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_expose, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);
        /*

        MenuItem mode = menu.findItem(R.id.mode_switch);
        if (mode != null) {

            mode.setChecked(true);
            if (mode.isChecked()) message("on");
            else message("off");
        }

        mode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                beep();
                return false;
            }
        });
         */
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            MapView map = findViewById(R.id.map);
            if (map != null) {

                MenuItem item = menu.findItem(R.id.map_scale_tiles);
                item.setChecked(map.isTilesScaledToDpi());

                item = menu.findItem(R.id.map_replicate_vertically);
                item.setChecked(map.isVerticalMapRepetitionEnabled());

                item = menu.findItem(R.id.map_replicate_horizontally);
                item.setChecked(map.isHorizontalMapRepetitionEnabled());

            }



        } catch (Exception e) {}

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        // show prefs
        if (id == R.id.action_prefs) {

            startActivity(new Intent(this, AppSettings.class));
            return true;
        }

        // show about
        if (id == R.id.action_about) {

            startActivity(new Intent(this, AppAbout.class));
            return true;
        }



        MapView map = findViewById(R.id.map);
        if (map != null) {
            if (id == R.id.map_scale_tiles) {

                boolean value = !map.isTilesScaledToDpi();
                map.setTilesScaledToDpi(value);
                map.invalidate();

                return true;
            }

            if (id == R.id.map_replicate_vertically) {
                boolean value = !map.isVerticalMapRepetitionEnabled();
                map.setVerticalMapRepetitionEnabled(value);
                map.invalidate();

                return true;
            }
            if (id == R.id.map_replicate_horizontally) {
                boolean value = !map.isHorizontalMapRepetitionEnabled();
                map.setHorizontalMapRepetitionEnabled(value);
                map.invalidate();

                return true;
            }

            if (id == R.id.map_rotate_clockwise) {
                float currentRotation = map.getMapOrientation() + 10;
                if (currentRotation > 360)
                    currentRotation = currentRotation - 360;
                map.setMapOrientation(currentRotation, true);

                return true;
            }
            if (id == R.id.map_rotate_counterclockwise) {
                float currentRotation = map.getMapOrientation() - 10;
                if (currentRotation < 0)
                    currentRotation = currentRotation + 360;
                map.setMapOrientation(currentRotation, true);

                return true;
            }
        }


        return super.onOptionsItemSelected(item);
    }//menu

}
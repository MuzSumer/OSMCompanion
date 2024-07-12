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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

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
            MenuItem item = menu.findItem(R.id.map_scale_tiles);
            item.setChecked(true);

            item = menu.findItem(R.id.map_replicate_vertically);
            item.setChecked(true);

            item = menu.findItem(R.id.map_replicate_horizontally);
            item.setChecked(true);



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

        // TODO

        if (id == R.id.map_scale_tiles) {}

        if (id == R.id.map_replicate_vertically) {}
        if (id == R.id.map_replicate_horizontally) {}

        if (id == R.id.map_rotate_clockwise) {}
        if (id == R.id.map_rotate_counterclockwise) {}

        return super.onOptionsItemSelected(item);
    }//menu


    private String createMap(double latitude, double longitude) {
        String map = "<HTML>";

        map += "<HEADER>";
        map += "<TITLE>Map | Geo City</TITLE>";

        map += "<script type='text/javascript' src='https://openlayers.org/api/OpenLayers.js'></script>";
        map += "<script type='text/javascript' src='https://openstreetmap.org/openlayers/OpenStreetMap.js'></script>";
        map += "<script type='text/javascript' src='tom.js'></script>";

        map += "<SCRIPT type='text/javascript'>";



        /*
        function drawmap() {
    // Popup und Popuptext mit evtl. Grafik
    var popuptext="<font color=\"black\"><b>Thomas Heiles<br>Stra&szlig;e 123<br>54290 Trier</b><p><img src=\"test.jpg\" width=\"180\" height=\"113\"></p></font>";

    OpenLayers.Lang.setCode('de');

    // Position und Zoomstufe der Karte
    var lon = 6.641389;
    var lat = 49.756667;
    var zoom = 7;

    map = new OpenLayers.Map('map', {
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.PanZoomBar()],
        maxExtent:
            new OpenLayers.Bounds(-20037508.34,-20037508.34,
                                    20037508.34, 20037508.34),
        numZoomLevels: 18,
        maxResolution: 156543,
        units: 'meters'
    });

    layer_mapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
    layer_markers = new OpenLayers.Layer.Markers("Address", { projection: new OpenLayers.Projection("EPSG:4326"),
    	                                          visibility: true, displayInLayerSwitcher: false });

    map.addLayers([layer_mapnik, layer_markers]);
    jumpTo(lon, lat, zoom);

    // Position des Markers
    addMarker(layer_markers, 6.641389, 49.756667, popuptext);

}
         */





        map += "</SCRIPT>";

        map += "</HEADER>";

        map += "<BODY onload=drawmap();>";

        map += "<div id='header'>";
        map += "<div id='content'>Map</div>";

        map += "<div id='osm'>© <a href='https://www.openstreetmap.org'>OpenStreetMap</a>\n" +
                "     <a href='https://www.openstreetmap.org/copyright'>Mitwirkende</a>,\n" +
                "     <a href='https://opendatacommons.org/licenses/odbl/'>ODbL</a>\n" +
                "   </div>";


        map += "</div>";

        map += "<div id='map'></div>";
        /*
        <div id="header">
   <div id="content">Karte (Testversion)</div>
   <div id="osm">© <a href="https://www.openstreetmap.org">OpenStreetMap</a>
     <a href="https://www.openstreetmap.org/copyright">Mitwirkende</a>,
     <a href="https://opendatacommons.org/licenses/odbl/">ODbL</a>
   </div>
  </div>
  <div id="map">
  </div>
         */




        map += "</BODY>";

        map += "</HTML>";


        return map;
    }

}
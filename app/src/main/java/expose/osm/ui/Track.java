package expose.osm.ui;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import expose.osm.AppAbout;
import osm.expose.R;
import expose.model.DiagramUtil;
import expose.model.dialog.EditorMap;
import expose.model.dialog.EditorProperties;
import expose.model.dialog.RemoveMany;
import expose.model.impl.DiagramExpose;
import expose.model.impl.DiagramStore;
import expose.model.meta.Command;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;

public class Track extends AppCompatActivity implements Command, LocationListener, TextToSpeech.OnInitListener {


    DiagramExpose expo;



    long interval = SECONDS.toMillis(1);

    private FusedLocationProviderClient fusedLocationClient;
    double longitude, latitude;
    double p_longitude, p_latitude;


    private static final int IMAGE_CAPTURE = 0;
    private final String camname = "cam.jpg";

    double trigger;

    MapView map;
    final ArrayList<OverlayItem> markers = new ArrayList<>();



    boolean preview = true;



    private void addOverlays() {
        // copyright
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getApplicationContext());
        copyrightOverlay.setTextSize(10);

        map.getOverlays().add(copyrightOverlay);



        // location
        MyLocationNewOverlay location = new MyLocationNewOverlay(map);
        location.setEnableAutoStop(false);
        //location.enableFollowLocation();
        location.enableMyLocation();

        map.getOverlayManager().add(location);


        // minimap
        final MinimapOverlay miniMapOverlay = new MinimapOverlay(getApplicationContext(), map.getTileRequestCompleteHandler());
        map.getOverlays().add(miniMapOverlay);


        // bookmarks
        addMarkers(expo());
        ItemizedIconOverlay<OverlayItem> marks = new ItemizedIconOverlay<>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        expo().setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {

                        expo().setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        return true;
                    }
                }, getApplicationContext());

        map.getOverlays().add(marks);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String s = preferences.getString("sensitivity", "10");
        trigger = 0.001 * Integer.parseInt(s);


        String name = getIntent().getStringExtra("name");
        String folder = getIntent().getStringExtra("folder");

        setContentView(R.layout.diagram_with_map);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(name);


        expo = new DiagramExpose(getApplicationContext(), findViewById(R.id.diagram), findViewById(R.id.scroll));
        Store store = new DiagramStore(expo(), "track.xml");
        expo().createStore(store, "track.xml", folder);


        LinearLayout layout = findViewById(R.id.mapview);


        map = new MapView(getApplicationContext());
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTileProvider(new MapTileProviderBasic(getApplicationContext()));


        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);



        RelativeLayout.LayoutParams fitscreen = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(map, fitscreen);




        addOverlays();




        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        expo().getDiagram().setLayoutManager(manager);

        ModelAdapter adapter = new ModelAdapter(getApplicationContext());
        expo().getDiagram().setAdapter(adapter);


        startLocationUpdates();

        getLocation();



        showPreview(latitude, longitude);

        registerActions();

        tts = new TextToSpeech(this, this);
    }//onCreate






    private void addMarkers(DiagramExpose expose) {

        for (int p=0; p<expose.getStore().size(); p++) {
            UniversalModel model = expose.getStore().getModelAt(p);

            String[] words = model.getCoordinates().split("/");

            double lat = Double.parseDouble(words[0]);
            double lon = Double.parseDouble(words[1]);

            markers.add(new OverlayItem(model.getId(), model.getTitle(), model.getSubject(),
                    new GeoPoint(lat, lon)));

        }

    }




    TextToSpeech tts;
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.GERMAN);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), "error tts", Toast.LENGTH_LONG).show();
            }

        }
    }


    private void speak(String subject) {

        tts.speak(subject, TextToSpeech.QUEUE_FLUSH, null, null);

    }


    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {

        longitude = location.getLongitude();
        latitude = location.getLatitude();


        double d = DiagramUtil.distanceInKilometers(latitude, longitude, p_latitude, p_longitude);

        if (d > 0.005) {


            showPreview(latitude, longitude);

            DecimalFormat df = new DecimalFormat("000.0000");

            Toast.makeText(getApplicationContext(), "° " + df.format(d), Toast.LENGTH_SHORT).show();

            p_longitude = longitude;
            p_latitude = latitude;
        }


    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        // get location
        CurrentLocationRequest r = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setDurationMillis(1000)
                .setMaxUpdateAgeMillis(0)
                .build();

        fusedLocationClient.getCurrentLocation(r, null);

        fusedLocationClient.getCurrentLocation(r, null)
                .addOnSuccessListener(Track.this, location -> {
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                });
    }



    private void showPreview(UniversalModel model) {
        if (model == null) return;

        String[] words = model.getCoordinates().split("/");

        double lat = Double.parseDouble(words[0]);
        double lon = Double.parseDouble(words[1]);

        showPreview(lat, lon);
    }

    private void showPreview(double plat, double plon) {



        map.getController().setZoom(15.2);
        map.getController().animateTo(new GeoPoint(plat, plon));

    }




    public DiagramExpose expo() { return expo; }
    public void addModel(UniversalModel model) {

        // test new model
        for (UniversalModel m : expo().getStore().getModels()) {

            double d = DiagramUtil.distanceInKilometers(model.getCoordinates(), m.getCoordinates());
            if (d < trigger) {

                Toast.makeText(getApplicationContext(), "schon vorhanden", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        expo().getStore().addModel(model);
        expo().getStore().saveLocalModel(expo(), expo().getFolder());

        expo().setFocus(model.getId(), false);
        expo().scrollToEnd();

        speak(model.getSubject());
    }

    public void removeModel(String id) {}







    public View.OnClickListener selectCell() { return cellSelect; }


    public View.OnClickListener openCell() { return cellOpen; }


    public View.OnClickListener editCell() { return cellEdit; }



    private final View.OnClickListener cellSelect = view -> {
        String id = view.getContentDescription().toString();

        expo().setFocus(id, false);

        UniversalModel m = expo().getStore().findModel(id);

        showPreview(m);

        speak(m.getSubject());
    };

    private final View.OnClickListener cellEdit = view -> {
        String id = view.getContentDescription().toString();
        expo().setFocus(id, false);

        UniversalModel model = expo().getStore().findModel(id);
        showPreview(model);

        EditorProperties editor = new EditorProperties(expo(),null, null, model);
        editor.show(getSupportFragmentManager(), "");
    };

    private final View.OnClickListener cellOpen = view -> {
        String id = view.getContentDescription().toString();

        if (!expo().getSelected().equals(id)) {

            expo().setFocus(id, false);

            UniversalModel m = expo().getStore().findModel(id);

            showPreview(m);


            speak(m.getSubject());

            return;
        }



        Intent intent = new Intent(Track.this, ViewPlace.class);

        intent.putExtra("namespace", expo().getNamespace());
        intent.putExtra("folder", expo().getFolder());
        intent.putExtra("id", id);


        startActivity(intent);
    };


    private void registerActions() {
        findViewById(R.id.record_add).setOnClickListener(
                v -> {

                    getLocation();

                    double la = latitude;
                    double lo = longitude;

                    UniversalModel m = expo().getSelectedModel();
                    if (m != null) {
                        String coords = m.getCoordinates();
                        String[] words = coords.split("/");

                        la = Double.parseDouble(words[0]);
                        lo = Double.parseDouble(words[1]);
                    }

                    EditorMap editor = new EditorMap(this, la, lo, markers);
                    editor.show(getSupportFragmentManager(), "locate");
                }
        );

        findViewById(R.id.record_remove).setOnClickListener(
                v -> {

                    RemoveMany editor = new RemoveMany(expo());
                    editor.show(getSupportFragmentManager(), "remove");



                    /*
                    getDiagram().getStore().close();
                    getDiagram().getStore().saveLocalModel(expo, expo.getFolder());
                    getDiagram().redraw(true);

                     */

                }
        );


        findViewById(R.id.record_search).setOnClickListener(
                v -> {

                    if (!expo().getSelected().isEmpty()) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, IMAGE_CAPTURE);
                    }

                }
        );

        findViewById(R.id.record_share).setOnClickListener(
                v -> {

                    if (preview) {
                        preview = false;
                        findViewById(R.id.mapview).setVisibility(View.GONE);
                    } else {
                        preview = true;
                        findViewById(R.id.mapview).setVisibility(View.VISIBLE);
                    }

                }
        );


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {


                Bitmap bitmap = (Bitmap) data.getExtras().get("data");


                try {

                    FileOutputStream out = getApplicationContext().openFileOutput(camname, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    out.flush();
                    out.close();


                    saveSnapshot();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveSnapshot() {
        String image_id = expo().getSelected();
        String image_name = image_id + ".jpg";

        try {
            // folder
            File home = new File(getApplicationContext().getFilesDir(), expo().getFolder());
            if (!home.exists()) {
                home.mkdirs();
            }

            // save snapshot
            File snapshot = new File(home, image_name);
            FileOutputStream output = new FileOutputStream(snapshot);


            FileInputStream input = getApplicationContext().openFileInput(camname);


            FileChannel in = input.getChannel();
            FileChannel out = output.getChannel();

            out.transferFrom(in, 0, in.size());
            out.close();

            ExifInterface exif = new ExifInterface(snapshot);
            in.close();


            exif.setLatLong(latitude, longitude);


            // create model
            UniversalModel model = expo().getSelectedModel();

            model.setSymbol(image_name);
            model.setContent(image_name);

            expo().getStore().saveLocalModel(expo(), expo().getFolder());
            expo().redraw(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }











    class ModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            UniversalModel model = expo().getStore().getModelAt(position);
            String id = model.getId();

            DiagramExpose.UniversalModelViewHolder mv = (DiagramExpose.UniversalModelViewHolder) holder;


            mv.itemView.setContentDescription(id);
            mv.itemView.setOnClickListener(openCell());



            ConstraintLayout layout = mv.getLayout();
            layout.setContentDescription(id);


            // hovering
            /*
            mv.getLayout().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String id1 = v.getContentDescription().toString();

                    message(id);
                }
            });

            mv.getLayout().setOnHoverListener((view1, event) -> {

                String id1 = view1.getContentDescription().toString();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        message("enter " + id1);
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        message("move " + id1);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        message("exit " + id1);
                        break;
                }
                return false;
            });
             */


            {
                if (id.equals(expo().getSelected())) {
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.app_rbox_selected));
                    registerForContextMenu(mv.getImage());
                } else {
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.app_rbox_background));
                    unregisterForContextMenu(mv.getImage());
                }
            }// selection


            {
                if (id.equals(expo().getSelected())) {
                    layout.post(() -> {
                        int l = layout.getMeasuredWidth()*3/5;
                        // select
                        Drawable d = getApplicationContext().getDrawable(R.drawable.app_dot_green);
                        int t = 23;
                        DiagramUtil.setDBounds(d, 16, l, t);

                        // edit
                        Drawable e = getApplicationContext().getDrawable(R.drawable.app_dot_blue);
                        t = 77;
                        DiagramUtil.setDBounds(e, 16, l, t);

                        // location
                        Drawable f = getApplicationContext().getDrawable(R.drawable.app_dot_red);
                        t = layout.getMeasuredHeight() - 23;
                        DiagramUtil.setDBounds(f, 16, l, t);


                        // image
                        Drawable g = getApplicationContext().getDrawable(R.drawable.app_dot_white);
                        l = mv.getImage().getMeasuredWidth()/2;
                        t = mv.itemView.getMeasuredHeight()/2;
                        DiagramUtil.setDBounds(g, 32, l, t);



                        layout.getOverlay().clear();
                        layout.getOverlay().add(d);
                        layout.getOverlay().add(e);
                        layout.getOverlay().add(f);
                        //layout.getOverlay().add(g);
                    });
                } else {
                    layout.post(() -> {
                        int l = layout.getMeasuredWidth()*3/5;

                        Drawable d = getApplicationContext().getDrawable(R.drawable.app_dot_yellow);
                        int t = 23;
                        DiagramUtil.setDBounds(d, 16, t, l);

                        layout.getOverlay().clear();
                        layout.getOverlay().add(d);
                        //layout.getOverlay().add(e);
                    });
                }
            }// overlay


            {
                mv.getDate().setText(model.getDate());
                mv.getDate().setContentDescription(id);
                mv.getDate().setOnClickListener(editCell());



                try {
                    mv.getType().setText(model.getType());
                    mv.getState().setText(model.getState());
                } catch (Exception e) {
                    mv.getType().setText(model.getType());
                    mv.getState().setText(model.getState());
                }


                mv.getType().setContentDescription(id);
                mv.getState().setContentDescription(id);

                mv.getType().setOnClickListener(editCell());
                mv.getState().setOnClickListener(editCell());

            }// date, type, state


            {
                mv.getTitle().setText(model.getTitle());
                mv.getTitle().setContentDescription(id);
                //mv.getTitle().setOnClickListener(selectCell());
                mv.getTitle().addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String id = mv.getTitle().getContentDescription().toString();
                        UniversalModel edit = expo().getStore().findModel(id);

                        edit.setTitle(DiagramUtil.trim(mv.getTitle()));
                        expo().getStore().saveLocalModel(expo(), expo().getFolder());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });


                mv.getSubject().setText(model.getSubject());
                mv.getSubject().setContentDescription(id);
                //mv.getSubject().setOnClickListener(selectCell());
                mv.getSubject().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String id = mv.getTitle().getContentDescription().toString();
                        UniversalModel edit = expo().getStore().findModel(id);

                        edit.setSubject(DiagramUtil.trim(mv.getSubject()));
                        expo().getStore().saveLocalModel(expo(), expo().getFolder());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }// title, subject


            {
                mv.getContent().setText(model.getContent());
                mv.getSpecs().setText(model.getSpecs());
                mv.getTags().setText(model.getTags());
            }// content, specs, tags


            {
                mv.getOpenLocation().setContentDescription(id);
                //mv.getOpenLocation().setOnClickListener(openMap());

                //mv.getLocation().setText(shortLocation(model.getLocation(), 1));
                mv.getLocation().setContentDescription(id);


                /*
                String l = model.getLocation();
                if (!l.isEmpty()) {
                    String words[] = model.getLocation().split(",");
                    for (int p=0; p<6; p++) {
                        if (p < words.length) {
                            if (l == "") {
                                l = words[p];
                            } else {
                                    l = "," + words[p];
                                }
                            }
                        }
                    }

                }
                 */

                String location = model.getLocation();

                // shor location
                if (!location.isEmpty()) {

                    int max = 64;

                    String l = "";

                    String[] words = location.split(",");
                    for (String w : words) {
                        if (l.isEmpty()) {
                            l = w;
                        } else {
                            if ((l.length() + w.length() + 1) < max) {
                                l = l + "," + w;
                            }
                        }
                    }

                    location = l;
                }
                mv.getLocation().setText(location);

                //mv.getLocation().setOnClickListener(wrongLocation());
            }// location



            {
                mv.getImage().setContentDescription(id);
                mv.getImage().setOnClickListener(openCell());

                expo().setImage(mv.getImage(), model.getSymbol(), getResources().getInteger(R.integer.cell_size_small));
            }// image

        }






        Context context;
        public ModelAdapter(Context classContext) {
            context = classContext;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return expo().createViewHolder(LayoutInflater.from(context).inflate(R.layout.diagram_topic, parent, false));
        }

        @Override
        public int getItemCount() {
            return expo().getStore().size();
        }


    }//ModelAdapter



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);


        //menu.setHeaderTitle("move");
        //menu.setHeaderIcon(R.drawable.ic_error);


        menu.add(0, v.getId(), 0, "up");
        menu.add(0, v.getId(), 0, "down");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        UniversalModel source = expo().getSelectedModel();
        String selected = source.getId();

        if (item.getTitle() == "up") {

            int p0 = findPosition(selected);

            int p1 = p0 - 1;

            if (p1 < 0) {
                return false;
            }


            UniversalModel older = expo().getStore().getModelAt(p1);
            expo().getStore().swapModel(source, older);

            expo().getStore().saveLocalModel(expo(), expo().getFolder());
            expo().setFocus(null, false);
        }

        if (item.getTitle() == "down") {

            int p0 = findPosition(selected);

            int p1 = p0 + 1;

            if (p1 > expo().getStore().size() - 1) {
                return false;
            }


            UniversalModel younger = expo().getStore().getModelAt(p1);
            expo().getStore().swapModel(source, younger);

            expo().getStore().saveLocalModel(expo(), expo().getFolder());
            expo().setFocus(null, false);
        }



        return true;
    }

    private int findPosition(String id) {
        int found = -1;

        for (int p = 0; p< expo().getStore().size(); p++) {
            UniversalModel model = expo().getStore().getModelAt(p);

            if (model.getId().equals(id)) {

                if (found < 0) { // find first
                    found = p;
                }
            }
        }

        return found;
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        p_longitude = 0*longitude;
                        p_latitude = 0*latitude;
                    }
                });



        int quality = LocationRequest.PRIORITY_HIGH_ACCURACY;

        // load interval
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String i = preferences.getString("interval", "1");
        if (i.matches("[-+]?\\d*\\.?\\d+")) {
            interval = SECONDS.toMillis(Integer.parseInt(i));
        }

        LocationRequest locationRequest = new LocationRequest.Builder(quality, interval).build();



        fusedLocationClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(this);
    }




    Menu app_menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track, menu);

        app_menu = menu;

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        // show about
        if (id == R.id.action_about) {

            startActivity(new Intent(this, AppAbout.class));
            return true;
        }



        return super.onOptionsItemSelected(item);
    }//menu


}

package expose.osm.ui;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

public class Places extends Fragment implements Command, LocationListener, TextToSpeech.OnInitListener {


    private FusedLocationProviderClient fusedLocationClient;
    double longitude, latitude;
    double p_longitude, p_latitude;


    long interval;
    double trigger;


    DiagramExpose expo;

    MapView map;

    final ArrayList<OverlayItem> markers = new ArrayList<>();


    boolean preview = true;

    private static final int IMAGE_CAPTURE = 0;
    private final String CAM_SHOT = "cam.jpg";


    TextToSpeech tts;
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.GERMAN);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getContext(), "error tts", Toast.LENGTH_LONG).show();
            }

        }
    }



    private void addOverlays() {
        // copyright
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getActivity());
        copyrightOverlay.setTextSize(10);

        map.getOverlays().add(copyrightOverlay);



        // location
        MyLocationNewOverlay location = new MyLocationNewOverlay(map);
        location.setEnableAutoStop(false);
        //location.enableFollowLocation();
        location.enableMyLocation();

        map.getOverlayManager().add(location);



        // minimap
        final MinimapOverlay miniMapOverlay = new MinimapOverlay(getActivity(), map.getTileRequestCompleteHandler());
        map.getOverlays().add(miniMapOverlay);



        // bookmarks
        addMarkers(expo());
        ItemizedIconOverlay<OverlayItem> marks = new ItemizedIconOverlay<>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        expo().setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        map.getController().animateTo(new GeoPoint(item.getPoint().getLatitude(), item.getPoint().getLongitude()));
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {

                        expo().setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        map.getController().animateTo(new GeoPoint(item.getPoint().getLatitude(), item.getPoint().getLongitude()));
                        return true;
                    }
                }, getActivity());

        map.getOverlays().add(marks);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /*
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
         */


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // setup
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        String i = preferences.getString("interval", "10");
        interval = SECONDS.toMillis(Integer.parseInt(i));

        String s = preferences.getString("sensitivity", "10");
        trigger = 0.001 * Integer.parseInt(s);



        tts = new TextToSpeech(getContext(), this);


    }//onCreate


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.diagram_with_map, container, false);


        expo = new DiagramExpose(getContext(), view.findViewById(R.id.diagram), view.findViewById(R.id.scroll));

        Store store = new DiagramStore(expo(),"places.xml");
        expo().createStore(store, "places.xml", "");


        LinearLayout layout = view.findViewById(R.id.mapview);


        map = new MapView(getActivity());
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setTileProvider(new MapTileProviderBasic(getActivity()));


        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);



        RelativeLayout.LayoutParams fitscreen = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(map, fitscreen);



        addOverlays();


        registerActions(view);



        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        expo().getDiagram().setLayoutManager(manager);

        ModelAdapter adapter = new ModelAdapter(expo().getContext());
        expo().getDiagram().setAdapter(adapter);

        return view;
    }


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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startLocationUpdates();

        getLocation();

        showPreview(latitude, longitude);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();

        map = null;

        tts.stop();
        tts.shutdown();
    }


    @Override
    public void onPause() {
        if (map != null) {
            map.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }



    public DiagramExpose expo() { return expo; }
    public void addModel(UniversalModel model) {

        // test new model
        for (UniversalModel m : expo().getStore().getModels()) {

            double d = DiagramUtil.distanceInKilometers(model.getCoordinates(), m.getCoordinates());
            if (d < trigger) {

                Toast.makeText(getContext(), "schon vorhanden", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        expo().getStore().addModel(model);
        expo().setFocus(model.getId(), false);
        expo().scrollToEnd();



        expo().getStore().saveLocalModel(expo(), expo().getFolder());
        speak(model.getSubject());

    }
    public void removeModel(String id) {}


    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {

        longitude = location.getLongitude();
        latitude = location.getLatitude();


        double d = DiagramUtil.distanceInKilometers(latitude, longitude, p_latitude, p_longitude);


        if (d > trigger) {


            //showPreview(latitude, longitude);

            testPlaces();


            p_longitude = longitude;
            p_latitude = latitude;
        }


    }


    private void showPreview(UniversalModel model) {
        if (model == null) return;

        String[] words = model.getCoordinates().split("/");

        double lat = Double.parseDouble(words[0]);
        double lon = Double.parseDouble(words[1]);

        showPreview(lat, lon);
    }

    private void showPreview(double plat, double plon) {

        /*
        DecimalFormat df = new DecimalFormat("000.0000");

        String lon = df.format(plon).replace(",", ".");
        String lat = df.format(plat).replace(",", ".");
         */


        map.getController().setZoom(15.2);
        map.getController().animateTo(new GeoPoint(plat, plon));



/*
        String url;

        url = String.format("https://www.openstreetmap.org/#map=" + Integer.toString(16) + "/" + lat + "/" + lon);
        wv.loadUrl(url);

 */

    }

    private void testPlaces() {

        for (UniversalModel m : expo().getStore().getModels()) {

            String coords = m.getCoordinates();
            String[] words = coords.split("/");

            double mlat = Double.parseDouble(words[0]);
            double mlon = Double.parseDouble(words[1]);


            double d = DiagramUtil.distanceInKilometers(latitude, longitude, mlat, mlon);

            if (d < trigger) {

                expo().setFocus(m.getId(), false);


                showPreview(m);

                String subject = m.getSubject();

                speak(subject);

                Toast.makeText(getContext(), subject, Toast.LENGTH_SHORT).show();

            }

        }
    }


    private void speak(String subject) {

        tts.speak(subject, TextToSpeech.QUEUE_FLUSH, null, null);

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
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                });
    }








    public View.OnClickListener selectCell() { return cellSelect; }


    public View.OnClickListener openCell() { return cellOpen; }


    public View.OnClickListener editCell() { return cellEdit; }



    private final View.OnClickListener cellSelect = view -> {
        String modelId = view.getContentDescription().toString();

        //getDiagram().setSelected(modelId);

        expo().setFocus(modelId, false);

        UniversalModel m = expo().getStore().findModel(modelId);



        showPreview(m);

        speak(m.getSubject());

    };

    private final View.OnClickListener cellEdit = view -> {
        String id = view.getContentDescription().toString();


        expo().setFocus(id, false);

        UniversalModel model = expo().getStore().findModel(id);

        showPreview(model);


        EditorProperties editor = new EditorProperties(expo(),null, null, model);
        editor.show(getChildFragmentManager(), "");
    };

    private final View.OnClickListener cellOpen = view -> {
        String id = view.getContentDescription().toString();

        if (!expo().getSelected().equals(id)) {
            //getDiagram().setSelected(id);
            expo().setFocus(id, false);

            UniversalModel model = expo().getStore().findModel(id);
            String subject = model.getSubject();

            speak(subject);

            showPreview(model);


            return;
        }


        Intent intent = new Intent(getActivity(), ViewPlace.class);
        intent.putExtra("namespace", expo().getNamespace());
        intent.putExtra("folder", expo().getFolder());
        intent.putExtra("id", id);

        startActivity(intent);
    };







    private void registerActions(View view) {
        view.findViewById(R.id.record_add).setOnClickListener(
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
                    editor.show(getChildFragmentManager(), "locate");

                }
        );

        view.findViewById(R.id.record_remove).setOnClickListener(
                v -> {
                    RemoveMany editor = new RemoveMany(expo());
                    editor.show(getChildFragmentManager(), "remove");
                }
        );


        view.findViewById(R.id.record_share).setOnClickListener(
                v -> {

                    if (preview) {
                        preview = false;
                        view.findViewById(R.id.mapview).setVisibility(View.GONE);
                    } else {
                        preview = true;
                        view.findViewById(R.id.mapview).setVisibility(View.VISIBLE);
                    }


                    // TODO remove
                    if (expo().getStore().size() < 2) return;

                    UniversalModel m0 = expo().getStore().getModelAt(0);
                    UniversalModel m1 = expo().getStore().getModelAt(1);

                    double d = DiagramUtil.distanceInKilometers(m0.getCoordinates(), m1.getCoordinates());

                    DecimalFormat df = new DecimalFormat("00.000");
                    String km = df.format(d).replace(",", ".");


                    Toast.makeText(getContext(), km + " km", Toast.LENGTH_SHORT).show();
                }
        );


        view.findViewById(R.id.record_search).setOnClickListener(
                v -> {

                    if (!expo().getSelected().isEmpty()) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, IMAGE_CAPTURE);
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

                    FileOutputStream out = getContext().openFileOutput(CAM_SHOT, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    out.flush();
                    out.close();


                    saveSnapshot();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(getContext(), "cancelled", Toast.LENGTH_SHORT).show();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    } // image capture

    private void saveSnapshot() {
        String image_id = expo().getSelected();
        String image_name = image_id + ".jpg";

        try {
            // folder
            File home = new File(getContext().getFilesDir(), expo().getFolder());
            if (!home.exists()) {
                home.mkdirs();
            }

            // save snapshot
            File snapshot = new File(home, image_name);
            FileOutputStream output = new FileOutputStream(snapshot);


            FileInputStream input = getContext().openFileInput(CAM_SHOT);


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

    } //saveSnapshot



    /*
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                    }
                }
            });
     */

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
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.app_rbox_selected));
                    registerForContextMenu(mv.getImage());
                } else {
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.app_rbox_background));
                    unregisterForContextMenu(mv.getImage());
                }
            }// selection


            {
                if (id.equals(expo().getSelected())) {
                    layout.post(() -> {
                        int l = layout.getMeasuredWidth()*3/5;
                        // select
                        Drawable d = getContext().getDrawable(R.drawable.app_dot_green);
                        int t = 23;
                        DiagramUtil.setDBounds(d, 16, l, t);

                        // edit
                        Drawable e = getContext().getDrawable(R.drawable.app_dot_blue);
                        t = 77;
                        DiagramUtil.setDBounds(e, 16, l, t);

                        // location
                        Drawable f = getContext().getDrawable(R.drawable.app_dot_red);
                        t = layout.getMeasuredHeight() - 23;
                        DiagramUtil.setDBounds(f, 16, l, t);


                        // image
                        Drawable g = getContext().getDrawable(R.drawable.app_dot_white);
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

                        Drawable d = getContext().getDrawable(R.drawable.app_dot_yellow);
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
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        p_longitude = 0*longitude;
                        p_latitude = 0*latitude;
                    }
                });



        LocationRequest locationRequest = new LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, interval).build();


        fusedLocationClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(this);
    }



}
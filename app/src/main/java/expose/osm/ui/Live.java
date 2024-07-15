package expose.osm.ui;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import expose.model.impl.DiagramExpose;
import expose.model.impl.DiagramStore;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;
import osm.expose.R;
import expose.model.DiagramUtil;

public class Live extends Fragment implements LocationListener, TextToSpeech.OnInitListener {


    long interval;
    double trigger;


    private FusedLocationProviderClient fusedLocationClient;
    double longitude, latitude;
    double p_longitude, p_latitude;


    String p_address;

    //WebView wv;
    MapView map;


    final ArrayList<OverlayItem> markers = new ArrayList<>();


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
    private void speak(String subject) {

        tts.speak(subject, TextToSpeech.QUEUE_FLUSH, null, null);

    }


    private void addOverlays() {

        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getActivity());
        copyrightOverlay.setTextSize(10);

        map.getOverlays().add(copyrightOverlay);



        MyLocationNewOverlay location = new MyLocationNewOverlay(map);
        location.setEnableAutoStop(false);
        location.enableFollowLocation();
        location.enableMyLocation();

        map.getOverlayManager().add(location);


        // minimap
        final MinimapOverlay miniMapOverlay = new MinimapOverlay(getActivity(), map.getTileRequestCompleteHandler());
        map.getOverlays().add(miniMapOverlay);


        // events
        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {


                // TODO
                return true;
            }
        });
        map.getOverlayManager().add(events);


        // rotation
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(final GeoPoint p) {

                return true;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        final RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(rotationGestureOverlay);



        // compass
        CompassOverlay overlay = new CompassOverlay(getContext(), map);
        overlay.setPointerMode(false);
        overlay.enableCompass();
        map.getOverlayManager().add(overlay);



        boolean marks = false;
        if (marks) {
            addBookmarks();
        }


        map.invalidate();
    }

    private void addBookmarks() {
        // bookmarks
        DiagramExpose e = new DiagramExpose(getContext(), null, null);
        Store store = new DiagramStore(e,"places.xml");
        e.createStore(store, "places.xml", "");


        addMarkers(e);
        ItemizedIconOverlay<OverlayItem> marks = new ItemizedIconOverlay<>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        //e.setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        //map.getController().animateTo(new GeoPoint(item.getPoint().getLatitude(), item.getPoint().getLongitude()));
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {

                        //e.setFocus(item.getUid(), false);
                        speak(item.getSnippet());

                        //map.getController().animateTo(new GeoPoint(item.getPoint().getLatitude(), item.getPoint().getLongitude()));
                        return true;
                    }
                }, getActivity());

        map.getOverlays().add(marks);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        String i = preferences.getString("interval", "10");
        interval = SECONDS.toMillis(Integer.parseInt(i));

        String s = preferences.getString("sensitivity", "10");
        trigger = 0.001 * Integer.parseInt(s);



        tts = new TextToSpeech(getContext(), this);


    }//onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.welcome, container, false);





        map = view.findViewById(R.id.map);//new MapView(getActivity());

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTileProvider(new MapTileProviderBasic(getActivity()));


        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);


        //LinearLayout layout = view.findViewById(R.id.mapview);
        //RelativeLayout.LayoutParams fitscreen = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        //layout.addView(map, fitscreen);



        addOverlays();



        return view;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startLocationUpdates();

        getLocation();

        String full_address = DiagramUtil.findReverseAddress(latitude, longitude);
        String address = full_address.split(",")[0];

        applyLocation(address);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopLocationUpdates();
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



    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {

        longitude = location.getLongitude();
        latitude = location.getLatitude();


        double d = DiagramUtil.distanceInKilometers(latitude, longitude, p_latitude, p_longitude);

        if (d > trigger) {



            String full_address = DiagramUtil.findReverseAddress(latitude, longitude);
            String address = full_address.split(",")[0];


            if (!address.equals(p_address)) {


                applyLocation(address);


                p_address = address;
            }

            p_longitude = longitude;
            p_latitude = latitude;
        }

    }



    private void applyLocation(String address) {

        try {
            TextView tv = getActivity().findViewById(R.id.map_coordinates);
            tv.setText(latitude + " / " + longitude);

            TextView l = getActivity().findViewById(R.id.map_location);
            l.setText(address);






            showPreview();


            speak(address);
            Toast.makeText(getContext(), address, Toast.LENGTH_LONG).show();

        } catch (Exception exception) {
            exception.printStackTrace();
        }


    }

    private void showPreview() {

        DecimalFormat df = new DecimalFormat("000.0000");

        String lon = df.format(longitude).replace(",", ".");
        String lat = df.format(latitude).replace(",", ".");

        map.getController().setZoom(16.2);
        map.getController().animateTo(new GeoPoint(latitude, longitude));



        /*
        String url;

        url = String.format("https://www.openstreetmap.org/#map=" + Integer.toString(map_size-3) + "/" + lat + "/" + lon);
        //wv.loadUrl(url);

        url = String.format("https://www.openstreetmap.org/#map=" + Integer.toString(map_size-2) + "/" + lat + "/" + lon);
        //wv.loadUrl(url);

        url = String.format("https://www.openstreetmap.org/#map=" + Integer.toString(map_size-1) + "/" + lat + "/" + lon);
        //wv.loadUrl(url);

        url = String.format("https://www.openstreetmap.org/#map=" + Integer.toString(map_size) + "/" + lat + "/" + lon);
        wv.loadUrl(url);
         */

    }




    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
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
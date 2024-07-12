package expose.osm.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import osm.expose.R;
import expose.model.impl.DiagramExpose;
import expose.model.impl.DiagramStore;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;

public class ViewPlace extends AppCompatActivity {

    DiagramExpose expo;
    WebView wv;

    double latitude, longitude;
    Drawable mark;




    private DiagramExpose expo() { return expo; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String namespace = getIntent().getStringExtra("namespace");
        String folder = getIntent().getStringExtra("folder");
        String selected = getIntent().getStringExtra("id");

        expo = new DiagramExpose(getApplicationContext(), null, null);

        Store store = new DiagramStore(expo(), namespace);
        expo().createStore(store, namespace, folder);


        mark = getApplicationContext().getDrawable(R.drawable.app_map);


        setContentView(R.layout.view_place);

        TextView c = findViewById(R.id.map_coordinates);
        TextView l = findViewById(R.id.map_location);



        wv = findViewById(R.id.map_view);


        wv.getSettings().setJavaScriptEnabled(true);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);


                int r = 48;
                int x = view.getWidth()/2;
                int y = view.getHeight()/2;

                mark.setBounds(x-r, y-r, x+r, y+r);

                view.getOverlay().clear();
                view.getOverlay().add(mark);



                // TODO draw other places
                for (UniversalModel model : expo().getStore().getModels()) {

                    String id = model.getId();
                    if (id != selected) {

                        String[] words = model.getCoordinates().split("/");
                        double latitude1 = Double.parseDouble(words[0]);
                        double longitude1 = Double.parseDouble(words[1]);

                        double dlatitude = latitude - latitude1;
                        double dlongitude = longitude - longitude1;

                        double angle = Math.toRadians(Math.atan2(dlatitude, dlongitude));


                        Drawable o = getApplicationContext().getDrawable(R.drawable.app_map_blue);
                        r = 24;
                        int l = 360;


                        x = view.getWidth()/2;
                        int x1 = (int) (x + l * Math.cos(angle));

                        y = view.getHeight()/2;
                        int y1 = (int) (y + l * Math.sin(angle));


                        o.setAlpha(80);
                        o.setBounds(x1-r, y1-r, x1+r, y1+r);



                        view.getOverlay().add(o);

                        Canvas canvas = null;

                        //canvas.drawText("text", 100, 100, new Paint());
                    }
                }
            }
        });







        UniversalModel model = expo().getStore().findModel(selected);
        if (model != null) {

            String coords = model.getCoordinates();
            c.setText(coords);
            l.setText(model.getLocation());


            String words[] = coords.split("/");

            latitude = Double.parseDouble(words[0]);
            longitude = Double.parseDouble(words[1]);




            DecimalFormat df = new DecimalFormat("000.0000");

            String lon = df.format(longitude).replace(",", ".");
            String lat = df.format(latitude).replace(",", ".");

            String url = String.format("https://www.openstreetmap.org/#map=17/" + lat + "/" + lon);
            wv.loadUrl(url);
        }

    }

}

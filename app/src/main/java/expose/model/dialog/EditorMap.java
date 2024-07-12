package expose.model.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.text.DecimalFormat;
import java.util.ArrayList;


import expose.model.DiagramUtil;
import expose.model.impl.DiagramModel;
import expose.model.meta.Command;
import expose.model.meta.UniversalModel;
import osm.expose.R;

public class EditorMap extends DialogFragment {

    Command diagram;
    WebView wv;

    ArrayList<OverlayItem> items;

    double latitude, longitude;
    public EditorMap(Command set_diagram, double set_latitude, double set_longitude, ArrayList<OverlayItem> set_items) {
        diagram = set_diagram;
        latitude = set_latitude;
        longitude = set_longitude;
        items = set_items;
    }





    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.ThemeOverlay);




        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.editor_map, null);




        builder.setView(view);



        wv = view.findViewById(R.id.map_view);

        wv.getSettings().setJavaScriptEnabled(true);
        WebViewClient wvc = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        };
        wv.setWebViewClient(wvc);


        DecimalFormat df = new DecimalFormat("000.0000");

        String lon = df.format(longitude).replace(",", ".");
        String lat = df.format(latitude).replace(",", ".");


        String url = String.format("https://www.openstreetmap.org/#map=17/" + lat + "/" + lon);
        wv.loadUrl(url);



        String okay = getActivity().getString(R.string.dialog_add_confirm);
        builder.setPositiveButton(okay, (dialog, id) -> {

            String[] words = wv.getUrl().split("/");
            int size = words.length;

            String lo = words[size-1];
            String la = words[size-2];


            UniversalModel model = new DiagramModel();
            model.setId(diagram.expo().getStore().getNewId());


            model.setDate(diagram.expo().getStore().today());

            model.setTitle(la + "/" + lo);
            model.setCoordinates(la + "/" + lo);

            longitude = Double.parseDouble(lo);
            latitude = Double.parseDouble(la);

            String a = DiagramUtil.findReverseAddress(latitude, longitude);

            model.setLocation(a);

            model.setSubject(a.split(",")[0]);


            diagram.addModel(model);
            OverlayItem item = new OverlayItem(model.getId(), model.getTitle(), model.getSubject(), new GeoPoint(latitude, longitude));
            items.add(item);
        });


        String cancel = getActivity().getString(R.string.dialog_cancel);
        builder.setNegativeButton(cancel, (dialog, id) -> {

            dialog.cancel();
        });





        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            Button negativeButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);

            negativeButton.setTextColor(Color.RED);
            positiveButton.setTextColor(Color.GREEN);

            negativeButton.invalidate();
            positiveButton.invalidate();
        });

        //dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        return dialog;
    }//builder


}
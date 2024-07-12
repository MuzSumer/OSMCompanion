package expose.osm;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import osm.expose.R;


public class AppAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_about);
    }
}
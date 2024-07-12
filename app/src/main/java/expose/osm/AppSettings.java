package expose.osm;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import osm.expose.R;

public class AppSettings extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.system_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle(getString(R.string.app_prefs));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_settings, new PreferenceFragment())
                .commit();
        /*


         */


    }

    /* --------------------------------windvolt-------------------------------- */

    // preference fragment
    public static class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            onSharedPreferenceChanged(sharedPreferences, "");
        }



        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            /*
            Preference host = findPreference("host");
            String vhost = sharedPreferences.getString("host", "");
            host.setTitle("title");
            host.setTitle(vhost);
             */

            Preference i = findPreference("interval");
            String vint = sharedPreferences.getString("interval", "1");
            i.setSummary("title");
            i.setSummary(vint);

            Preference s = findPreference("sensitivity");
            String vsen = sharedPreferences.getString("sensitivity", "1");
            s.setSummary("title");
            s.setSummary(vsen);

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }//PreferenceFragment
}

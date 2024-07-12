package expose.model;

import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class DiagramUtil {

    private DiagramUtil() {}



    public static String findReverseAddress(double latitude, double longitude) {

        String city_name = "";


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection connection = null;
        {
            try {
                String uri = "https://nominatim.openstreetmap.org/reverse?format=json";
                uri += "&lat=" + Double.toString(latitude).replace(",", ".");
                uri += "&lon=" + Double.toString(longitude).replace(",", ".");
                uri += "&zoom=16";
                //uri += "&key=ExpoDrive";

                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();

                BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
                InputStreamReader input = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(input);

                String line = reader.readLine();

                reader.close();
                input.close();
                stream.close();

                connection.disconnect();


                String[] words = line.split("\",\"");
                for (String word : words) {

                    if (word.contains("display_name")) {
                        city_name = word.substring(15);
                    }
                }


            } catch (Exception e) {
                if (connection != null) connection.disconnect();

                e.printStackTrace();
                return "";
            }
        }
        return city_name;
    }

    public static String trim(String value) {
        String trimmed = value;

        trimmed.replace("     ", " ");
        trimmed.replace("    ", " ");
        trimmed.replace("   ", " ");
        trimmed.replace("  ", " ");

        // trim leading spaces
        while (trimmed.startsWith(" ")) {
            trimmed = trimmed.substring(1);
        }

        // trim end spaces
        while (trimmed.endsWith(" ")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }



        return trimmed;
    }

    public static String trim(TextView view) {
        String value = view.getText().toString();

        return trim(value);
    }




    public static boolean isNumeric(String str) {
        return str != null && str.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isRemote(String exp) {

        if (exp.startsWith("http")) return true;
        if (exp.startsWith("*/")) return true;
        if (exp.startsWith("#/")) return true;

        return false;
    }




    public static double distanceInKilometers(String c0, String c1) {
        double earthRadiusKm = 6371;

        String words0[] = c0.split("/");
        double lat0 = Double.parseDouble(words0[0]);
        double lon0 = Double.parseDouble(words0[1]);

        String words1[] = c1.split("/");
        double lat1 = Double.parseDouble(words1[0]);
        double lon1 = Double.parseDouble(words1[1]);

        return distanceInKilometers(lat0, lon0, lat1, lon1);
    }

    public static double distanceInKilometers(double lat0, double lon0, double lat1, double lon1) {
        double earthRadiusKm = 6371;

        double dlat = radians(lat1 - lat0);
        double dlon = radians(lon1 - lon0);


        double rlat0 = radians(lat0);
        double rlat1 = radians(lat1);

        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.sin(dlon/2) * Math.sin(dlon/2) * Math.cos(rlat0) * Math.cos(rlat1);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return c * earthRadiusKm;
    }

    private static double radians(double degrees) {
        return degrees * Math.PI / 180;
    }



    public static void setDBounds(Drawable d, int r, int l, int t) {
        d.setBounds(l-r, t-r, l+r, t+r);
    }


}

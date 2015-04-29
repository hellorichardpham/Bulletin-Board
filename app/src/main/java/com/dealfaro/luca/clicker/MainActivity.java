package com.dealfaro.luca.clicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;



import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    Location lastLocation;
    private double lastAccuracy = (double) 1e10;
    private long lastAccuracyTime = 0;

    private static final String LOG_TAG = "lclicker";

    private static final float GOOD_ACCURACY_METERS = 100;

    // This is an id for my app, to keep the key space separate from other apps.
    private static final String MY_APP_ID = "luca_bboard";

    private static final String SERVER_URL_PREFIX = "https://luca-teaching.appspot.com/store/default/";

    // To remember the favorite account.
    public static final String PREF_ACCOUNT = "pref_account";

    // To remember the post we received.
    public static final String PREF_POSTS = "pref_posts";

    // Uploader.
    private ServerCall uploader;

    // Remember whether we have already successfully checked in.
    private boolean checkinSuccessful = false;

    private ArrayList<String> accountList;

    private class ListElement {
        ListElement() {};

        public String textLabel;
        public String buttonLabel;
    }

    private ArrayList<ListElement> aList;

    ProgressBar spinner;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
           // Button b = (Button) newView.findViewById(R.id.itemButton);
            tv.setText(w.textLabel);
            //b.setText(w.buttonLabel);

            // Sets a listener for the button, and a tag for the button as well.
            //b.setTag(new Integer(position));
           /* b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reacts to a button press.
                    // Gets the integer tag of the button.
                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s + "WOW", duration);
                    toast.show();
                }
            });
            */

            // Set a listener for the whole list item.
            newView.setTag(w.textLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        aa.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // First super, then do stuff.
        // Let us display the previous posts, if any.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String result = settings.getString(PREF_POSTS, null);
        if (result != null) {
            try {
                displayResult(result);
            } catch (Exception e) {
                // Removes settings that can't be correctly decoded.
                Log.w(LOG_TAG, "Failed to display old messages: " + result + " " + e);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove(PREF_POSTS);
                editor.commit();
            }
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }



    @Override
    protected void onPause() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        // Stops the upload if any.
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }

        super.onPause();
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String lat = String.format("%.3f", location.getLatitude());
            String lng = String.format("%.3f", location.getLongitude());
            lastLocation = location;
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            String locationString = "Lat: " + lat + " Long: "
                    + lng;
            Toast toast = Toast.makeText(context, locationString, duration);
            toast.show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };



    public void clickPost(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();
        spinner.setVisibility(v.VISIBLE);
        //Get your current lat/lng
        //msg will contain "withmywoes" or whatever. DONE
        //Generate a msgid
        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();

        myCallSpec.url = SERVER_URL_PREFIX + "put_local";
        myCallSpec.context = MainActivity.this;

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();

        String msgid = UUID.randomUUID().toString();
        msgid = msgid.replace("-", "");

        String lat = Double.toString(latitude);
        String lng = Double.toString(longitude);
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("msg", msg);
        m.put("lat", lat);
        m.put("lng", lng);
        m.put("msgid", msgid);
        m.put("msg", msg);
        m.put("app_id", MY_APP_ID);
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
        et.setText("");

    }

    public void clickRefresh(View v) {
        spinner.setVisibility(v.VISIBLE);
        PostMessageSpec myCallSpec = new PostMessageSpec();

        myCallSpec.url = SERVER_URL_PREFIX + "get_local";
        myCallSpec.context = MainActivity.this;
        //lat=120.1111&lng=33.333333
        String lat = Double.toString(lastLocation.getLatitude());
        String lng = Double.toString(lastLocation.getLongitude());

        HashMap<String,String> m = new HashMap<String,String>();
        m.put("lat", lat);
        m.put("lng", lng);
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }


    /**
     * This class is used to do the HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            System.out.println("Entering useResult");
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");
            } else {
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();
            }
        }
    }


    private void displayResult(String result) {
        System.out.println("Result: " + result);
        Gson gson = new Gson();
        MessageList ml = gson.fromJson(result, MessageList.class);
        //Clear the list of messages so it's a clean slate.
        aList.clear();
        //Iterate through the gson request. Create a
        //ListElement which is one line of the view.
        //Set the attributes of the ListElement using
        //the gson attributes. Then add to list.
        final int MAXIMUM_MESSAGES = 10;
        for (int i = 0; i < ml.messages.length && i <= MAXIMUM_MESSAGES; i++) {
            ListElement ael = new ListElement();
            if(!ml.messages[i].msg.equals("")) {
                ael.textLabel = ml.messages[i].getTimedMessage();
                //ael.buttonLabel = "Click";
                aList.add(ael);
            }
        }
        aa.notifyDataSetChanged();
        spinner.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

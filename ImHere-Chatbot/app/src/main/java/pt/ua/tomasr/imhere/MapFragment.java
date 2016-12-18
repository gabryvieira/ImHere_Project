package pt.ua.tomasr.imhere;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ua.tomasr.imhere.chat.ChatActivity;
import pt.ua.tomasr.imhere.modules.GeoChat;
import pt.ua.tomasr.imhere.modules.InfoChat;
import pt.ua.tomasr.imhere.modules.LocationCoord;
import pt.ua.tomasr.imhere.rabitt.MessageBroker;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_MAGENTA;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ROSE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;


/**
 * Tomás Rodrigues (tomasrodrigues@ua.pt)
 * @date Setember 2016
 */

@SuppressLint("ValidFragment")
public class MapFragment extends Fragment implements OnMapReadyCallback {

    //Loading
    ProgressBar progress_bar;
    TextView progress_text;

    //On Map View
    private GoogleMap mMap;
    private List<Circle> mCircle = new ArrayList<>();
    private List<Marker> mMarkers = new ArrayList<>();
    private LocationCoord gps;

    //Gets
    ArrayList<Double> ClosestPoints = new ArrayList<Double>();
    ArrayList<Double> InsideCircle = new ArrayList<Double>();

    List<GeoChat> insideCircle = new ArrayList<GeoChat>();
    List<InfoChat> chatinfos = new ArrayList<InfoChat>();
    ArrayList<Double> ids = new ArrayList();
    ArrayList<Integer> ids_info = new ArrayList();

    //Rabbit parameters
    MessageBroker msg = MainActivity.msg;
    String hash = "";
    String user_id = "";
    public static ArrayList<String> chatmessages = new ArrayList<String>();

    //Variaveis adicionais(create chat)
      private String chat_name, chat_description, chat_password, event_type;
//    private Double chat_radius, chat_time;
//    private Boolean isPublic;

    public MapFragment(LocationCoord gps) {
        this.gps = gps;
    }
    public MapFragment(LocationCoord gps, List<GeoChat> insideCircle) {
        this.gps = gps;
        this.insideCircle = insideCircle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        hash = bundle.getString("hash");

//      HTTP GET CLOSESTS POINTS
        String URL_closestpoints = "http://192.168.8.217:5011/location/closestPoints?latitude="+gps.getLatitude()+
                "&longitude="+gps.getLongitude()+"&points=10&distance=150000";

        String URL_insidecircle = "http://192.168.8.217:5011/location/insideCircle?latitude="+gps.getLatitude()+
                "&longitude="+gps.getLongitude();

        new GETClosestsPoints().execute(URL_closestpoints);

        for (GeoChat tmp:insideCircle) {
            ids.add(tmp.getID());
        }

        new RabbitGetChatInfo().execute();
        new GETInsideCircle().execute(URL_insidecircle);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        progress_bar = (ProgressBar) v.findViewById(R.id.progress_bar);
        progress_text = (TextView) v.findViewById(R.id.progress_text);
        progress_bar.setVisibility(View.VISIBLE);
        progress_text.setVisibility(View.VISIBLE);

        try {
            for (InfoChat tmp : msg.getChatInfos()) {
                ids_info.add(tmp.getID());
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        Log.i("ids",""+ids_info);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);


        if(ClosestPoints==null || ClosestPoints.size()==0){
            progress_bar = (ProgressBar) getView().findViewById(R.id.progress_bar);
            progress_text = (TextView) getView().findViewById(R.id.progress_text);
            progress_text.setText("Updating Map...");
            progress_bar.setVisibility(View.VISIBLE);
            progress_text.setVisibility(View.VISIBLE);
            SystemClock.sleep(1500);
            progress_bar.setVisibility(View.GONE);
            progress_text.setVisibility(View.GONE);
        }
        Log.i("json",""+ClosestPoints);
        Double[] a_closests = ClosestPoints.toArray(new Double[ClosestPoints.size()]);


        //Desenhar Closest Points
        //id-0, distance-1, lat-2, lon-3, radius-4
        for(int i=0; i< ClosestPoints.size(); i+=5) {
            addClosestCircle(a_closests[i+2],a_closests[i+3],a_closests[i+4]);
            //addMarker(chat_name, chat_description, event_type);
            addClosestMarker(a_closests[i+2],a_closests[i+3]);
        }

        //Inside Circle
        // id-0, lat-1, lon-2, radius-3
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Double[] a_insidecircles = InsideCircle.toArray(new Double[InsideCircle.size()]);
                for (int i = 0; i < a_insidecircles.length; i += 4) {
                    LatLng circulos = new LatLng(a_insidecircles[i + 1], a_insidecircles[i + 2]);
                    //entra no chat
                    if (marker.getPosition().equals(circulos)) {

                        new RabbitJoinChat().execute(a_insidecircles[i].toString());
                        SystemClock.sleep(500);

                        String message = "Entering Chat... ";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                        String extraFromName = getActivity().getIntent().getExtras().getString("EXTRA_SESSION_Name");
                        String extraFromEmail = getActivity().getIntent().getExtras().getString("EXTRA_SESSION_Email");

                        Intent intent = new Intent(getActivity().getBaseContext(), ChatActivity.class);

                        intent.putExtra("EXTRA_SESSION_Name", extraFromName);
                        intent.putExtra("EXTRA_SESSION_Email", extraFromEmail);
                        intent.putExtra("EXTRA_SESSION_Hash", hash);


                        try {
                            for(int j=0; j<msg.getChatInfos().size();j++) {
                                if(a_insidecircles[j].intValue() == msg.getChatInfos().get(j).getID()) {
                                    intent.putExtra("chat_title", msg.getChatInfos().get(j).getName());
                                    intent.putExtra("chat_subtitle", msg.getChatInfos().get(j).getDescription());
                                    intent.putExtra("chat_id", a_insidecircles[i].toString());
                                    chatmessages = msg.getChatMessages();
                                    intent.putStringArrayListExtra("chat_messages", chatmessages);

                                    startActivityForResult(intent, 1);
                                }
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }



                        return true;
                    }
                    //ELSE - ESTÁ DEMASIADO LONGE
                    Toast.makeText(getActivity(), "Esse chat está demasiado longe!", Toast.LENGTH_SHORT).show();
                    Log.i("posicao", "Esse chat está demasiado longe!");
                }
                    return false;

            }
        });


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 8));
    }

    public void addClosestCircle(Double chat_lat, Double chat_lon, Double chat_radius) {
        mCircle.add(mMap.addCircle(new CircleOptions().center(
                new LatLng(chat_lat, chat_lon)).radius(chat_radius).fillColor(Color.argb(60, 0, 0, 200)).strokeColor(Color.argb(90, 0, 0, 200)).strokeWidth(8)));
    }

    public void addMarker(String chat_name, String chat_description, String event_type) {
        float cor = HUE_YELLOW;
        switch (event_type){
            case "Music festival":
                cor = HUE_ROSE;
                break;
            case "Local show":
                cor = HUE_VIOLET;
                break;
            case "Street market":
                cor = HUE_ORANGE;
                break;
            case "Building Reunion":
                cor = HUE_BLUE;
                break;
            case "School/University":
                cor = HUE_GREEN;
                break;
            case "Sport related":
                cor = HUE_MAGENTA;
                break;
            case "Other":
                cor = HUE_YELLOW;
                break;
        }

        mMarkers.add(mMap.addMarker(new MarkerOptions().position(
                new LatLng((gps.getLatitude()), gps.getLongitude())).
                title(chat_name).snippet(event_type + ": "+ chat_description).icon(BitmapDescriptorFactory.
                defaultMarker(cor))));
    }

    public void addClosestMarker(Double chat_lat, Double chat_lon) {
        float cor = HUE_YELLOW;
//        switch (event_type){
//            case "Music festival":
//                cor = HUE_ROSE;
//                break;
//            case "Local show":
//                cor = HUE_VIOLET;
//                break;
//            case "Street market":
//                cor = HUE_ORANGE;
//                break;
//            case "Building Reunion":
//                cor = HUE_BLUE;
//                break;
//            case "School/University":
//                cor = HUE_GREEN;
//                break;
//            case "Sport related":
//                cor = HUE_MAGENTA;
//                break;
//            case "Other":
//                cor = HUE_YELLOW;
//                break;
//        }

        mMarkers.add(mMap.addMarker(new MarkerOptions().position(
                new LatLng(chat_lat, chat_lon)).
                title("Este chat está demasiado longe").snippet("Este chat está demasiado longe").icon(BitmapDescriptorFactory.
                defaultMarker(cor))));
    }

    public double distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (double) (earthRadius * c);

        return dist;
    }

    private class RabbitJoinChat extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                String mensagem = "{\"op_id\":4,\"hash\":\""+hash+"\",\"chat_id\":\""+urls[0]+"\"}";
                chatmessages.clear();
                msg.publish("hello",mensagem);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","ERRO RABBIT");
            }

            return "";
        }

        protected void onPostExecute(Boolean result) {

        }

    }


    private class GETClosestsPoints extends AsyncTask<String, Void, ArrayList> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList doInBackground(String... urls) {

            StringBuilder result = new StringBuilder();
            try {
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            String resultado = result.toString();
            JSONArray jArray = new JSONArray(resultado);

            for (int i=0; i < jArray.length(); i++) {

                JSONObject oneObject = jArray.getJSONObject(i);

                // Pulling items from the Objects
                double d_id = oneObject.getDouble("id");
                double d_distance = oneObject.getDouble("distance");
                double d_latitude = oneObject.getDouble("latitude");
                double d_longitude = oneObject.getDouble("longitude");
                double d_radius = oneObject.getDouble("radius");

                //Add to the list
                ClosestPoints.add(d_id);
                ClosestPoints.add(d_distance);
                ClosestPoints.add(d_latitude);
                ClosestPoints.add(d_longitude);
                ClosestPoints.add(d_radius);

            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ClosestPoints;
        }

        protected void onPostExecute(Boolean result) {

        }

    }

    private class RabbitGetChatInfo extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {

                String mensagem = "{\"op_id\":8,\"hash\":\""+hash+"\",\"chat_id\":"+ids+"}";
                JSONObject obj = new JSONObject(mensagem);

                msg.getChatInfos().clear();
                msg.publish("hello",mensagem);
                SystemClock.sleep(500);


            }catch (Exception e){
                e.printStackTrace();
                Log.e("error","ERRO RABBIT");
            }

            return "";
        }

        protected void onPostExecute(Boolean result) {

        }

    }

    private class GETInsideCircle extends AsyncTask<String, Void, ArrayList> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList doInBackground(String... urls) {

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();

                String resultado = result.toString();
                JSONArray jArray = new JSONArray(resultado);

                for (int i=0; i < jArray.length(); i++) {

                    JSONObject oneObject = jArray.getJSONObject(i);

                    // Pulling items from the Objects
                    double d_id = oneObject.getDouble("id");
                    double d_latitude = oneObject.getDouble("latitude");
                    double d_longitude = oneObject.getDouble("longitude");
                    double d_radius = oneObject.getDouble("radius");

                    //Add to the list
                    InsideCircle.add(d_id);
                    InsideCircle.add(d_latitude);
                    InsideCircle.add(d_longitude);
                    InsideCircle.add(d_radius);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return InsideCircle;
        }

        protected void onPostExecute(Boolean result) {

        }

    }



}

package pt.ua.tomasr.imhere;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;
import java.util.List;

import pt.ua.tomasr.imhere.modules.LocationCoord;

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

    //On Map View
    private GoogleMap mMap;
    private List<Circle> mCircle = new ArrayList<>();
    private List<Marker> mMarkers = new ArrayList<>();
    private LocationCoord gps;

    //Variaveis adicionais(create chat)
    private String chat_name, chat_description, chat_password, event_type;
    private Double chat_radius, chat_time;
    private Boolean isPublic;

    public MapFragment(LocationCoord gps) {
        this.gps = gps;
    }

    public MapFragment(LocationCoord gps, List<Marker> mMarkers, List<Circle> mCircle) {
        this.gps = gps;
        this.mMarkers = mMarkers;
        this.mCircle = mCircle;
    }

    public MapFragment(LocationCoord gps, String chat_name, String chat_description, Double chat_radius, Double chat_time, Boolean isPublic, String chat_password, String event_type) {
        this.gps = gps;
        this.chat_name = chat_name;
        this.chat_description = chat_description;
        this.chat_radius = chat_radius;
        this.chat_time = chat_time;
        this.isPublic = isPublic;
        this.chat_password = chat_password;
        this.event_type = event_type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if(chat_radius != null && chat_name != null && event_type!=null){
            addCircle(chat_radius);
            addMarker(chat_name, chat_description, event_type);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getActivity().getBaseContext(), pt.ua.tomasr.imhere.chat.MainActivity.class);
                intent.putExtra("chat_title", marker.getTitle());
                intent.putExtra("chat_subtitle", marker.getSnippet());
                startActivityForResult(intent, 1);
                return true;
            }
        });


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 2));
    }

    public void addCircle(Double chat_radius) {
        mCircle.add(mMap.addCircle(new CircleOptions().center(
                new LatLng((gps.getLatitude()), gps.getLongitude())).radius(chat_radius).fillColor(Color.argb(120, 0, 0, 200)).strokeColor(Color.argb(90, 0, 0, 200)).strokeWidth(8)));
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

    public float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }





}

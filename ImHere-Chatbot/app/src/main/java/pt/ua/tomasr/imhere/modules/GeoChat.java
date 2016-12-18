package pt.ua.tomasr.imhere.modules;

/**
 * Created by reytm on 02/11/2016.
 */

public class GeoChat {
    private Double id;
    private Double lat;
    private Double lon;
    private Double radius;


    public GeoChat(Double id, Double lat, Double lon, Double radius) {
        super();
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
    }

    public Double getID() {
        return id;
    }
    public Double getLat() {
        return lat;
    }
    public Double getLon() {
        return lon;
    }
    public Double getRadius() {
        return radius;
    }

    public void setId(Double id){
        this.id=id;
    }
    public void setLat(Double lat){
        this.lat=lat;
    }
    public void setLon(Double lon){
        this.lon=lon;
    }

    public void setRadius(Double radius){
        this.radius=radius;
    }

}

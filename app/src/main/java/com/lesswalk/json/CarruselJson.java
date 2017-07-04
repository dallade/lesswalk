package com.lesswalk.json;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by elazar on 20/06/17.
 */

public class CarruselJson
{
    private String        audio             = null;
    private String        content_type      = null;
    private String        creation_time     = null;
    private String        icon              = null;
    private Image         image             = null;
    private int           index             = -1;
    private Indoor        indoor            = null;
    private IndoorAddress indoor_address    = null;
    private String        key               = null;
    private String        label             = null;
    private double        latitude          = 0.0;
    private double        longitude         = 0.0;
    private MapAddress    map_address       = null;
    private String        map_thumbnail     = null;
    private double        modification_time = 0.0;
    private Parkings      parkings[]        = null;
    private Site          site              = null;
    private String        tips              = null;
    private String        title             = null;
    private String        type              = null;
    private Video         video             = null;

    public String getMap_thumbnail() {return map_thumbnail;}
    public String getTips() {return tips;}
    public String getKey() {return key;}
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}
    public double getModification_time() {return modification_time;}
    public Image getImage() {return image;}
    public Indoor getIndoor() {return indoor;}
    public IndoorAddress getIndoor_address() {return indoor_address;}
    public int getIndex() {return index;}
    public MapAddress getMap_address() {return map_address;}
    public Parkings[] getParkings() {return parkings;}
    public Site getSite() {return site;}
    public String getAudio() {return audio;}
    public String getCreation_time() {return creation_time;}
    public String getIcon() {return icon;}
    public String getLabel() {return label;}
    public String getContent_type() {return content_type;}
    public String getTitle() {return title;}
    public String getType() {return type;}
    public Video getVideo() {return video;}

    public void setMap_thumbnail(String map_thumbnail) {this.map_thumbnail = map_thumbnail;}
    public void setMap_address(MapAddress map_address) {this.map_address = map_address;}
    public void setAudio(String audio) {this.audio = audio;}
    public void setCreation_time(String creation_time) {this.creation_time = creation_time;}
    public void setIcon(String icon) {this.icon = icon;}
    public void setImage(Image image) {this.image = image;}
    public void setIndex(int index) {this.index = index;}
    public void setIndoor(Indoor indoor) {this.indoor = indoor;}
    public void setIndoor_address(IndoorAddress indoor_address) {this.indoor_address = indoor_address;}
    public void setKey(String key) {this.key = key;}
    public void setLabel(String label) {this.label = label;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public void setModification_time(double modification_time) {this.modification_time = modification_time;}
    public void setParkings(Parkings[] parkings) {this.parkings = parkings;}
    public void setSite(Site site) {this.site = site;}
    public void setContent_type(String content_type) {this.content_type = content_type;}
    public void setTips(String tips) {this.tips = tips;}
    public void setTitle(String title) {this.title = title;}
    public void setType(String type) {this.type = type;}
    public void setVideo(Video video) {this.video = video;}
    
    public void Print()
    {
        Log.d("CarruselJson", "CarruselJson:");
        Log.d("CarruselJson", "audio:" + audio);
        Log.d("CarruselJson", "content_type:" + content_type);
        Log.d("CarruselJson", "creation_time:" + creation_time);
        Log.d("CarruselJson", "icon:" + icon);
        Log.d("CarruselJson", "image:" + image);
        Log.d("CarruselJson", "index:" + index);
        Log.d("CarruselJson", "indoor:" + indoor);
        Log.d("CarruselJson", "indoor_address:" + indoor_address);

        Log.d("CarruselJson", "key:" + key);
        Log.d("CarruselJson", "label:" + label);
        Log.d("CarruselJson", "latitude:" + latitude);
        Log.d("CarruselJson", "longitude:" + longitude);
        Log.d("CarruselJson", "map_address:" + map_address);
        Log.d("CarruselJson", "map_thumbnail:" + map_thumbnail);
        Log.d("CarruselJson", "modification_time:" + modification_time);

        if (parkings != null)
        {
            for (int i = 0; i < parkings.length; i++)
            {
                parkings[i].Print(i);
            }
        }
        Log.d("CarruselJson", "tips:" + tips);
        Log.d("CarruselJson", "title:" + title);
        Log.d("CarruselJson", "type:" + type);
        Log.d("CarruselJson", "video:" + video);
    }

    public void setParkingsAmmount(int parkingsAmmount)
    {
        parkings = new Parkings[parkingsAmmount];

        for(int i = 0; i < parkingsAmmount; i++)
        {
            parkings[i] = new Parkings();
        }
    }

    public static class Image
    {
        private String name = null;

        public String getName() {return name;}
        public void setName(String name) {this.name = name;}
    }

    public class Indoor
    {
        private int estimated_time = 0;

        public int getEstimated_time() {return estimated_time;}
        public void setEstimated_time(int estimated_time) {this.estimated_time = estimated_time;}
    }

    public class IndoorAddress
    {
        private String apt   = null;
        private String floor = null;

        public String getApt() {return apt;}
        public String getFloor() {return floor;}
        public void setApt(String apt) {this.apt = apt;}
        public void setFloor(String floor) {this.floor = floor;}
    }

    public static class MapAddress
    {
        private String City                    = null;
        private String Country                 = null;
        private String CountryCode             = null;
        private String FormattedAddressLines[] = null;
        private String Name                    = null;
        private String State                   = null;
        private String Street                  = null;
        private String SubLocality             = null;
        private String SubThoroughfare         = null;
        private String Thoroughfare            = null;

        public String getCity() {return City;}
        public String getCountry() {return Country;}
        public String getCountryCode() {return CountryCode;}
        public String getName() {return Name;}
        public String getState() {return State;}
        public String getStreet() {return Street;}
        public String getSubLocality() {return SubLocality;}
        public String getSubThoroughfare() {return SubThoroughfare;}
        public String getThoroughfare() {return Thoroughfare;}
        public String[] getFormattedAddressLines()
        {
            String ret[] = null;

            if(FormattedAddressLines != null)
            {
                ret = new String[FormattedAddressLines.length];
                for (int i = 0; i < ret.length; i++)
                {
                    ret[i] = FormattedAddressLines[i];
                }
            }
            return ret;
        }

        public void setCity(String city) {City = city;}
        public void setCountry(String country) {Country = country;}
        public void setCountryCode(String countryCode) {CountryCode = countryCode;}
        public void setFormattedAddressLines(String[] formattedAddressLines)
        {
            if(formattedAddressLines != null)
            {
                FormattedAddressLines = new String[formattedAddressLines.length];
                for (int i = 0; i < FormattedAddressLines.length; i++)
                {
                    FormattedAddressLines[i] = formattedAddressLines[i];
                }
            }
            else FormattedAddressLines = null;
        }

        public void setName(String name) {Name = name;}
        public void setState(String state) {State = state;}
        public void setStreet(String street) {Street = street;}
        public void setSubLocality(String subLocality) {SubLocality = subLocality;}
        public void setSubThoroughfare(String subThoroughfare) {SubThoroughfare = subThoroughfare;}
        public void setThoroughfare(String thoroughfare) {Thoroughfare = thoroughfare;}
    }

    public class Parkings
    {
        private int           distance       = 0;
        private int           estimated_time = 0;
        private String        key            = null;
        private double        latitude       = 0.0;
        private double        longitude      = 0.0;
        private MapAddress    map_address    = null;
        private String        map_thumbnail  = null;
        private StreetAddress street_address = null;
        private String        tips           = null;
        private Image         image          = null;

        public int getEstimated_time() {return estimated_time;}
        public double getLatitude() {return latitude;}
        public double getLongitude() {return longitude;}
        public int getDistance() {return distance;}
        public MapAddress getMap_address() {return map_address;}
        public StreetAddress getStreet_address() {return street_address;}
        public String getKey() {return key;}
        public String getMap_thumbnail() {return map_thumbnail;}
        public String getTips() {return tips;}
        public Image getImage() {return image;}

        public void setDistance(int distance) {this.distance = distance;}
        public void setEstimated_time(int estimated_time) {this.estimated_time = estimated_time;}
        public void setKey(String key) {this.key = key;}
        public void setLatitude(double latitude) {this.latitude = latitude;}
        public void setLongitude(double longitude) {this.longitude = longitude;}
        public void setMap_address(MapAddress map_address) {this.map_address = map_address;}
        public void setMap_thumbnail(String map_thumbnail) {this.map_thumbnail = map_thumbnail;}
        public void setStreet_address(StreetAddress street_address) {this.street_address = street_address;}
        public void setTips(String tips) {this.tips = tips;}
        public void setImage(Image image) {this.image = image;}

        public void Print(int i)
        {
            Log.d("CarruselJson", "parkings_" + i + ": distance: " + distance);
            Log.d("CarruselJson", "parkings_" + i + ": estimated_time: " + estimated_time);
            Log.d("CarruselJson", "parkings_" + i + ": key: " + key);
            Log.d("CarruselJson", "parkings_" + i + ": latitude: " + latitude);
            Log.d("CarruselJson", "parkings_" + i + ": longitude: " + longitude);
            Log.d("CarruselJson", "parkings_" + i + ": map_address: " + map_address);
            Log.d("CarruselJson", "parkings_" + i + ": map_thumbnail: " + map_thumbnail);
            Log.d("CarruselJson", "parkings_" + i + ": street_address: " + street_address);
            Log.d("CarruselJson", "parkings_" + i + ": tips: " + tips);
        }
    }

    public class StreetAddress
    {
        private String city   = null;
        private String region = null;
        private String street = null;

        public String getCity() {return city;}
        public String getRegion() {return region;}
        public String getStreet() {return street;}
        public void setCity(String city) {this.city = city;}
        public void setRegion(String region) {this.region = region;}
        public void setStreet(String street) {this.street = street;}
    }

    public class Site
    {

    }

    public class Video
    {
        private String name = null;

        public String getName() {return name;}
        public void setName(String name) {this.name = name;}
    }

    public void save(File file)
    {
        try
        {
            String       json = new Gson().toJson(this);
            OutputStream os   = new FileOutputStream(file);

            os.write(json.getBytes());
            os.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

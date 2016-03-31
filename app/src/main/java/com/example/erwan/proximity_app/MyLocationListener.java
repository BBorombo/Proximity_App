package com.example.erwan.proximity_app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Erwan on 20/11/2015.
 */
public class MyLocationListener implements LocationListener {

    private Context context;
    Geocoder geo;
    private Location myLocation = new Location("");
    private Location adrLocation = new Location("");
    private String adresse;
    private String lastAdresse = "Lyon";
    private Float distance;
    TextView editLongitude;
    TextView editLatitude;
    TextView editDistance;
    Boolean makeNotification;

    public MyLocationListener(Context context, TextView longitude, TextView latitude, TextView distance) {
        //On récupère le context
        this.context = context;

        //On initialise les coordonées
        this.adrLocation.setLongitude(0.0);
        this.adrLocation.setLatitude(0.0);

        this.editLongitude = longitude;
        this.editLatitude = latitude;
        this.editDistance = distance;

        //On initialise les notifications à vrai
        this.makeNotification = true;

        geo = new Geocoder(context, Locale.FRANCE);


        LocationManager locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        //Depuis l'API 23, en plus d'ajouter les permissions dans le Manifest, il faut aussi les vérifier avant de les utiliser
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        /**
         * On met en place un appel a la fonction onLocationChanged lorsque le GPS (et non la localisation par
         * réseaux de données)est activé, toutes les secondes.
         */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    /**
     * Permet de récupérer les coordonnées de l'adresse pour pouvoir les comparer avec la position
     */
    public void setAdresse(){
        List<Address> listOfAddress;
        checkPreferences();
        try {
            //On récupère un objet Address à partir de l'adresse définie
            listOfAddress = geo.getFromLocationName(adresse, 1);
            if(listOfAddress != null && !listOfAddress.isEmpty()){
                Address address = listOfAddress.get(0);
                //On récupère ses coordonées
                adrLocation.setLatitude(address.getLatitude());
                adrLocation.setLongitude(address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fonction appelée lorsque la position change
     * @param location Position
     */
    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
        editLongitude.setText(String.valueOf(myLocation.getLongitude()));
        editLatitude.setText(String.valueOf(myLocation.getLatitude()));
        setAdresse();       //On récupère l'adresse, si jamais elle a changé
        calculDistance();   //Après la mise à jour de la position, on recalcule la distance
    }

    /**
     * Fonction qui permet de calculer la distance entre l'utilisateur, et l'adresse définie
     */
    public void calculDistance(){
        if (adrLocation.getLatitude() != 0.0 && adrLocation.getLongitude() != 0.0){
            this.distance = this.myLocation.distanceTo(adrLocation);
            editDistance.setText(formatDistance());
            //Si la distance est inférieur à 1km
            if (this.distance < 1000){
                /*Et si la notification est active, on en envoie une, sinon, on ne fait rien, pour éviter l'envoie
                 de plusieur notification une fois que la prmeière a été supprimée*/
                if (makeNotification)
                    addNotification(); //Si la distance est inférieure à 1km, on envoie une notification
            } else {
                //Sinon, si on sort du périmètre, ou dans les autres cas, on réactive la notfication
                makeNotification = true;
            }
        }
    }

    /**
     * Permet de formater la distance pour qu'elle soit lisible à l'écran en km
     * @return dist La distance en km arrondi au mètre près
     */
    public String formatDistance(){
        String dist;
        Float dst = this.distance / 1000;
        dist = context.getResources().getString(R.string.arr, dst) + " km";
        return dist;
    }

    /**
     * Fonction qui permet d'émettre une notification
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) //La classe Notification.Build ne fonctionne qu'avec l'API 15 au minimum
    private void addNotification() {
        Notification.Builder builder = new Notification.Builder(this.context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Titre de Notification")
                .setContentText("Vous êtes à moins d'1Km de l'adresse sélectionnée ");

        Intent notificationIntent = new Intent(this.context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
        //On stoppe l'ajout de notification
        makeNotification = false;
    }

    /**
     * Permet de récupérer l'adresse des préférences, et d'anticiper un changement d'adresse, pour les notifications
     */
    public void checkPreferences () {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        adresse = sp.getString("adresse","Lyon");
        //Si on change l'adresse, on peut réactiver la notification
        if (!lastAdresse.equals(adresse))
            makeNotification = true;
        lastAdresse = adresse;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}

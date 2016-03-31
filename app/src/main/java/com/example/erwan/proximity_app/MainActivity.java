package com.example.erwan.proximity_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String[] drawerItemsList;
    private ListView myDrawer;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //On récupère les vues qui contiendrons les données
        TextView longitude = (TextView) findViewById(R.id.editLongitude);
        TextView latitude = (TextView) findViewById(R.id.editLatitude);
        TextView distance = (TextView) findViewById(R.id.editDistance);

        //Bouton qui permet d'accéder aux préférences
        Button pref = (Button) findViewById(R.id.preferences);
        pref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);

                /*Intent i = new Intent(MainActivity.this, MyPreferencesActivity.class);
                startActivity(i);*/
            }
        });

        //Création de notre Listener
        MyLocationListener myLocationListener = new MyLocationListener(this, longitude, latitude, distance);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerItemsList = getResources().getStringArray(R.array.items);
        myDrawer = (ListView) findViewById(R.id.my_drawer);
        myDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, drawerItemsList));
        myDrawer.setOnItemClickListener(new MyDrawerItemClickListener());

    }

    public AlertDialog.Builder alertBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        return builder;
    }

    public Context getContext(){
        return this;
    }

    private class MyDrawerItemClickListener implements
            ListView.OnItemClickListener {
        private String adresse;
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
            String clickedItem = (String) adapter.getAdapter().getItem(pos);
            if (clickedItem.equals("Adresse")){
                AlertDialog.Builder builder = alertBuilder();
                builder.setTitle("Définir l'adresse ");
                final EditText input = new EditText(getContext());
                adresse = sp.getString("adresse","Lyon");
                input.setText(adresse);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("adresse", input.getText().toString());
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
            //Log.d("Position",clickedItem );
            drawerLayout.closeDrawer(myDrawer);
        }
    }



}

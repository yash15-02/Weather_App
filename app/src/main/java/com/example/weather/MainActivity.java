package com.example.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingPB;
    private RelativeLayout homeRL;
    private ImageView backIV,searchIV,iconIV;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private TextInputEditText cityEdt;
    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        loadingPB = findViewById(R.id.PBLoading);
        homeRL = findViewById(R.id.RLHome);
        backIV= findViewById(R.id.IVBack);
        searchIV = findViewById(R.id.IVSearch);
        iconIV = findViewById(R.id.IVIcon);
        cityNameTV = findViewById(R.id.TVCityName);
        temperatureTV = findViewById(R.id.TVTemperature);
        conditionTV = findViewById(R.id.TVCondition);
        cityEdt = findViewById(R.id.EditCity);
        weatherRV = findViewById(R.id.RVWeather);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getweatherInfo(cityName);

        searchIV.setOnClickListener (new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getweatherInfo(city);
                }
            }
        });




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for(Address adr : addresses){
                if(adr != null){
                    String city = adr.getLocality();
                    if(city != null && !city.equals("") ){
                        cityName=city;
                    } else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this,"User city not found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cityName;
    }

    private void getweatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/current.json?key=c6509228fbd5434fb9994610230607&q=" + cityName + "&aqi=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try{
                    String tempetature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(tempetature +"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("Icon");
                   // String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1){
                        Picasso.get().load("https://www.timesnownews.com/web-stories/viral/good-morning-images-and-quotes-for-whatsapp-status/photostory/96357340.cms").into(backIV);
                    }else{
                        Picasso.get().load("https://www.google.com/imgres?imgurl=https%3A%2F%2Fs.itl.cat%2Fpngfile%2Fs%2F43-438745_ciel-nocturne-mobile-wallpaper-lock-screen-wallpaper-night.jpg&tbnid=jsIvlQVZ72pKZM&vet=10CBQQxiAoAmoXChMImMuZoKiCgAMVAAAAAB0AAAAAEA8..i&imgrefurl=https%3A%2F%2Fwww.itl.cat%2Fwallview%2FhmbRwb_ciel-nocturne-mobile-wallpaper-lock-screen-wallpaper-night%2F&docid=JxPeuu2dpnSzQM&w=500&h=888&itg=1&q=night%20wallpaper&ved=0CBQQxiAoAmoXChMImMuZoKiCgAMVAAAAAB0AAAAAEA8").into(backIV);
                    }
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");

                    for(int i=0; i<hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("time");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please add valid city name..", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
package com.example.forecastbook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.forecastbook.Adapters.ViewPagerAdapter;
import com.example.forecastbook.Adapters.WeatherRecyclerAdapter;
import com.example.forecastbook.Api.ApiClient;
import com.example.forecastbook.Models.CurrentWeather;
import com.example.forecastbook.Models.List;
import com.example.forecastbook.Models.LongTerm;
import com.example.forecastbook.Models.Sys;
import com.example.forecastbook.Models.UVindex;
import com.example.forecastbook.Models.Weather;
import com.example.forecastbook.Models.Weather_;
import com.example.forecastbook.Models.Weathermap;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    static CurrentWeather currentWeather;
    private TextView todayTemperature;
    private TextView todayDescription;
    private TextView todayWind;
    private TextView todayPressure;
    private TextView todayHumidity;
    private TextView todaySunrise;
    private TextView todaySunset;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView todayUvIndex;
    private TextView lastUpdate;
    private AppBarLayout appBarLayout;
    Formatting formatting;
    private TextView todayIcon;
    ProgressDialog progressDialog;
    private ViewPager viewPager;
    public static Typeface weatherio;
    private TabLayout tabLayout;
    public Boolean tempinF = false;

    private String apikey = "3e29e62e2ddf6dd3d2ebd28aed069215";
    String lattitude, longtitude;
    String templat, templon;
    String city = "jaipur";


    private java.util.List<LongTerm> longTermWeather = new ArrayList<>();
    private java.util.List<LongTerm> longTermTodayWeather = new ArrayList<>();
    private java.util.List<LongTerm> longTermTomorrowWeather = new ArrayList<>();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) ;
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 0, locationListener);
                }
            }
        }
    }

    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        listenlocation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 300000, 0, locationListener);

            }
        }


        formatting = new Formatting(this);

        progressDialog = new ProgressDialog(MainActivity.this);
        weatherio = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherio);


    }


    @Override
    protected void onStart() {
        super.onStart();
        updateLongTermWeatherUI();
        Task task = new Task();
        task.execute();
    }



    public void initializeUI() {


        appBarLayout = findViewById(R.id.appbarlayout);
        todayDescription = findViewById(R.id.todayDescription);
        todayHumidity = findViewById(R.id.todayHumidity);
        todayPressure = findViewById(R.id.todayPressure);
        todaySunrise = findViewById(R.id.todaySunrise);
        todaySunset = findViewById(R.id.todaySunset);
        lastUpdate = findViewById(R.id.lastUpdate);
        todayTemperature = findViewById(R.id.todayTemperature);
        todayWind = findViewById(R.id.todayWind);
        todayUvIndex = findViewById(R.id.todayUvIndex);
//        toolbar = findViewById(R.id.Toolbar);
        todayIcon = findViewById(R.id.todayIcon);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabs);
        swipeRefreshLayout = findViewById(R.id.refresher);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getdatafromapi(city);
                updateLongTermWeatherUI();
                swipeRefreshLayout.setRefreshing(false);

            }
        });


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Only allow pull to refresh when scrolled to top
                swipeRefreshLayout.setEnabled(verticalOffset == 0);
            }
        });


    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }


    public void getdatafromapi(String city) {

        Call<CurrentWeather> call = ApiClient.getInstance().getApi().getallstats(city, apikey);

        call.enqueue(new Callback<CurrentWeather>() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {

                progressDialog.setMessage("Downloading your Data");
                progressDialog.show();

                if (response.isSuccessful()) {
                    currentWeather = response.body();
                    templat = String.valueOf(currentWeather.getCoord().getLat());
                    templon = String.valueOf(currentWeather.getCoord().getLon());
                    updatecurrent();

                    final Call<UVindex> callUV = ApiClient.getInstance().getApi().getUVindex(templat, templon, apikey);
                    callUV.enqueue(new Callback<UVindex>() {
                        @Override
                        public void onResponse(Call<UVindex> call, Response<UVindex> response) {
                            if (response.isSuccessful()) {
                                UVindex uVindex = response.body();
                                todayUvIndex.setText("UV Index: " + UnitConvertor.convertUvIndexToRiskLevel(uVindex.getValue(), getApplicationContext()));
                                progressDialog.dismiss();
                                updateLongTerm(templat, templon);
                            }
                        }

                        @Override
                        public void onFailure(Call<UVindex> call, Throwable t) {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Invalid City",Toast.LENGTH_LONG).show();
                        }
                    });

                }


            }

            @Override
            public void onFailure(Call<CurrentWeather> call, Throwable t) {

                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Invalid City",Toast.LENGTH_LONG).show();
                Log.i("Resultt", "Faileddd");

            }
        });


    }


    public void updateLongTerm(String templat, String templon) {

        final Call<Weather> calllongterm = ApiClient.getInstance().getApi().getlongterm(templat, templon, apikey);
        calllongterm.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {

                    List tempobj;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    longTermTomorrowWeather = new ArrayList<>();


                    for (int i = 0; i < response.body().getList().size(); i++) {

                        LongTerm anotherobj = new LongTerm();
                        tempobj = response.body().getList().get(i);

                        anotherobj.setDescription(tempobj.getWeather().get(0).getDescription());
                        anotherobj.setHumidity(String.valueOf(tempobj.getMain().getHumidity()));
                        anotherobj.setPressure(String.valueOf(tempobj.getMain().getPressure()));
                        anotherobj.setTemperature(String.valueOf(tempobj.getMain().getTemp()));
                        anotherobj.setWind(String.valueOf(tempobj.getWind().getSpeed()));
                        anotherobj.setIcon(formatting.setWeatherIcon(tempobj.getWeather().get(0).getId(), true));
                        SimpleDateFormat lsdf = new SimpleDateFormat("E dd.MM.yyyy-HH:mm");
                        long time = tempobj.getDt();
                        Date date = new Date(time * 1000);
                        String everydaydate = lsdf.format(date);
                        anotherobj.setDate(String.valueOf(everydaydate));


                        Calendar cal = Calendar.getInstance();
                        String timemillis = tempobj.getDt() + "000";
                        cal.setTimeInMillis(Long.parseLong(timemillis));
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);

                        Calendar tomorrow = (Calendar) today.clone();
                        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

                        Calendar later = (Calendar) today.clone();
                        later.add(Calendar.DAY_OF_YEAR, 2);

                        if (cal.before(tomorrow)) {
                            longTermTodayWeather.add(anotherobj);

                        } else if (cal.before(later)) {
                            longTermTomorrowWeather.add(anotherobj);
                        } else {
                            longTermWeather.add(anotherobj);
                        }
                    }
                    updateLongTermWeatherUI();

                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });


    }


    private void updateLongTermWeatherUI() {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerviewFragment recyclerViewFragmentToday = new RecyclerviewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, "TODAY");

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerviewFragment recyclerViewFragmentTomorrow = new RecyclerviewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, "TOMORROW");

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerviewFragment recyclerViewFragment = new RecyclerviewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, "LATER");

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(currentPage, false);
    }


    public void listenlocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longtitude = String.valueOf(location.getLongitude());
                lattitude = String.valueOf(location.getLatitude());


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }


    public void updatecurrent() {

        Double tempinK = currentWeather.getMain().getTemp();
        todayTemperature.setText(String.format("%.1f", UnitConvertor.KelvintoCelcius(tempinK)) + " °C");


        String desc = currentWeather.getWeather().get(0).getDescription();
        todayDescription.setText(desc.substring(0, 1).toUpperCase() + desc.substring(1, desc.length()));

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm");
        long sunrise = currentWeather.getSys().getSunrise();
        long sunset = currentWeather.getSys().getSunset();
        Date sunr = new Date(sunrise * 1000);
        Date suns = new Date(sunset * 1000);
        todaySunrise.setText(String.valueOf("Sunrise : " + sdf.format(sunr)) + " AM");
        todaySunset.setText(String.valueOf("Sunset : " + sdf.format(suns)) + " PM");
        // toolbar.setTitle(currentWeather.getSys().getCountry());
        todayWind.setText(String.valueOf("Wind : " + currentWeather.getWind().getSpeed() + " m/s"));
        todayPressure.setText("Pressure: " + currentWeather.getMain().getPressure() + " hPa/mBar");
        todayHumidity.setText("Humidity: " + currentWeather.getMain().getHumidity() + " %");
        getSupportActionBar().setTitle(currentWeather.getName() + "," + currentWeather.getSys().getCountry());

        todayIcon.setText(formatting.setWeatherIcon(currentWeather.getWeather().get(0).getId(), true));


    }


    class Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getdatafromapi(city);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateLongTermWeatherUI();
        }
    }


    class CurrentLocationDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Call<CurrentWeather> call = ApiClient.getInstance().getApi().currentlocationdata(lattitude, longtitude, apikey);
            final Call<UVindex> callUV = ApiClient.getInstance().getApi().currentlocationUV(lattitude, longtitude, apikey);
            updateLongTerm(lattitude, longtitude);
            templat = lattitude;
            templon = longtitude;
            call.enqueue(new Callback<CurrentWeather>() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {

                    progressDialog.setMessage("Downloading your Data");
                    progressDialog.show();

                    if (response.isSuccessful()) {
                        currentWeather = response.body();
                        city = currentWeather.getName();
                        System.out.println("Reached Here bro");
                        updatecurrent();
                    }
                    callUV.enqueue(new Callback<UVindex>() {
                        @Override
                        public void onResponse(Call<UVindex> call, Response<UVindex> response) {
                            if (response.isSuccessful()) {
                                UVindex uVindex = response.body();

                                if (uVindex == null) {

                                    todayUvIndex.setText("Null");
                                    progressDialog.dismiss();
                                } else {
                                    todayUvIndex.setText("UV Index: " + UnitConvertor.convertUvIndexToRiskLevel(uVindex.getValue(), getApplicationContext()));
                                    progressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UVindex> call, Throwable t) {
                            progressDialog.dismiss();

                        }
                    });


                }

                @Override
                public void onFailure(Call<CurrentWeather> call, Throwable t) {

                    progressDialog.dismiss();
                    Log.i("Resultt", "Faileddd");

                }
            });

            return null;
        }
    }


    private void searchCities() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setPadding(32, 0, 32, 0);
        inputLayout.addView(input);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Search For City");
        alert.setView(inputLayout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    try{
                    getdatafromapi(result);
                        city = result;}
                    catch (Exception e){
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Invalid City",Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void refreshWeather() {
        getdatafromapi(city);
        updateLongTermWeatherUI();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshWeather();
            return true;
        }
        if (id == R.id.action_location) {

            CurrentLocationDataTask currentLocationDataTask = new CurrentLocationDataTask();
            currentLocationDataTask.execute();
            return true;
        }

        if (id == R.id.action_search) {
            searchCities();
            return true;
        }

        if (id == R.id.action_map){
            Intent i = new Intent(this, Weathermap.class);
            i.putExtra("lat",templat);
            i.putExtra("lon",templon);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);

    }

}





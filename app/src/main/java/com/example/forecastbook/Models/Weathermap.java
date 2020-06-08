package com.example.forecastbook.Models;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.forecastbook.R;

public class Weathermap extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weathermap);


        Intent i = getIntent();
        String lat = i.getStringExtra("lat");
        String lon = i.getStringExtra("lon");

        String url = "https://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature&";
        url = url + "lat=" + lat + "&lon=" + lon + "zoom=4";
        webView = findViewById(R.id.webView);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }
}

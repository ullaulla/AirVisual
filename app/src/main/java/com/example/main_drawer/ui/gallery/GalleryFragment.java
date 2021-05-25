package com.example.main_drawer.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.main_drawer.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryFragment extends Fragment implements NaverMap.OnMapClickListener, Overlay.OnClickListener, OnMapReadyCallback, NaverMap.OnCameraChangeListener, NaverMap.OnCameraIdleListener {
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;
    private List<Marker> markerList = new ArrayList<Marker>();
    private boolean isCameraAnimated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery,
                container,
                false);
        return view;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap){
        this.naverMap = naverMap;

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        naverMap.addOnCameraChangeListener(this);
        naverMap.addOnCameraIdleListener(this);
        //.setOnMapClickListener(this);

        LatLng mapCenter = naverMap.getCameraPosition().target;
        fetchFineDust(mapCenter.latitude, mapCenter.longitude, 5000);
    }

    //현재 위치 표시 안됌
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }

    @Override
    public void onCameraChange(int reason, boolean animated) {

        isCameraAnimated = animated;
    }

    @Override
    public void onCameraIdle() {
        if (isCameraAnimated) {
            LatLng mapCenter = naverMap.getCameraPosition().target;
            fetchFineDust(mapCenter.latitude, mapCenter.longitude, 5000);
        }
    }

    private void fetchFineDust(double lat, double lng, int m) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.iqair.com").addConverterFactory(GsonConverterFactory.create()).build();
        AirVisualApi airvisualApi = retrofit.create(AirVisualApi.class);
        airvisualApi.getFineByGeo(lat, lng, m).enqueue(new Callback<FineDustResult>() {

            /*
            @Override
            public void onResponseOK(Response<FineDustResult> response) {
                if (response.code() == 200) {
                    FineDustResult result = response.body();
                    updateMapMarkers(result);
                }
            }

             */

            @Override
            public void onResponse(Call<FineDustResult> call, Response<FineDustResult> response) {
                if (response.code() == 200) {
                    FineDustResult result = response.body();
                    updateMapMarkers(result);
                }
            }

            @Override
            public void onFailure(Call<FineDustResult> call, Throwable t) {

            }
        });
    }

    private void updateMapMarkers(FineDustResult result) {
        resetMarkerList();
        if (result.dustdust != null && result.dustdust.size() > 0) {
            for (Dust dust: result.dustdust) {
                Marker marker = new Marker();
                marker.setTag(dust);
                marker.setPosition(new LatLng(dustdust.lat, dustdust.lng));
                if (dust.pm10<30&&dust.pm2_5<15) {
                    marker.setIcon(OverlayImage.fromResource(R.drawable.marker_icon));
                }
                marker.setAnchor(new PointF(0.5f, 1.0f));
                marker.setMap(naverMap);
                marker.setOnClickListener(this);
                markerList.add(marker);
            }
        }
    }

    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        if (infoWindow.getMarker() != null) {
            infoWindow.close();
        }
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null) {
                infoWindow.close();
            } else {
                infoWindow.open(marker);
            }
            return true;
        }
        return false;
    }

    private void resetMarkerList() {
        if (markerList != null && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.setMap(null);
            }
            markerList.clear();
        }
    }
}
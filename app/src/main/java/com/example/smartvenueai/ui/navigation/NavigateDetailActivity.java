package com.example.smartvenueai.ui.navigation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartvenueai.R;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class NavigateDetailActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // MapLibre must be initialized before setContentView (which inflates MapView)
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_navigate_detail);

        // Back button
        findViewById(R.id.btnBackDetail).setOnClickListener(v -> finish());

        // MapView setup
        mapView = findViewById(R.id.detailMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapLibreMap maplibreMap) {
                maplibreMap.setStyle("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json",
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                // Draw dummy route
                                drawRoute(style);
                                
                                // Fit camera
                                org.maplibre.android.geometry.LatLngBounds bounds = new org.maplibre.android.geometry.LatLngBounds.Builder()
                                        .include(new org.maplibre.android.geometry.LatLng(18.9388, 72.8248))
                                        .include(new org.maplibre.android.geometry.LatLng(18.9378, 72.8258))
                                        .build();
                                maplibreMap.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            }
                        });
            }
        });

        // 3D View toggle buttons
        findViewById(R.id.btn3DViewTop).setOnClickListener(v ->
                Toast.makeText(this, "3D View toggled", Toast.LENGTH_SHORT).show());
                
        // Set up Directions RecyclerView
        RecyclerView rvRouteSteps = findViewById(R.id.rvRouteSteps);
        rvRouteSteps.setLayoutManager(new LinearLayoutManager(this));
        
        List<RouteStep> steps = new ArrayList<>();
        steps.add(new RouteStep("Head north on East Concourse", "50m", false));
        steps.add(new RouteStep("Turn left towards Central Aisle", "120m", false));
        steps.add(new RouteStep("Slight right at Section B", "30m", false));
        steps.add(new RouteStep("Arrive at Food Stall 1", "", true));
        
        RouteStepAdapter adapter = new RouteStepAdapter(steps);
        rvRouteSteps.setAdapter(adapter);
    }

    @Override protected void onStart()   { super.onStart();   if (mapView != null) mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  if (mapView != null) mapView.onResume(); }
    @Override protected void onPause()   { super.onPause();   if (mapView != null) mapView.onPause(); }
    @Override protected void onStop()    { super.onStop();    if (mapView != null) mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
    
    private void drawRoute(Style style) {
        java.util.List<org.maplibre.geojson.Point> routeCoordinates = new java.util.ArrayList<>();
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8258, 18.9380)); // Gate A
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8254, 18.9381)); // Inside curve
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8251, 18.9385)); // Mid-concourse
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8251, 18.9388)); // Concession

        org.maplibre.geojson.LineString lineString = org.maplibre.geojson.LineString.fromLngLats(routeCoordinates);
        org.maplibre.geojson.Feature routeFeature = org.maplibre.geojson.Feature.fromGeometry(lineString);

        org.maplibre.android.style.sources.GeoJsonSource source = new org.maplibre.android.style.sources.GeoJsonSource("route-source", routeFeature);
        style.addSource(source);

        org.maplibre.android.style.layers.LineLayer routeLayer = new org.maplibre.android.style.layers.LineLayer("route-layer", "route-source");
        routeLayer.setProperties(
                org.maplibre.android.style.layers.PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                org.maplibre.android.style.layers.PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                org.maplibre.android.style.layers.PropertyFactory.lineWidth(5f),
                org.maplibre.android.style.layers.PropertyFactory.lineColor(android.graphics.Color.parseColor("#1565C0"))
        );
        style.addLayer(routeLayer);
    }
}

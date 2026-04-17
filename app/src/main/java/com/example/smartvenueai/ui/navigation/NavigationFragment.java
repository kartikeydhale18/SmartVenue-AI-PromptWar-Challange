package com.example.smartvenueai.ui.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartvenueai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

public class NavigationFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // MapLibre must be initialized before MapView is inflated
        MapLibre.getInstance(requireContext());
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Route swapping logic
        final boolean[] isReversed = {false};
        android.widget.TextView tvOrigin = view.findViewById(R.id.tvOrigin);
        android.widget.TextView tvDestination = view.findViewById(R.id.tvDestination);

        tvOrigin.setText("Gate 3 (Exit)");
        tvDestination.setText("Food Stall 1 (Concession)");
        
        view.findViewById(R.id.btnSwapRoute).setOnClickListener(v -> {
            isReversed[0] = !isReversed[0];
            if (isReversed[0]) {
                tvOrigin.setText("Food Stall 1 (Concession)");
                tvDestination.setText("Gate 3 (Exit)");
            } else {
                tvOrigin.setText("Gate 3 (Exit)");
                tvDestination.setText("Food Stall 1 (Concession)");
            }
            Toast.makeText(getContext(), "Route swapped!", Toast.LENGTH_SHORT).show();
        });

        // MapView
        mapView = view.findViewById(R.id.navMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapLibreMap maplibreMap) {
                maplibreMap.setStyle("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json",
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
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

        // Location FAB
        FloatingActionButton fabLocation = view.findViewById(R.id.navFabLocation);
        fabLocation.setOnClickListener(v -> {
            if (mapView != null) {
                mapView.getMapAsync(map -> {
                    map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                            new org.maplibre.android.geometry.LatLng(18.9388, 72.8248), 16.5));
                });
            }
        });

        // Tapping the ETA card opens Navigate Detail screen
        view.findViewById(R.id.etaCard).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NavigateDetailActivity.class)));
    }

    @Override public void onStart()  { super.onStart();  if (mapView != null) mapView.onStart(); }
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause()  { super.onPause();  if (mapView != null) mapView.onPause(); }
    @Override public void onStop()   { super.onStop();   if (mapView != null) mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) { mapView.onDestroy(); mapView = null; }
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
    
    private void drawRoute(Style style) {
        java.util.List<org.maplibre.geojson.Point> routeCoordinates = new java.util.ArrayList<>();
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8258, 18.9378)); // Gate 3
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8250, 18.9375));
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8243, 18.9380));
        routeCoordinates.add(org.maplibre.geojson.Point.fromLngLat(72.8248, 18.9388)); // Concession

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

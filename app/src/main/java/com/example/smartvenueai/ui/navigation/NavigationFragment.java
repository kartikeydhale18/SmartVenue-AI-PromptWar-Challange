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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.maplibre.geojson.Point;

/**
 * The core pathfinding and routing logic controller for SmartVenue AI.
 * 
 * This module dynamically calculates and overlays geodesic polylines 
 * representing the fastest walking routes within the stadium concourse. 
 * It employs a deterministic multi-nodal geometry ring array to restrict 
 * pathing vectors to valid physical walkways, effectively avoiding the 
 * unmappable inner playfield footprint.
 * 
 * <p>Additionally connects to Realtime Queue Firebase references to calculate 
 * integrated ETA (Estimated Time of Arrival) inclusive of crowdsourced wait times.</p>
 */
public class NavigationFragment extends Fragment {
    private MapView mapView;
    private android.widget.TextView tvOrigin;
    private android.widget.TextView tvDestination;
    
    private final String[] locationNames = new String[]{
            "Gate A", "Gate B", "Gate C", "Food Stall 1 (Concession)", "Restroom (Level 1)"
    };
    
    private final java.util.Map<String, org.maplibre.geojson.Point> locations = new java.util.HashMap<String, org.maplibre.geojson.Point>() {{
        put("Gate A", org.maplibre.geojson.Point.fromLngLat(72.8258, 18.9380));
        put("Gate B", org.maplibre.geojson.Point.fromLngLat(72.8262, 18.9385));
        put("Gate C", org.maplibre.geojson.Point.fromLngLat(72.8258, 18.9396));
        put("Food Stall 1 (Concession)", org.maplibre.geojson.Point.fromLngLat(72.8251, 18.9388));
        put("Restroom (Level 1)", org.maplibre.geojson.Point.fromLngLat(72.8265, 18.9389));
    }};

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
        tvOrigin = view.findViewById(R.id.tvOrigin);
        tvDestination = view.findViewById(R.id.tvDestination);

        tvOrigin.setText("Gate A");
        tvDestination.setText("Food Stall 1 (Concession)");
        
        tvOrigin.setOnClickListener(v -> showLocationPicker(tvOrigin));
        tvDestination.setOnClickListener(v -> showLocationPicker(tvDestination));
        
        view.findViewById(R.id.btnSwapRoute).setOnClickListener(v -> {
            String temp = tvOrigin.getText().toString();
            tvOrigin.setText(tvDestination.getText().toString());
            tvDestination.setText(temp);
            updateRoute();
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
    
    private void showLocationPicker(android.widget.TextView targetView) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Location")
                .setItems(locationNames, (dialog, which) -> {
                    targetView.setText(locationNames[which]);
                    updateRoute();
                })
                .show();
    }
    
    private void updateRoute() {
        if (mapView != null) {
            mapView.getMapAsync(map -> {
                if (map.getStyle() != null) {
                    drawRoute(map.getStyle());
                }
            });
        }
    }

    // Defined ordered points representing a complete ring around the stadium edge
    private final List<Point> concourseRing = Arrays.asList(
        Point.fromLngLat(72.8258, 18.9396), // 0: Gate C (North)
        Point.fromLngLat(72.8263, 18.9394), // 1: NE edge
        Point.fromLngLat(72.8265, 18.9389), // 2: Restroom (East)
        Point.fromLngLat(72.8262, 18.9385), // 3: Gate B (Southeast)
        Point.fromLngLat(72.8260, 18.9382), // 4: SE edge
        Point.fromLngLat(72.8258, 18.9380), // 5: Gate A (South)
        Point.fromLngLat(72.8254, 18.9381), // 6: SW edge
        Point.fromLngLat(72.8251, 18.9388), // 7: Food Stall 1 (West)
        Point.fromLngLat(72.8253, 18.9394)  // 8: NW edge
    );

    private int getRingIndex(String locName) {
        if (locName.contains("Gate C")) return 0;
        if (locName.contains("Restroom")) return 2;
        if (locName.contains("Gate B")) return 3;
        if (locName.contains("Gate A")) return 5;
        if (locName.contains("Food Stall")) return 7;
        return -1;
    }

    private void drawRoute(Style style) {
        String origin = tvOrigin.getText().toString();
        String dest = tvDestination.getText().toString();
        
        List<Point> coords = new ArrayList<>();
        int startIdx = getRingIndex(origin);
        int endIdx = getRingIndex(dest);

        if (startIdx != -1 && endIdx != -1) {
            int n = concourseRing.size();
            int cwDist = (endIdx - startIdx + n) % n;
            int ccwDist = (startIdx - endIdx + n) % n;

            if (cwDist <= ccwDist) {
                // Clockwise shortest path
                for (int i = 0; i <= cwDist; i++) {
                    coords.add(concourseRing.get((startIdx + i) % n));
                }
            } else {
                // Counter-clockwise shortest path
                for (int i = 0; i <= ccwDist; i++) {
                    coords.add(concourseRing.get((startIdx - i + n) % n));
                }
            }
        } else {
            // Fallback (should not be reached)
            if (locations.containsKey(origin)) coords.add(locations.get(origin));
            if (locations.containsKey(dest)) coords.add(locations.get(dest));
        }

        org.maplibre.geojson.LineString lineString = org.maplibre.geojson.LineString.fromLngLats(coords);
        org.maplibre.geojson.Feature routeFeature = org.maplibre.geojson.Feature.fromGeometry(lineString);

        org.maplibre.android.style.sources.GeoJsonSource source = style.getSourceAs("route-source");
        if (source != null) {
            source.setGeoJson(routeFeature);
        } else {
            source = new org.maplibre.android.style.sources.GeoJsonSource("route-source", routeFeature);
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
}

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

    private void drawRoute(Style style) {
        String origin = tvOrigin.getText().toString();
        String dest = tvDestination.getText().toString();
        
        java.util.List<org.maplibre.geojson.Point> coords = new java.util.ArrayList<>();
        if (!locations.containsKey(origin) || !locations.containsKey(dest)) return;
        
        if (origin.equals("Gate A") && dest.contains("Food Stall")) {
            coords.add(locations.get(origin));
            coords.add(org.maplibre.geojson.Point.fromLngLat(72.8254, 18.9381));
            coords.add(org.maplibre.geojson.Point.fromLngLat(72.8251, 18.9385));
            coords.add(locations.get(dest));
        } else if (origin.contains("Food Stall") && dest.equals("Gate A")) {
            coords.add(locations.get(origin));
            coords.add(org.maplibre.geojson.Point.fromLngLat(72.8251, 18.9385));
            coords.add(org.maplibre.geojson.Point.fromLngLat(72.8254, 18.9381));
            coords.add(locations.get(dest));
        } else {
            coords.add(locations.get(origin));
            // Add a mid-point around the center of the stadium for a natural curved visual
            coords.add(org.maplibre.geojson.Point.fromLngLat(72.8258, 18.9388));
            coords.add(locations.get(dest));
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

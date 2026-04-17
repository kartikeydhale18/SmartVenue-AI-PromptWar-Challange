package com.example.smartvenueai.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smartvenueai.MainActivity;
import com.example.smartvenueai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
    private static final String POI_SOURCE_ID = "POI_SOURCE_ID";
    private static final String POI_LAYER_ID = "POI_LAYER_ID";
    private static final String HEATMAP_SOURCE_ID = "HEATMAP_SOURCE_ID";
    private static final String HEATMAP_LAYER_ID = "HEATMAP_LAYER_ID";
    private static final LatLng DEFAULT_CENTER = new LatLng(18.9388, 72.8258); // Wankhede Stadium
    
    private boolean isHeatmapVisible = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted && mapLibreMap != null && mapLibreMap.getStyle() != null) {
                        enableLocationComponent(mapLibreMap.getStyle());
                        // Attempt to animate immediately if location becomes available
                        android.location.Location loc = mapLibreMap.getLocationComponent().getLastKnownLocation();
                        if (loc != null) {
                            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(loc.getLatitude(), loc.getLongitude()), 16.5));
                        }
                    } else if (!isGranted) {
                        Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // MapLibre must be initialized before MapView is inflated
        MapLibre.getInstance(requireContext());
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hamburger menu button opens the drawer
        ImageButton btnOpenDrawer = view.findViewById(R.id.btnOpenDrawer);
        btnOpenDrawer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // MapView setup
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapLibreMap maplibreMap) {
                MapFragment.this.mapLibreMap = maplibreMap;
                
                // Set initial camera position to Wankhede Stadium
                maplibreMap.setCameraPosition(new CameraPosition.Builder()
                        .target(DEFAULT_CENTER)
                        .zoom(16.5)
                        .build());
                        
                maplibreMap.setStyle("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json",
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                addMarkerIconToStyle(style);
                                addPoiLayer(style);
                                addHeatmapLayer(style);
                                enableLocationComponent(style);
                            }
                        });
                        
                maplibreMap.addOnMapClickListener(new MapLibreMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        return handleMapClick(point);
                    }
                });
            }
        });

        // My Location FAB
        FloatingActionButton fabMyLocation = view.findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(v -> {
            if (mapLibreMap != null && mapLibreMap.getLocationComponent().isLocationComponentActivated()) {
                android.location.Location location = mapLibreMap.getLocationComponent().getLastKnownLocation();
                if (location != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 16.5));
                } else {
                    Toast.makeText(getContext(), "Location not available yet", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please enable location access", Toast.LENGTH_SHORT).show();
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
        
        // Heatmap Toggle FAB
        FloatingActionButton fabHeatmap = view.findViewById(R.id.fabHeatmap);
        fabHeatmap.setOnClickListener(v -> {
            if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                CircleLayer layer = mapLibreMap.getStyle().getLayerAs(HEATMAP_LAYER_ID);
                if (layer != null) {
                    isHeatmapVisible = !isHeatmapVisible;
                    layer.setProperties(PropertyFactory.visibility(
                            isHeatmapVisible ? Property.VISIBLE : Property.NONE));
                    Toast.makeText(getContext(), isHeatmapVisible ? "Heatmap ON" : "Heatmap OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationComponent locationComponent = mapLibreMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.NONE);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
        // If not granted, we do NOT ask here on startup. We wait for the FAB click.
    }
    
    private void addMarkerIconToStyle(Style style) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker);
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            style.addImage(MARKER_IMAGE_ID, bitmap);
        }
    }
    
    private void addHeatmapLayer(Style style) {
        // Initialize empty source
        GeoJsonSource source = new GeoJsonSource(HEATMAP_SOURCE_ID, FeatureCollection.fromFeatures(new ArrayList<>()));
        style.addSource(source);

        CircleLayer heatmapLayer = new CircleLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID);
        heatmapLayer.setProperties(
                PropertyFactory.circleRadius(30f),
                PropertyFactory.circleColor(android.graphics.Color.parseColor("#E65100")),
                PropertyFactory.circleBlur(1.5f),
                PropertyFactory.circleOpacity(0.6f)
        );
        style.addLayerBelow(heatmapLayer, POI_LAYER_ID);

        // Coordinate dictionary mapping to String names from the reporting form
        java.util.Map<String, Point> locationMap = new java.util.HashMap<>();
        locationMap.put("Gate A", Point.fromLngLat(72.8258, 18.9380));
        locationMap.put("Gate B", Point.fromLngLat(72.8262, 18.9385));
        locationMap.put("Gate C", Point.fromLngLat(72.8258, 18.9396));
        locationMap.put("Food Stall 1 (Concession)", Point.fromLngLat(72.8251, 18.9388));
        locationMap.put("Restroom (Level 1)", Point.fromLngLat(72.8265, 18.9389));

        // Connect to Firestore live feed
        FirebaseFirestore.getInstance().collection("crowd_reports")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    
                    List<Feature> livePoints = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String locName = doc.getString("location");
                        if (locName != null && locationMap.containsKey(locName)) {
                            // Multiple points on same spot stack their opacity, creating intensity organically!
                            livePoints.add(Feature.fromGeometry(locationMap.get(locName)));
                        }
                    }
                    
                    if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                        GeoJsonSource liveSource = mapLibreMap.getStyle().getSourceAs(HEATMAP_SOURCE_ID);
                        if (liveSource != null) {
                            liveSource.setGeoJson(FeatureCollection.fromFeatures(livePoints));
                        }
                    }
                });
    }
    
    private void addPoiLayer(Style style) {
        Feature concession = Feature.fromGeometry(Point.fromLngLat(72.8251, 18.9388));
        concession.addStringProperty("title", "Food Stall 1 (Concession)");
        
        Feature restroom = Feature.fromGeometry(Point.fromLngLat(72.8265, 18.9389));
        restroom.addStringProperty("title", "Restroom (Level 1)");
        
        Feature gateA = Feature.fromGeometry(Point.fromLngLat(72.8258, 18.9380));
        gateA.addStringProperty("title", "Gate A");

        Feature gateB = Feature.fromGeometry(Point.fromLngLat(72.8262, 18.9385));
        gateB.addStringProperty("title", "Gate B");

        Feature gateC = Feature.fromGeometry(Point.fromLngLat(72.8258, 18.9396));
        gateC.addStringProperty("title", "Gate C");

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{
                concession, restroom, gateA, gateB, gateC
        });

        GeoJsonSource source = new GeoJsonSource(POI_SOURCE_ID, featureCollection);
        style.addSource(source);

        SymbolLayer symbolLayer = new SymbolLayer(POI_LAYER_ID, POI_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage(MARKER_IMAGE_ID),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconOffset(new Float[]{0f, -18f})
                );
        style.addLayer(symbolLayer);
    }
    
    private boolean handleMapClick(LatLng point) {
        PointF screenPoint = mapLibreMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapLibreMap.queryRenderedFeatures(screenPoint, POI_LAYER_ID);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);
            String title = feature.getStringProperty("title");
            if (title != null) {
                Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    // ── MapView lifecycle forwarding ──
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
}

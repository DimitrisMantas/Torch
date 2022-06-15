/*
 * Torch is an Android application for the optimal routing of offline
 * mobile devices.
 * Copyright (C) 2021-2022  DIMITRIS(.)MANTAS(@outlook.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.dimitrismantas.torch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.math.SupplementalMath;
import com.dimitrismantas.torch.ui.routing.RouteManager;
import com.dimitrismantas.torch.ui.map.MapController;
import com.dimitrismantas.torch.ui.map.markers.ExtendedMarkerItem;
import com.dimitrismantas.torch.ui.map.scalebar.ScaleBar;
import com.dimitrismantas.torch.ui.map.markers.extensions.DragListener;
import com.dimitrismantas.torch.ui.map.markers.extensions.ExtendedItemizedLayer;
import com.dimitrismantas.torch.ui.utils.AnimationManager;
import com.dimitrismantas.torch.ui.textinput.TextInputHandler;
import com.dimitrismantas.torch.utils.data.AssetManager;
import com.dimitrismantas.torch.utils.data.DataManager;
import com.dimitrismantas.torch.utils.data.FileManager;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;
import com.dimitrismantas.torch.utils.permissions.RuntimePermissionManager;
import com.dimitrismantas.torch.utils.multithreading.ThreadManager;

import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Main extends AppCompatActivity implements ItemizedLayer.OnItemGestureListener<MarkerInterface> {
    private static final String TAG = "Main";
    private static final double[] INITIAL_MAP_CENTER = {37.9838,23.7275};
    private static final int INITIAL_MAP_ZOOM_LEVEL = 12;
    private static final double[] GRAPH_BOUNDING_BOX = {36.4025066, 20.0121613, 41.7575614, 26.6283465};
    /**
     * The list containing all points currently on the map. It includes marker locations and route endpoints.
     *
     * @apiNote If the size of this list is equal two one, it then contains the location of the origin marker. Alternatively, if its size is equal two two, its zero-th element is the location of the origin marker, and the first that of the destination. Otherwise, the zero-th and last elements of the list are the locations of the origin and destination markers, respectively, with every other element being a route endpoint. Route endpoints are always added to the list in order from the source to the target.
     * @since 1.0.0
     */
    private final List<GeoPoint> pointsOnMap = new ArrayList<>();
    /**
     * A text field allowing the user to enter an origin and a destination.
     */
    private EditText odTextField;
    private ImageView odMarkerLegend;
    private MapView mapView;
    private org.oscim.map.Map map;
    private MapFileTileSource mapTileSource;
    private IRenderTheme mapTheme;
    private TextView routeAttributeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() ->  !RuntimePermissionManager.checkRuntimePermissionStatus(this));

        ThreadManager.unpackCriticalAssets(getApplicationContext());

        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        map = mapView.map();
        odTextField = findViewById(R.id.textInput);
        odMarkerLegend = findViewById(R.id.markerHint);
        routeAttributeTextView = findViewById(R.id.routeAttributes);
        // Instantiate the UI sections.
        instantiateMap(FileManager.concatenateNestedPaths(FileManager.getPrimaryStorageDevicePath(), "map/grc.map"));
        instantiateMapController();
        instantiateRoutingManager();
        // Unpack the non-critical application assets.

        ThreadManager.instantiateRoutingServices(getApplicationContext());
        final ImageButton helpButton = findViewById(R.id.info);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), Help.class);
                Main.this.startActivity(intent);
            }
        });
        odTextField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    final Editable editable = odTextField.getText();
                    switch (TextInputHandler.processEditable(editable)) {
                        case COORDINATE_PAIR:
                            final double[] geoRepresentation = TextInputHandler.getCoordinates(editable);
                            addMarkerUsingEditText(new GeoPoint(geoRepresentation[0], geoRepresentation[1]), odTextField, odMarkerLegend);
                            return true;
                        case VERTEX_LABEL:
                            final int intRepresentation = TextInputHandler.getIntegerRepresentation(editable);
                            // This check is required
                            if (!DataManager.areRoutingServicesAvailable()) {
                                // TODO - Think of a possibly better message.
                                Toast.makeText(getApplicationContext(), "Torch is still setting up. Please try again in a few moments.", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            final DeserializedVertex vertex = DataManager.getGraph().vertices(intRepresentation);
                            addMarkerUsingEditText(new GeoPoint(vertex.lat(), vertex.lon()), odTextField, odMarkerLegend);
                            return true;
                        case EMPTY_STRING:
                            return true;
                        case INVALID_STRING:
                            Toast.makeText(getApplicationContext(), "Please input a pair of geographic coordinates.", Toast.LENGTH_SHORT).show();
                            odTextField.getText().clear();
                            return true;
                    }
                }
                return false;
            }
        });
//        System.out.println( Runtime.getRuntime().totalMemory());
    }

    private void instantiateMap(String path) {
        setMapTileSource(path);
        createBaseMapLayers();
        // Zoom levels are expressed internally as powers of two.
        map.setMapPosition(new MapPosition(
                INITIAL_MAP_CENTER[0],
                INITIAL_MAP_CENTER[1],
                SupplementalMath.exp2(INITIAL_MAP_ZOOM_LEVEL)));
        // Disable roll.
        map.viewport().setMinRoll(0F);
        map.viewport().setMaxRoll(0F);
        // The map stops when the limit is at the center of the screen.
        map.viewport().setMapLimit(new BoundingBox(GRAPH_BOUNDING_BOX[0], GRAPH_BOUNDING_BOX[1], GRAPH_BOUNDING_BOX[2], GRAPH_BOUNDING_BOX[3]));
    }

    private void setMapTileSource(String path) {
        mapTileSource = new MapFileTileSource();
        FileInputStream mapTileInputStream = null;
        try {
            mapTileInputStream = (FileInputStream) getContentResolver().openInputStream(Uri.fromFile(new File(path)));
        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "Failed to load map tile source.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, e);
        }
        mapTileSource.setMapFileInputStream(mapTileInputStream);
    }

    private void createBaseMapLayers() {
        map.layers().add(new MapEventReceiver(map));
        final VectorTileLayer vectorTileLayer = map.setBaseMap(mapTileSource);
        map.layers().add(new BuildingLayer(map, vectorTileLayer));
        map.layers().add(new LabelLayer(map, vectorTileLayer));
        mapTheme = map.setTheme(VtmThemes.DEFAULT);
        addScaleBar();
        addMapDataCredits();
    }

    private void addScaleBar() {
        ScaleBar mapScaleBar = new ScaleBar(map);
        // TODO - Add a custom scale bar with a better style.
        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(map, mapScaleBar);
        mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
        mapScaleBarLayer.getRenderer().setOffset(
                // The first multiplier is equal to the required offset in dp.
                16 * CanvasAdapter.getScale(), 8 * CanvasAdapter.getScale());
        map.layers().add(mapScaleBarLayer);
    }

    private void addMapDataCredits() {
        TextView mapDataCredits = findViewById(R.id.mapDataCredit);
        mapDataCredits.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void instantiateMapController() {
        MapController mapController = new MapController(map, pointsOnMap);
        final ImageButton resetMapButton = this.findViewById(R.id.resetMapPosition);
        resetMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.resetMap(MapController.InputEvent.CLICK);
            }
        });
        resetMapButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View resetMapButtonView) {
                mapController.resetMap(MapController.InputEvent.LONG_CLICK);
                return true;
            }
        });
    }

    private void instantiateRoutingManager() {
        // Android throws a NullPointerException if this view is instantiated in onCreate()...
        final ImageView routingInProgressNotifier = findViewById(R.id.routingInProgressNotifier);
        final ImageButton clearButton = findViewById(R.id.clearRoute);
        final RouteManager routeManager = new RouteManager(this, map, pointsOnMap, odTextField, odMarkerLegend, routingInProgressNotifier, clearButton, routeAttributeTextView);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routeManager.deleteRoute();
            }
        });
        final ImageButton routeButton = findViewById(R.id.route);
        registerForContextMenu(routeButton);
        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routeManager.route(AStar.OptimizationMode.MINIMIZE_DISTANCE);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.route) {
            menu.setHeaderTitle("Optimization Modes");
            getMenuInflater().inflate(R.menu.menu_optimization_mode, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        final ImageView routingInProgressNotifier = findViewById(R.id.routingInProgressNotifier);
        final ImageButton clearButton = findViewById(R.id.clearRoute);
        final RouteManager routeManager = new RouteManager(this, map, pointsOnMap, odTextField, odMarkerLegend, routingInProgressNotifier, clearButton, routeAttributeTextView);
        if (item.getItemId() == R.id.minimizeDistance) {
            routeManager.route(AStar.OptimizationMode.MINIMIZE_DISTANCE);
            return true;
        } else if (item.getItemId() == R.id.minimizeTravelTime) {
            routeManager.route(AStar.OptimizationMode.MINIMIZE_TRAVEL_TIME);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void addMarkerUsingEditText(final GeoPoint location, final EditText editText, final ImageView legend) {
        MarkerMode mode = null;
        // No markers are present on the map. Add an origin, and change the appearance of the EditText and its accompanying ImageView accordingly.
        if (pointsOnMap.size() == 0) {
            mode = MarkerMode.ORIGIN;
        }
        // Only the origin marker is present on the map. Add a destination, and hide the EditText and ImageView from view.
        else if (pointsOnMap.size() == 1) {
            // The origin marker is present on the map.
            mode = MarkerMode.DESTINATION;
        }
        //  The text field will be hidden from view when there are two or more points present on the map. Since the user will not have access to it during this time, the marker mode can never be null.
        addMarkerUsingGUI(location, mode, editText, legend);
        new MapController(map, pointsOnMap).resetMap(MapController.InputEvent.CLICK);
    }

    private void addMarkerUsingGUI(final GeoPoint location, final MarkerMode mode, final EditText editText, final ImageView legend) {
        // Hide the keyboard.
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(odTextField.getWindowToken(), 0);
     isOutOfGraphBounds(location);
        final Bitmap icon;
        final String title;
        switch (mode) {
            case ORIGIN:
                icon = new AndroidBitmap(AssetManager.toBitmap(R.drawable.ic_origin_marker_green_30dp, getApplicationContext()));
                title = "Origin";
                editText.getText().clear();
                editText.setHint("Select a destination");
                legend.setImageResource(R.drawable.ic_destination_marker_red_30dp);
                break;
            case DESTINATION:
                icon = new AndroidBitmap(AssetManager.toBitmap(R.drawable.ic_destination_marker_red_30dp, getApplicationContext()));
                title = "Destination";
                AnimationManager.fade(odTextField, AnimationManager.FadeMode.OUT);
                AnimationManager.fade(odMarkerLegend, AnimationManager.FadeMode.OUT);
                break;
            default:
                Log.e("Map", String.format("The selected marker mode could not be recognized (%s).", mode));
                return;
        }
        final MarkerSymbol symbol = new MarkerSymbol(icon, MarkerSymbol.HotspotPlace.BOTTOM_CENTER);
        final String description = String.format(Locale.getDefault(), "(%4f, 4%f)", location.getLatitude(), location.getLongitude());
        DragListener listener = new DragListener() {
            @Override
            public void pickUp(ExtendedMarkerItem item, GeoPoint eventLocation) {
                RouteManager.setIsRouteOnMap(false);
                // TODO - Close the marker's info bubble.
            }

            @Override
            public void drag(ExtendedMarkerItem item, GeoPoint eventLocation) {
            }

            @Override
            public void drop(ExtendedMarkerItem item, GeoPoint eventLocation) {
                switch (mode) {
                    case ORIGIN:
                       isOutOfGraphBounds(eventLocation);
                        invalidatePointOnMap(0, eventLocation);
                        break;
                    case DESTINATION:
                       isOutOfGraphBounds(eventLocation);
                        invalidatePointOnMap(1, eventLocation);
                        break;
                    default:
                        Log.e("Map", String.format("The selected marker mode could not be recognized (%s).", mode));
                }
            }
        };
        final ExtendedMarkerItem item = new ExtendedMarkerItem(title, description, location, listener);
        final ItemizedLayer layer = new ExtendedItemizedLayer(map, new ArrayList<>(1), symbol, this);
        layer.addItem(item);
        map.layers().add(layer);
        map.updateMap();
        pointsOnMap.add(location);
    }

    private void invalidatePointOnMap(final int i, final GeoPoint newPoint) {
        pointsOnMap.remove(i);
        pointsOnMap.add(i, newPoint);
    }

    private void isOutOfGraphBounds(final GeoPoint point) {
        final double lat = point.getLatitude();
        final double lon = point.getLongitude();
        if (lat < GRAPH_BOUNDING_BOX[0] || lat > GRAPH_BOUNDING_BOX[2] || lon < GRAPH_BOUNDING_BOX[1] || lon > GRAPH_BOUNDING_BOX[3]) {
            Toast.makeText(this, "The marker is out of bounds. Routing services will be provided from the nearest point.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        mapTheme.dispose();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public boolean onItemSingleTapUp(int index, MarkerInterface item) {
        MarkerItem markerItem = (MarkerItem) item;
        if (markerItem.getMarker() == null) {
            // TODO - Enable an info bubble.
//            Toast.makeText(getApplicationContext(), "Marker clicked.", Toast.LENGTH_SHORT).show();
        } else {
            markerItem.setMarker(null);
        }
        return true;
    }

    @Override
    public boolean onItemLongPress(int index, MarkerInterface item) {
        return true;
    }

    private enum MarkerMode {
        ORIGIN, DESTINATION
    }

    private final class MapEventReceiver extends Layer implements GestureListener {
        public MapEventReceiver(final org.oscim.map.Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Tap) {
                // TODO - Close all active info bubbles.
//                Toast.makeText(getApplicationContext(), "Closing all active info bubbles...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (g instanceof Gesture.LongPress) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                switch (pointsOnMap.size()) {
                    case 0:  // No markers.
                        addMarkerUsingGUI(p, MarkerMode.ORIGIN, odTextField, odMarkerLegend);
                        break;
                    case 1:  // One marker.
                        addMarkerUsingGUI(p, MarkerMode.DESTINATION, odTextField, odMarkerLegend);
                        break;
                    case 2:  // Two markers.
                        Toast.makeText(getApplicationContext(), "You have already selected both an origin and a destination.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Please delete the previous routing results to continue.", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            } else if (g instanceof Gesture.TwoFingerTap) {
                new MapController(map, pointsOnMap).zoomOut();
                return true;
            }
            return false;
        }
    }
}

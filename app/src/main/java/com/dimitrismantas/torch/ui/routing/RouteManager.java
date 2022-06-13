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
package com.dimitrismantas.torch.ui.routing;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.dimitrismantas.torch.R;
import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.EqualEndpointException;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.UnreachableTargetException;
import com.dimitrismantas.torch.core.main.utils.NearestNeighborSearch;
import com.dimitrismantas.torch.ui.map.MapController;
import com.dimitrismantas.torch.ui.utils.AnimationManager;
import com.dimitrismantas.torch.utils.data.AssetManager;
import com.dimitrismantas.torch.utils.data.DataManager;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A class containing the logic of the routing section of the app's UI.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RouteManager {
    /**
     * The class log tag.
     *
     * @since 1.0.0
     */
    private static final String TAG = "RoutingManager";
    /**
     * The ratio of the DPI of the current device's screen to the default of 160.
     *
     * @since 1.0.0
     */
    private static final float DPI_SCALE = CanvasAdapter.getScale();
    /**
     * The index of the map layer list, at which the route legs will be added.
     *
     * @apiNote This value depends on the number and ordering of the base map layers and needs to be computed manually.
     * @since 1.0.0
     */
    private static final int ROUTE_LEG_INSERTION_INDEX = 4;
    /**
     * The stroke weight of the route in pixels.
     *
     * @since 1.0.0
     */
    private static final float STROKE_WEIGHT = 4.5f * DPI_SCALE;
    /**
     * Declares if a route (including the corresponding origin and destination markers) is already present on the map.
     *
     * @since 1.0.0
     */
    private static boolean isRouteOnMap = false;
    /**
     * The activity instantiating and calling this class.
     *
     * @since 1.0.0
     */
    private final Activity parent;
    /**
     * The  map.
     *
     * @since 1.0.0
     */
    private final Map map;
    /**
     * The list containing all points currently on the map. It includes marker locations and route endpoints.
     *
     * @apiNote If the size of this list is equal two one, it then contains the location of the origin marker. Alternatively, if its size is equal two two, its zero-th element is the location of the origin marker, and the first that of the destination. Otherwise, the zero-th and last elements of the list are the locations of the origin and destination markers, respectively, with every other element being a route endpoint. Route endpoints are always added to the list in order from the source to the target.
     * @since 1.0.0
     */
    private final List<GeoPoint> pointsOnMap;
    /**
     * The routing-in-progress notifier.
     *
     * @since 1.0.0
     */
    private final ImageView routingInProgressNotifier;
    private final ImageButton deleteRouteButton;
    /**
     * The application context.
     *
     * @since 1.0.0
     */
    private final Context appContext;
    /**
     * The application resources.
     *
     * @since 1.0.0
     */
    private final Resources appResources;
    private final EditText odTextField;
    private final ImageView odMarkerLegend;
    private final TextView routeAttributes;
    /**
     * The exception (if any) thrown by the app during the routing process.
     *
     * @since 1.0.0
     */
    private Exception routingException;
    /**
     * The origin.
     *
     * @since 1.0.0
     */
    private GeoPoint origin;
    /**
     * The source.
     *
     * @since 1.0.0
     */
    private GeoPoint source;
    /**
     * The target.
     *
     * @since 1.0.0
     */
    private GeoPoint target;
    /**
     * The destination.
     *
     * @since 1.0.0
     */
    private GeoPoint destination;
    private DeserializedVertex sourceVertex;
    private DeserializedVertex targetVertex;

    /**
     * Instantiates this class.
     *
     * @param parent                    The activity instantiating and calling this class.
     * @param map                       The map.
     * @param pointsOnMap               The list containing all points currently on the map. It includes marker locations and route endpoints.
     * @param routingInProgressNotifier The routing-in-progress notifier.
     * @since 1.0.0
     */
    public RouteManager(final Activity parent, final Map map, final List<GeoPoint> pointsOnMap, final EditText odTextField, final ImageView odMarkerLegend, final ImageView routingInProgressNotifier, final ImageButton deleteRouteButton, final TextView routeAttributes) {
        this.parent = parent;
        this.map = map;
        this.pointsOnMap = pointsOnMap;
        this.odTextField = odTextField;
        this.odMarkerLegend = odMarkerLegend;
        this.routingInProgressNotifier = routingInProgressNotifier;
        this.deleteRouteButton = deleteRouteButton;
        this.routeAttributes = routeAttributes;
        this.appContext = parent.getApplicationContext();
        this.appResources = parent.getResources();
    }

    public static void setIsRouteOnMap(boolean isRouteOnMap) {
        RouteManager.isRouteOnMap = isRouteOnMap;
    }

    public void route(final AStar.OptimizationMode optimizationMode) {
        switch (this.pointsOnMap.size()) {
            case 0:
                Toast.makeText(appContext, "Please select an origin.", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(appContext, "Please select a destination.", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                // A set of routing results can contain only two points on the map. This can happen if the origin and destination are also the endpoints of a single edge. This check ensures that the user will never be able to route when previous routing results are still on the map.
                if (isRouteOnMap) {
                    promptRouteDeletion();
                    break;
                }
                // TODO - Fix the markers in place.
                origin = pointsOnMap.get(0);
                destination = pointsOnMap.get(1);
                // This whole block is copied from StackOverflow; I have no idea how it works.
                final ExecutorService aStarExecutor = Executors.newSingleThreadExecutor();
                final Handler messageLooper = new Handler(Looper.getMainLooper());
                // Work is now being done on the A* thread.
                aStarExecutor.execute(() -> {
                    if (!DataManager.areRoutingServicesAvailable()) {
                        messageLooper.post(() -> Toast.makeText(appContext, "Torch is still setting up. Please try again in a few moments.", Toast.LENGTH_LONG).show());
                        return;
                    }

                    messageLooper.post(() -> {
                        // Activate the routing-in-progress notifier.
                        AnimationManager.spin(routingInProgressNotifier);
                        // TODO - Change the behavior of the delete-route button to make it double as a cancel-routing button.
                        deleteRouteButton.setOnClickListener(v -> {
                            // Do nothing so the user can't delete the origin and destination markers.
                        });
                    });
                    final ExecutorService nearestNeighborSearchExecutor = Executors.newFixedThreadPool(2);
                    final NearestNeighborSearch nearestNeighborSearch = DataManager.getNearestNeighborSearch();
                    // Work is now being done on the Nearest Neighbor Search threads.
                    nearestNeighborSearchExecutor.execute(() -> sourceVertex = nearestNeighborSearch.run(origin.getLatitude(), origin.getLongitude()));
                    nearestNeighborSearchExecutor.execute(() -> targetVertex = nearestNeighborSearch.run(destination.getLatitude(), destination.getLongitude()));
                    nearestNeighborSearchExecutor.shutdown();
                    try {
                        nearestNeighborSearchExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        messageLooper.post(() -> Toast.makeText(appContext, "An unexpected error has occurred. Please try again.", Toast.LENGTH_LONG).show());
                        isRouteOnMap = false;
                        return;
                    }
                    isRouteOnMap = true;
                    routingException=null;
                    try {
                        // Do not factor the graph out to a new variable!
                        DataManager.setRoute(DataManager.getaStar().run(DataManager.getGraph().vertices(sourceVertex.lbl()), DataManager.getGraph().vertices(targetVertex.lbl()), optimizationMode));
                    } catch (final EqualEndpointException | UnreachableTargetException e) {
                        routingException = e;
                        Log.w(TAG, e);
                    }
                    // Work is now being done on the main thread.
                    messageLooper.post(() -> {
                        if (routingException != null && routingException instanceof EqualEndpointException) {
                            Toast.makeText(appContext, "The origin and destination are too close to each other.", Toast.LENGTH_SHORT).show();
                        } else if (routingException != null && routingException instanceof UnreachableTargetException) {
                            Toast.makeText(appContext, "The destination is unreachable from the source.", Toast.LENGTH_SHORT).show();
                        } else {
                            final Path route = DataManager.getRoute();
                            System.out.println("HERE");
                            routeAttributes.setText(new RouteAttributeStringBuilder(optimizationMode, route, appContext).buildRouteAttributeString());
                            AnimationManager.slide(routeAttributes, AnimationManager.SlideMode.DOWN, appContext);
                            // These two lines must be in this order because draw route uses points on map.
                            pointsOnMap.addAll(1, DataManager.toGeoPoints(route.getEndpoints()));
                            drawRoute(DataManager.toGeoPoints(route.getEndpoints()));
                        }
                        AnimationManager.makeDisappear(routingInProgressNotifier);
                        // Revert the behavior of the delete-route button to its previous state.
                        deleteRouteButton.setOnClickListener(v -> deleteRoute());
                    });
                });
                aStarExecutor.shutdown();
                break;
            default:
                promptRouteDeletion();
        }
    }

    /**
     * Prompts the user to delete a route already present on the map.
     *
     * @since 1.0.0
     */
    private void promptRouteDeletion() {
        Toast.makeText(appContext, "Please delete the previous route to continue.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Draws a route on the map given its endpoints.
     *
     * @param endpoints The endpoints of the route to be drawn.
     * @apiNote The route legs from the origin to the source and from the target to the destination are represented by straight dashed lines of dark color, while the main route leg (i.e., from the source to the target) is drawn using a solid line of lighter color.
     * @implNote Round stroke corners are not supported.
     * @see #route(AStar.OptimizationMode)
     * @see #maskRoute()
     * @since 1.0.0
     */
    private void drawRoute(final List<GeoPoint> endpoints) {
        source = endpoints.get(0);
        target = endpoints.get(endpoints.size() - 1);
        // Create the leg from the source to the target.
        final int mainLegColor = ResourcesCompat.getColor(appResources, R.color.colorSecondary, null);
        final PathLayer sToTLeg = new PathLayer(map, mainLegColor, STROKE_WEIGHT);
        sToTLeg.setPoints(endpoints);
        // Create the route legs from the origin to the source and from the source to the destination.
        final int secondaryLegColor = ResourcesCompat.getColor(appResources, R.color.colorSecondaryVariant, null);
        final Style secondaryLegStyle = Style.builder()
                // The stroke color also doubles as the gap "color".
                .strokeColor(Color.TRANSPARENT).strokeWidth(STROKE_WEIGHT)
                // This method sets the gap length between two consecutive dashes in pixels.
                .stipple(12).stippleColor(secondaryLegColor)
                // This method sets the width of each dash relative to the selected stroke width.
                .stippleWidth(1).build();
        final PathLayer oToSLeg = new PathLayer(map, secondaryLegStyle);
        // Since there is no information available as to how to get from the origin to the source or from the target to the destination, they'll be connected by a straight line.
        // TODO - Convert this line to a geodesic or a second-order polynomial like in Google Maps.
        oToSLeg.setPoints(Arrays.asList(origin, source));
        final PathLayer tToDLeg = new PathLayer(map, secondaryLegStyle);
        tToDLeg.setPoints(Arrays.asList(target, destination));
        // Add each route leg to the map layers in reverse order of traversal, between the building and label layers. The insertion index must be defined during the development process.
        map.layers().add(ROUTE_LEG_INSERTION_INDEX, tToDLeg);
        map.layers().add(ROUTE_LEG_INSERTION_INDEX, sToTLeg);
        map.layers().add(ROUTE_LEG_INSERTION_INDEX, oToSLeg);
        maskRoute();
        new MapController(map, pointsOnMap).resetMap(MapController.InputEvent.CLICK);
    }

    private void maskRoute() {
        final Bitmap icon = new AndroidBitmap(AssetManager.toBitmap(R.drawable.ic_route_mask_30dp, appContext));
        final MarkerSymbol symbol = new MarkerSymbol(icon, MarkerSymbol.HotspotPlace.CENTER);
        final ItemizedLayer layer = new ItemizedLayer(map, symbol);
        final MarkerItem originMask = new MarkerItem(null, null, origin);
        layer.addItem(originMask);
        final MarkerItem sourceMask = new MarkerItem(null, null, source);
        layer.addItem(sourceMask);
        final MarkerItem targetMask = new MarkerItem(null, null, target);
        layer.addItem(targetMask);
        final MarkerItem destinationMask = new MarkerItem(null, null, destination);
        layer.addItem(destinationMask);
        // There are three route legs in total.
        map.layers().add(ROUTE_LEG_INSERTION_INDEX + 3, layer);
    }

    public void deleteRoute() {
        isRouteOnMap = false;
        switch (pointsOnMap.size()) {
            case 0:
                break;
            case 1:
                // The markers are placed on top of every other map layer.
                deleteTopmostMapLayer();
                odTextField.setHint("Select an origin");
                odMarkerLegend.setImageResource(R.drawable.ic_origin_marker_green_30dp);
                break;
            case 2:
                deleteMarkers();
                odTextField.setHint("Select an origin");
                odMarkerLegend.setImageResource(R.drawable.ic_origin_marker_green_30dp);
                AnimationManager.fade(odTextField, AnimationManager.FadeMode.IN);
                AnimationManager.fade(odMarkerLegend, AnimationManager.FadeMode.IN);
                break;
            default:
                deleteMarkers();
                // There are four routing layers in total (i.e., three for the route, and one for its mask).
                for (int i = 0; i < 4; i++) {
                    map.layers().remove(ROUTE_LEG_INSERTION_INDEX);
                }
                AnimationManager.slide(routeAttributes, AnimationManager.SlideMode.UP, appContext);
                odTextField.getText().clear();
                odTextField.setHint("Select an origin");
                odMarkerLegend.setImageResource(R.drawable.ic_origin_marker_green_30dp);
                AnimationManager.fade(odTextField, AnimationManager.FadeMode.IN);
                AnimationManager.fade(odMarkerLegend, AnimationManager.FadeMode.IN);
        }
        map.updateMap();
        pointsOnMap.clear();
    }

    private void deleteTopmostMapLayer() {
        map.layers().remove(map.layers().size() - 1);
    }

    private void deleteMarkers() {
        for (int i = 0; i < 2; i++) {
            deleteTopmostMapLayer();
        }
    }
}

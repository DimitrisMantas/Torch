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
package com.dimitrismantas.torch.core.main.engine;

import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.EqualEndpointException;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.UnreachableTargetException;
import com.dimitrismantas.torch.core.main.engine.utils.heuristics.GreatCircleDistanceHeuristic;
import com.dimitrismantas.torch.core.main.engine.utils.heuristics.AbstractHeuristic;
import com.dimitrismantas.torch.core.main.engine.utils.heuristics.TravelTimeHeuristic;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedEdge;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedGraph;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;
import com.dimitrismantas.torch.core.utils.serialization.DeserializationManager;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class AStar {
    private final DeserializedGraph graph;
    private AbstractHeuristic heuristic;
    private Path route;
    private PriorityQueue<DeserializedVertex> pq;
    private short numExecs;

    public AStar(final DeserializedGraph graph) {
        this.graph = graph;
    }

    @SuppressWarnings("ConstantConditions")
    public Path run(final DeserializedVertex source, final DeserializedVertex target, final OptimizationMode optMode) throws UnreachableTargetException, EqualEndpointException {
        invalidatePreviousExecution(target, optMode);
        // This can happen if the origin and destination are sufficiently close to each other.
        if (DeserializationManager.equals(source, target)) {
            throw new EqualEndpointException("The source and target vertices are equal.");
        }
        // The correct value of the estimated cost to from the source vertex to the target is equal to the corresponding value of the appropriate heuristic. However, since the priority queue is initially empty, the source is guaranteed to be dequeued first.
        initialize(source, -1, 0, 0);

        int N = 0;

        while (!pq.isEmpty()) {
            final DeserializedVertex curr = pq.poll();
            // Since there might be two "copies" of the current vertex in the priority queue, we must be able to differentiate between them so that we use the correct one (i.e., the one with the minimum key).
            if (curr.pqKey() > curr.actualCostFromSource() + heuristic.calc(curr)) {
                continue;
            }

            N++;
            route.setN(N);

            if (DeserializationManager.equals(curr, target)) {
                populatePath(source, curr);
                break;
            }
            for (int i = 0; i < curr.outgoingEdgesLength(); i++) {
                final DeserializedEdge outEdge = curr.outgoingEdges(i);
                final DeserializedVertex adj = graph.vertices(outEdge.endVertexLabel());
                int costFromSource = curr.actualCostFromSource();
                switch (optMode) {
                    case MINIMIZE_DISTANCE:
                        costFromSource += outEdge.length();
                        break;
                    case MINIMIZE_TRAVEL_TIME:
                        costFromSource += outEdge.travelTime();
                        break;
                }
                final int costToTarget = heuristic.calc(adj);
                if (adj.numInitialized() == numExecs) {
                    relax(curr, adj, costFromSource, costToTarget);
                } else {
                    initialize(adj, curr.lbl(), costFromSource, costToTarget);
                }
            }
        }
        if (route.getEndpoints().isEmpty()) {
            throw new UnreachableTargetException("The target vertex is unreachable from the source.");
        }
        return route;
    }

    private void invalidatePreviousExecution(final DeserializedVertex target, final OptimizationMode optMode) {
        heuristic = getHeuristic(target, optMode);
        route = new Path();
        pq = new PriorityQueue<>(Comparator.comparingInt(DeserializedVertex::pqKey));
        numExecs++;
    }

    private void initialize(final DeserializedVertex v, final int predLbl, final int costFromSource, final int costToTarget) {
        v.mutateActualCostFromSource(costFromSource);
        v.mutateNumInitialized(numExecs);
        v.mutatePqKey(costFromSource + costToTarget);
        v.mutatePredecessorLabel(predLbl);
        pq.add(v);
    }

    private void relax(final DeserializedVertex v, final DeserializedVertex adj, final int costFromSource, final int costToTarget) {
        if (costFromSource < adj.actualCostFromSource()) {
            adj.mutateActualCostFromSource(costFromSource);
            adj.mutatePqKey(costFromSource + costToTarget);
            adj.mutatePredecessorLabel(v.lbl());
            // Add a "duplicate" vertex to the priority queue, whose key is smaller than that of its copy. This means that between these two vertices, this one will be dequeued first.
            pq.add(adj);
        }
    }

    private void populatePath(final DeserializedVertex source, DeserializedVertex current) {
        while (current.predecessorLabel() != -1) {
            route.addEndpoint(current);
            current = graph.vertices(current.predecessorLabel());
        }
        // The source vertex is originally not included as an endpoint, since it has no predecessor.
        route.addEndpoint(source);
        // Using each endpoint, increment the route length and travel time accordingly.
        for (int i = 0; i < route.getEndpoints().size() - 1; i++) {
            final DeserializedVertex endpoint = graph.vertices(route.getEndpoints().get(i).lbl());
            for (int j = 0; j < endpoint.outgoingEdgesLength(); j++) {
                final DeserializedEdge outEdge = endpoint.outgoingEdges(j);
                if (outEdge.endVertexLabel() == route.getEndpoints().get(i + 1).lbl()) {
                    route.incrementLength(outEdge.length());
                    route.incrementTravelTime(outEdge.travelTime());
                    break;
                }
            }
        }
    }

    private AbstractHeuristic getHeuristic(final DeserializedVertex target, final OptimizationMode optMode) {
        switch (optMode) {
            case MINIMIZE_DISTANCE:
                return new GreatCircleDistanceHeuristic(target);
            case MINIMIZE_TRAVEL_TIME:
                return new TravelTimeHeuristic(target);
            default:
                return null;
        }
    }

    public enum OptimizationMode {
        MINIMIZE_DISTANCE, MINIMIZE_TRAVEL_TIME
    }
}

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
import com.dimitrismantas.torch.core.main.engine.utils.priorityqueue.PriorityQueueEntry;
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
    private PriorityQueue<PriorityQueueEntry> priorityQueue;
    private short numExecutions;

    public AStar(final DeserializedGraph graph) {
        this.graph = graph;
    }


    public Path run(final DeserializedVertex source, final DeserializedVertex target, final OptimizationMode optimizationMode) {
        invalidatePreviousExecution(target, optimizationMode);
        // This can happen if the origin and destination are so close to each other that their nearest neighbors are equal.
        if (DeserializationManager.equals(source, target)) {
            throw new EqualEndpointException("The source and target vertices are equal.");
        }
        // The correct value of the estimated cost to from the source vertex to the target is equal to the corresponding value of the appropriate heuristic. However, since the priority queue is initially empty, the source is guaranteed to be dequeued first.
        initialize(source, -1, 0, 0);

        while (!priorityQueue.isEmpty()) {
            final PriorityQueueEntry entry =  priorityQueue.poll();
            final DeserializedVertex curr = graph.vertices(entry.getVertexLabel());
            // Since there might be two "copies" of the current vertex in the priority queue, we must be able to differentiate between them so that we use the correct one (i.e., the one with the minimum key).
            if (entry.getKey() > curr.actualCostFromSource()+heuristic.calc(curr)) {
                continue;
            }

            if (DeserializationManager.equals(curr, target)) {
                populatePath(source, curr);
                break;
            }
            for (int i = 0; i < curr.outgoingEdgesLength(); i++) {
                final DeserializedEdge outEdge = curr.outgoingEdges(i);
                final DeserializedVertex adj = graph.vertices(outEdge.endVertexLabel());
                int costFromSource = curr.actualCostFromSource();
                switch (optimizationMode) {
                    case MINIMIZE_DISTANCE:
                        costFromSource += outEdge.length();
                        break;
                    case MINIMIZE_TRAVEL_TIME:
                        costFromSource += outEdge.travelTime();
                        break;
                }
                final int costToTarget = heuristic.calc(adj);
                if (adj.numInitialized() == numExecutions) {
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
        priorityQueue = new PriorityQueue<>(Comparator.comparingInt(PriorityQueueEntry::getKey));
        numExecutions++;
    }

    private void initialize(final DeserializedVertex vertex, final int predecessorLabel, final int costFromSource, final int costToTarget) {
        vertex.mutateActualCostFromSource(costFromSource);
        vertex.mutateNumInitialized(numExecutions);
        vertex.mutatePredecessorLabel(predecessorLabel);
        priorityQueue.add(new PriorityQueueEntry(costFromSource+costToTarget, vertex.lbl()));

    }

    private void relax(final DeserializedVertex vertex, final DeserializedVertex adjacent, final int costFromSource, final int costToTarget) {
        if (costFromSource < adjacent.actualCostFromSource()) {
            adjacent.mutateActualCostFromSource(costFromSource);
            adjacent.mutatePredecessorLabel(vertex.lbl());
            // Add a "duplicate" vertex to the priority queue, whose key is smaller than that of its copy. This means that between these two vertices, this one will be dequeued first.
            priorityQueue.add(new PriorityQueueEntry(costFromSource+costToTarget, adjacent.lbl()));
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

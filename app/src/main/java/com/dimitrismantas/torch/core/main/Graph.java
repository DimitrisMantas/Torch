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
package com.dimitrismantas.torch.core.main;

import com.dimitrismantas.torch.core.utils.serialization.DeserializedEdge;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedGraph;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;
import com.google.flatbuffers.FlatBufferBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Graph {
    private static final String COLUMN_DELIMITER = " ";
    private final List<Vertex> vertices = new ArrayList<>(8436216);

    public Graph(final String vdfName, final String edfName) {
        importVertices(vdfName);
        importEdges(edfName);
    }

    private void importVertices(final String vdfName) {
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(vdfName))) {
            String elementDefinition;
            while ((elementDefinition = bufferedReader.readLine()) != null) {
                final String[] elementAttributes = elementDefinition.split(COLUMN_DELIMITER);
                vertices.add(new Vertex(Integer.parseInt(elementAttributes[0]), Float.parseFloat(elementAttributes[1]), Float.parseFloat(elementAttributes[2])));
            }
        } catch (final IOException e) {
            // TODO - This exception should be logged as a warning after migrating the backend to Android.
            e.printStackTrace();
        }
    }

    private void importEdges(final String edfName) {
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(edfName))) {
            String elementDefinition;
            while ((elementDefinition = bufferedReader.readLine()) != null) {
                final String[] elementAttributes = elementDefinition.split(COLUMN_DELIMITER);
                final Vertex startVertex = vertices.get(Integer.parseInt(elementAttributes[0]));
                final Vertex endVertex = vertices.get(Integer.parseInt(elementAttributes[1]));
                final short length = Short.parseShort(elementAttributes[2]);
                final short travelTime = Short.parseShort(elementAttributes[3]);
                vertices.get(startVertex.getLabel()).addOutboundEdge(new Edge(endVertex.getLabel(), length, travelTime));
                if (Integer.parseInt(elementAttributes[4]) == 1 && Integer.parseInt(elementAttributes[5]) == 1) {
                    vertices.get(endVertex.getLabel()).addOutboundEdge(new Edge(startVertex.getLabel(), length, travelTime));
                }
            }
        } catch (final IOException e) {
            // TODO - This exception should be logged as a warning after migrating the backend to Android.
            e.printStackTrace();
        }
    }

    // TODO - Write this method according to https://github.com/google/flatbuffers/blob/master/tests/JavaTest.java.
    public void serialize(final String outputFilePath) {
        final FlatBufferBuilder builder = new FlatBufferBuilder().forceDefaults(true);
        final int[] vertexData = new int[this.vertices.size()];
        for (int i = 0; i < this.vertices.size(); i++) {
            final Vertex vertex = this.vertices.get(i);
            final List<Edge> outgoingEdges = vertex.getOutEdges();
            final int numOutgoingEdges = vertex.getOutEdges().size();
            final int[] edgeData = new int[numOutgoingEdges];
            for (int j = 0; j < outgoingEdges.size(); j++) {
                final Edge edge = outgoingEdges.get(j);
                final int edgeOffset = DeserializedEdge.createDeserializedEdge(builder, edge.getEndVertexLabel(), edge.getLength(), edge.getTravelTime());
                edgeData[j] = edgeOffset;
            }
            final int edgesOffset = DeserializedVertex.createOutgoingEdgesVector(builder, edgeData);
            final int vertexOffset = DeserializedVertex.createDeserializedVertex(builder, vertex.getLabel(), vertex.getLatitude(), vertex.getLongitude(), vertex.getNumInitialized(), vertex.getActualCostFromSource(), vertex.getPredecessorLabel(), edgesOffset);
            vertexData[i] = vertexOffset;
        }
        final int verticesOffset = DeserializedGraph.createVerticesVector(builder, vertexData);
        final int graphOffset = DeserializedGraph.createDeserializedGraph(builder, verticesOffset);
        builder.finish(graphOffset);
        final byte[] serializedData = builder.sizedByteArray();
        try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
            outputStream.write(serializedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 * Torch is a model open-source Android application for optimal routing
 * in offline mobile devices.
 * Copyright (C) 2021-2022  DIMITRIS(.)MANTAS(@outlook.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.dimitrismantas.torch;

import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.main.utils.NearestNeighborSearch;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedGraph;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class UnitTests {
    @Test
    public void routingTest() {
        final File serializedGraphPath = new File("C:\\Documents\\Torch\\app\\src\\main\\assets\\bin\\grc.bin");

        MappedByteBuffer bb = null;
        try (final FileChannel fChannel = new RandomAccessFile(serializedGraphPath, "rw").getChannel()) {
            bb = fChannel.map(FileChannel.MapMode.PRIVATE, 0, fChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DeserializedGraph graph = DeserializedGraph.getRootAsDeserializedGraph(bb);

        final double[] origin = {37.9838, 23.7275};
        final double[] destination = {40.6401, 22.9444};

        final NearestNeighborSearch nns = new NearestNeighborSearch(graph);
        final DeserializedVertex s = nns.run(origin[0], origin[1]);
        final DeserializedVertex t = nns.run(destination[0], destination[1]);

        final AStar aStar = new AStar(graph);
        final long t0 = System.nanoTime();
        final Path route = aStar.run(s, t, AStar.OptimizationMode.MINIMIZE_DISTANCE);
        System.out.println((System.nanoTime() - t0) * 1e-6);
        System.out.println(route.getEndpoints().size());
    }
}
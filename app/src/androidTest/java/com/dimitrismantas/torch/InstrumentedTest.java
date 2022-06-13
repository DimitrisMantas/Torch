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

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.EqualEndpointException;
import com.dimitrismantas.torch.core.main.engine.utils.exceptions.UnreachableTargetException;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;
import com.dimitrismantas.torch.utils.data.DataManager;
import com.dimitrismantas.torch.utils.data.FileManager;
import com.dimitrismantas.torch.utils.multithreading.ThreadManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    private static final String LOG_TAG = "InstrumentedTest";
    private static final int NUM_AVERAGED_TRIALS = 1000;
    private static final int NUM_TRIALS = 3;
    private static final int NUM_WARMUP_TRIALS = NUM_AVERAGED_TRIALS / 10;
    private static final int NUM_VERTICES = 8436216;
    private static final double TO_MILLI = 1e-6d;

    private static void warmupDevice() {
        ThreadManager.instantiateRoutingServices(InstrumentationRegistry.getInstrumentation().getTargetContext());
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final SupplementalMath math = new SupplementalMath();
        for (int i = 0; i < NUM_WARMUP_TRIALS; i++) {
            final DeserializedVertex source = DataManager.getGraph().vertices(math.random(0, NUM_VERTICES));
            final DeserializedVertex target = DataManager.getGraph().vertices(math.random(0, NUM_VERTICES));
            try {
                DataManager.getaStar().run(source, target, AStar.OptimizationMode.MINIMIZE_DISTANCE);
                Log.d(LOG_TAG, String.format("Warmup Trials Completed: %d", i + 1));
            } catch (UnreachableTargetException | EqualEndpointException e) {
                Log.w(LOG_TAG, e);
                i--;
            }
        }
    }

    private static void writeToFile(final List<Double> list, final String pathname) {
        try (final FileWriter writer = new FileWriter(FileManager.getPrimaryStorageDevicePath() + pathname)) {
            for (final double measurement : list) {
                writer.write(measurement + System.lineSeparator());
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, e);
        }
    }

    private static void measureSingleOptimizationModeApplicationPerformance(final AStar.OptimizationMode optimizationMode, final String tag) {
        warmupDevice();
        Log.d(LOG_TAG, "Warmup completed.");
        // Instantiate the averaged data lists.
        final List<Double> avgR = new ArrayList<>(NUM_AVERAGED_TRIALS);
        final List<Double> avgN = new ArrayList<>(NUM_AVERAGED_TRIALS);
        final List<Double> avgE = new ArrayList<>(NUM_AVERAGED_TRIALS);
        final List<Double> avgL = new ArrayList<>(NUM_AVERAGED_TRIALS);
        final List<Double> avgT = new ArrayList<>(NUM_AVERAGED_TRIALS);
        final SupplementalMath math = new SupplementalMath();
        for (int i = 0; i < NUM_AVERAGED_TRIALS; i++) {
            final DeserializedVertex source = DataManager.getGraph().vertices(math.random(0, NUM_VERTICES));
            final DeserializedVertex target = DataManager.getGraph().vertices(math.random(0, NUM_VERTICES));
            // Run each trial multiple times to eliminate possible instabilities.
            final List<Integer> R = new ArrayList<>(NUM_TRIALS);
            final List<Integer> N = new ArrayList<>(NUM_TRIALS);
            final List<Integer> E = new ArrayList<>(NUM_TRIALS);
            final List<Integer> L = new ArrayList<>(NUM_TRIALS);
            final List<Integer> T = new ArrayList<>(NUM_TRIALS);
            for (int j = 0; j < NUM_TRIALS; j++) {
                System.gc();
                System.gc();
                System.gc();
                try {
                    final double t0 = System.nanoTime() * TO_MILLI;
                    final Path route = DataManager.getaStar().run(source, target, optimizationMode);
                    R.add((int) Math.round((System.nanoTime() * TO_MILLI - t0)));
                    N.add(route.getN());
                    E.add(route.getEndpoints().size());
                    L.add(route.getLength());
                    T.add(route.getTravelTime());
                } catch (final EqualEndpointException | UnreachableTargetException e) {
                    Log.d(LOG_TAG, String.valueOf(e));
                    Log.d(LOG_TAG, String.format("Reattempting trial No.%d...", --i));
                    i--;
                    break;
                }
            }
            avgR.add(math.mean(R));
            avgN.add(math.mean(N));
            avgE.add(math.mean(E));
            avgL.add(math.mean(L));
            avgT.add(math.mean(T));
            Log.d(LOG_TAG, String.format("Trials Completed: %d", i + 1));
        }
        writeToFile(avgR, tag + "R.txt");
        writeToFile(avgN, tag + "N.txt");
        writeToFile(avgE, tag + "E.txt");
        writeToFile(avgL, tag + "L.txt");
        writeToFile(avgT, tag + "T.txt");
    }

    @Test
    public void measureApplicationPerformance() {
        measureSingleOptimizationModeApplicationPerformance(AStar.OptimizationMode.MINIMIZE_DISTANCE, "A-L-");
        Log.d(LOG_TAG, "Torch profiled using the shortest route optimization mode.");
        measureSingleOptimizationModeApplicationPerformance(AStar.OptimizationMode.MINIMIZE_TRAVEL_TIME, "A-T-");
        Log.d(LOG_TAG, "Torch profiled using the fastest route optimization mode.");
    }

    private static final class SupplementalMath {
        private final Random randomNumberGenerator;

        private SupplementalMath() {
            this.randomNumberGenerator = new Random(362647020392L);
        }

        private int random(final int min, final int max) {
            return (int) ((randomNumberGenerator.nextDouble() * (max - min)) + min);
        }

        private <T> double mean(final List<T> numbers) {
            final OptionalDouble mean = numbers.stream().mapToDouble(a -> Double.parseDouble(String.valueOf(a))).average();
            if (mean.isPresent()) {
                return mean.getAsDouble();
            } else {
                return Double.NaN;
            }
        }
    }
}

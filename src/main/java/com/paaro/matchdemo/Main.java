package com.paaro.matchdemo;

import com.paaro.matchdemo.impl.DemoFixedSizeBag;
import com.paaro.matchdemo.impl.DemoFixedSizeBagMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedure;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedureMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBatchWithUniqueTimestamps;
import com.paaro.matchdemo.impl.DemoMaximumBatchSizeWithUniqueMatchIds;

public class Main {
    public static void main(final String[] args) {
        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(100, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(200, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(500, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(1000, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(500, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(1000, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(500, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(1000, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(500, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(1000, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(100, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(200, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(500, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(1000, true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.run(1000, true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.run(10000, true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.run(100000, true);
        }
    }
}

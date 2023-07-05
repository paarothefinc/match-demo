package com.paaro.matchdemo;

import com.paaro.matchdemo.impl.DemoConcurrentHashMapMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBag;
import com.paaro.matchdemo.impl.DemoFixedSizeBagMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedure;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedureMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBatchWithUniqueTimestamps;
import com.paaro.matchdemo.impl.DemoMaximumBatchSizeWithUniqueMatchIds;

public class Main {
    public static void main(final String[] args) {
        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.withBatchSize(100).run(true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.withBatchSize(200).run(true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.withBatchSize(500).run(true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.withBatchSize(1000).run(true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.withBagSize(100).run(true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.withBagSize(200).run(true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.withBagSize(500).run(true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.withBagSize(1000).run(true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.withBagSize(100).run(true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.withBagSize(200).run(true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.withBagSize(500).run(true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.withBagSize(1000).run(true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.withBagSize(100).withNumberOfThreads(10).run(true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.withBagSize(200).withNumberOfThreads(10).run(true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.withBagSize(500).withNumberOfThreads(10).run(true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.withBagSize(1000).withNumberOfThreads(10).run(true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.withBagSize(100).withNumberOfThreads(10).run(true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.withBagSize(200).withNumberOfThreads(10).run(true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.withBagSize(500).withNumberOfThreads(10).run(true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.withBagSize(1000).withNumberOfThreads(10).run(true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.withBatchSize(100).run(true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.withBatchSize(1000).run(true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.withBatchSize(10000).run(true);
        }

        try (final DemoFixedSizeBatchWithUniqueTimestamps demo = new DemoFixedSizeBatchWithUniqueTimestamps()) {
            demo.withBatchSize(100000).run(true);
        }

        try (final DemoConcurrentHashMapMultiThreaded demo = new DemoConcurrentHashMapMultiThreaded()) {
            demo.run(true);
        }
    }
}

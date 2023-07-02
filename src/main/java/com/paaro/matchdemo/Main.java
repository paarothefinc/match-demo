package com.paaro.matchdemo;

import com.paaro.matchdemo.impl.DemoFixedSizeBag;
import com.paaro.matchdemo.impl.DemoFixedSizeBagMultiThreaded;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedure;
import com.paaro.matchdemo.impl.DemoFixedSizeBagWithStoredProcedureMultiThreaded;
import com.paaro.matchdemo.impl.DemoMaximumBatchSizeWithUniqueMatchIds;

public class Main {
    public static void main(final String[] args) {
        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(5, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(10, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(100, true);
        }

        try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(5, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(10, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBag demo = new DemoFixedSizeBag()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(5, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(10, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBagWithStoredProcedure demo = new DemoFixedSizeBagWithStoredProcedure()) {
            demo.run(200, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(5, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(10, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(100, true);
        }

        try (final DemoFixedSizeBagMultiThreaded demo = new DemoFixedSizeBagMultiThreaded()) {
            demo.run(200, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(5, true);
        }

        try (
            final DemoFixedSizeBagWithStoredProcedureMultiThreaded demo =
                new DemoFixedSizeBagWithStoredProcedureMultiThreaded()) {
            demo.run(10, true);
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
    }
}

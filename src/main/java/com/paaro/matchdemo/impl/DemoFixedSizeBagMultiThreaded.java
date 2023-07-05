package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Bag;
import com.paaro.matchdemo.model.Event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DemoFixedSizeBagMultiThreaded extends DemoBase {
    int bagSize = 100;
    int numberOfThreads = 10;

    public DemoFixedSizeBagMultiThreaded withBagSize(final int bagSize) {
        this.bagSize = bagSize;

        return this;
    }

    public DemoFixedSizeBagMultiThreaded withNumberOfThreads(final int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;

        return this;
    }

    public void run(final boolean truncateTableAfterRun) {
        outputHeader("Bags with unique match IDs with multi-threading",
            String.format("Bag size: %d, Number of threads: %d%n", bagSize, numberOfThreads));

        try {
            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

            // list of bags. Bag can only contain events with unique match IDs
            final List<Bag<Event>> bagList = new ArrayList<>();

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final Event event = new Event(line);

                boolean eventAdded = false;
                final Iterator<Bag<Event>> bagIterator = bagList.iterator();

                // iterate bag list
                while (bagIterator.hasNext()) {
                    final Bag<Event> bag = bagIterator.next();

                    // if the current bag is full (it reached bagSize), flush it to the DB
                    if (bag.isFull()) {
                        executorService.execute(new Consumer(bag));
                        bagIterator.remove();
                        dbCallCounter++;

                        continue;
                    }

                    // if the current bag doesn't contain match ID, proceed with adding the event
                    if (!bag.containsMatchId(event.getMatchId())) {
                        bag.add(event);
                        eventAdded = true;

                        break;
                    }
                }

                // event was not added, because there are no bags, which don't already contain this match ID
                // so create a new bag and put the event in it
                if (!eventAdded) {
                    final Bag<Event> bag = new Bag<>(bagSize);
                    bag.add(event);
                    bagList.add(bag);
                }
            }

            // at the end, flush all remaining bags to the DB
            for (final Bag<Event> bag : bagList) {
                executorService.execute(new Consumer(bag));
                dbCallCounter++;
            }

            // shutdown executor service and wait at most 5 minutes for all threads to finish
            executorService.shutdown();

            try {
                if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (final InterruptedException e) {
                executorService.shutdownNow();
            }

            // output summary
            verifyResult(dbCallCounter, truncateTableAfterRun);
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    class Consumer implements Runnable {
        private Bag<Event> bag;
        private Timestamp dateInsert;

        public Consumer(final Bag<Event> bag) {
            this.bag = bag;
            this.dateInsert = getUniqueTimestamp();
        }

        @Override
        public void run() {
            try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT_WITH_TIMESTAMP)) {
                for (final Event event : bag) {
                    setStatementParametersWithInsertTimestamp(statement, event, dateInsert);
                    statement.addBatch();
                }

                statement.executeBatch();
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}


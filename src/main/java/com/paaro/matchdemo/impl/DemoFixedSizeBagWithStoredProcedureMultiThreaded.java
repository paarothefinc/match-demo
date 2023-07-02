package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Bag;
import com.paaro.matchdemo.model.EventDbType;

import oracle.jdbc.OracleConnection;

import java.io.IOException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DemoFixedSizeBagWithStoredProcedureMultiThreaded extends DemoBase {
    public void run(final int bagSize, final boolean truncateTableAfterRun) {
        outputHeader("Bags with unique match IDs with multi-threading (10 threads) using the stored procedure",
            bagSize);

        try {
            final ExecutorService executorService = Executors.newFixedThreadPool(10);

            // list of bags. Bag can only contain events with unique match IDs
            final List<Bag<EventDbType>> bagList = new ArrayList<>();

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final EventDbType event = new EventDbType(line);

                boolean eventAdded = false;
                final Iterator<Bag<EventDbType>> bagIterator = bagList.iterator();

                // iterate bag list
                while (bagIterator.hasNext()) {
                    final Bag<EventDbType> bag = bagIterator.next();

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
                    final Bag<EventDbType> bag = new Bag<>(bagSize);
                    bag.add(event);
                    bagList.add(bag);
                }
            }

            // at the end, flush all remaining bags to the DB
            for (final Bag<EventDbType> bag : bagList) {
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
        private Bag<EventDbType> bag;
        private Timestamp dateInsert;

        public Consumer(final Bag<EventDbType> bag) {
            this.bag = bag;
            this.dateInsert = getUniqueTimestamp();
        }

        @Override
        public void run() {
            try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT_PROCEDURE)) {
                final Array eventArray = ((OracleConnection) CONNECTION).createOracleArray(
                    EventDbType.SQL_TABLE_TYPE_NAME, bag.getEventList().toArray());
                statement.setArray(1, eventArray);
                statement.setTimestamp(2, dateInsert);
                statement.execute();
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}


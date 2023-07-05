package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoConcurrentHashMapMultiThreaded extends DemoBase {

    // event map, where key=match ID and value=queue of events
    final Map<String, Queue<Event>> eventMap = new ConcurrentHashMap<>();

    // flag to tell the consumer thread when to stop processing
    final AtomicBoolean hasMoreData = new AtomicBoolean(true);

    // db call counter
    final AtomicInteger dbCallCounter = new AtomicInteger(0);

    public void run(final boolean truncateTableAfterRun) {
        outputHeader("Concurrent hash map implementation with one consumer thread",
            "Batch size depends of the current unique MATCH_ID stream");

        try {
            final ExecutorService executorService = Executors.newFixedThreadPool(1);

            executorService.execute(new Consumer());

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final Event event = new Event(line);

                eventMap.computeIfAbsent(event.getMatchId(), x -> new ConcurrentLinkedQueue<>()).add(event);
            }

            // inform consumer threads that all data has been processed
            hasMoreData.set(false);

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
            verifyResult(dbCallCounter.get(), truncateTableAfterRun);
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    class Consumer implements Runnable {
        @Override
        public void run() {
            Iterator<Entry<String, Queue<Event>>> iterator = eventMap.entrySet().iterator();

            try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT)) {

                // iterate if there are more events enqueued to process or if event stream is not finished yet
                while (!eventMap.isEmpty() || hasMoreData.get()) {

                    // if map has no more elements (it is either empty or the iterator has reached the end),
                    // reset the iterator, execute (possible) batch and increment db call counter.
                    if (!iterator.hasNext()) {
                        iterator = eventMap.entrySet().iterator();
                        statement.executeBatch();
                        dbCallCounter.incrementAndGet();

                        continue;
                    }

                    final Entry<String, Queue<Event>> entry = iterator.next();

                    // remove the key with empty event list
                    if (entry.getValue().isEmpty()) {
                        iterator.remove();

                        continue;
                    }

                    // add the event to the batch
                    setStatementParameters(statement, entry.getValue().remove());
                    statement.addBatch();
                }
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}


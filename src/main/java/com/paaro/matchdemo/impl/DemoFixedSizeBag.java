package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Bag;
import com.paaro.matchdemo.model.Event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DemoFixedSizeBag extends DemoBase {
    public void run(final int bagSize, final boolean truncateTableAfterRun) {
        outputHeader("Bags with unique match IDs", bagSize);

        try {
            // list of bags. Bag can only contain events with unique match IDs
            final List<Bag<Event>> bagList = new ArrayList<>();

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final Event event = new Event(line);

                try {
                    boolean eventAdded = false;
                    final Iterator<Bag<Event>> bagIterator = bagList.iterator();

                    // iterate bag list
                    while (bagIterator.hasNext()) {
                        final Bag<Event> bag = bagIterator.next();

                        // if the current bag is full (it reached bagSize), flush it to the DB
                        if (bag.isFull()) {
                            flushBag(bag);
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
                } catch (final SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            // at the end, flush all remaining bags to the DB
            for (final Bag<Event> bag : bagList) {
                flushBag(bag);
                dbCallCounter++;
            }

            // output summary
            verifyResult(dbCallCounter, truncateTableAfterRun);
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void flushBag(final Bag<Event> bag) throws SQLException {
        try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT)) {
            for (final Event event : bag) {
                setStatementParameters(statement, event);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}


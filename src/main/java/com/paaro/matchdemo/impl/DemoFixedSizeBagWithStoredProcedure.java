package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Bag;
import com.paaro.matchdemo.model.EventDbType;

import oracle.jdbc.OracleConnection;

import java.io.IOException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DemoFixedSizeBagWithStoredProcedure extends DemoBase {
    public void run(final int bagSize, final boolean truncateTableAfterRun) {
        outputHeader("Bags with unique match IDs using the stored procedure", bagSize);

        try {
            final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT_PROCEDURE);

            // list of bags. Bag can only contain events with unique match IDs
            final List<Bag<EventDbType>> bagList = new ArrayList<>();
            bagList.add(new Bag<>(bagSize));

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final EventDbType event = new EventDbType(line);

                try {
                    boolean eventAdded = false;
                    final Iterator<Bag<EventDbType>> bagIterator = bagList.iterator();

                    // iterate bag list
                    while (bagIterator.hasNext()) {
                        final Bag<EventDbType> bag = bagIterator.next();

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
                        final Bag<EventDbType> bag = new Bag<>(bagSize);
                        bag.add(event);
                        bagList.add(bag);
                    }
                } catch (final SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            // at the end, flush all remaining bags to the DB
            for (final Bag<EventDbType> bag : bagList) {
                flushBag(bag);
                dbCallCounter++;
            }

            // output summary
            verifyResult(dbCallCounter, truncateTableAfterRun);

            statement.close();
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void flushBag(final Bag<EventDbType> bag) throws SQLException {
        try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT_PROCEDURE)) {
            final Array eventArray = ((OracleConnection) CONNECTION).createOracleArray(EventDbType.SQL_TABLE_TYPE_NAME,
                bag.getEventList().toArray());
            statement.setArray(1, eventArray);
            statement.setNull(2, Types.TIMESTAMP);
            statement.execute();
        }
    }
}


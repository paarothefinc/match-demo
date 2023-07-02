package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DemoMaximumBatchSizeWithUniqueMatchIds extends DemoBase {
    public void run(final int batchSize, final boolean truncateTableAfterRun) {
        outputHeader("Maximum batch size with unique match IDs", batchSize);

        try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT)) {
            // set with only unique match IDs
            final Set<String> uniqueMatchIdSet = new HashSet<>();

            // counter of events in one batch, which must not exceed specified batchSize
            int eventCounter = 0;

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final Event event = new Event(line);

                try {
                    // executes batch either when batchSize is reached or batch already contains current matchId
                    if (!uniqueMatchIdSet.isEmpty() && (++eventCounter % batchSize == 0 || uniqueMatchIdSet.contains(
                        event.getMatchId()))) {

                        statement.executeBatch();
                        dbCallCounter++;

                        // since we flushed the batch, reset eventCounter and uniqueMatchIdSet for the new iteration
                        eventCounter = 0;
                        uniqueMatchIdSet.clear();
                    }

                    // proceed with adding the match ID to the set and setting the SQL insert parameters
                    uniqueMatchIdSet.add(event.getMatchId());
                    setStatementParameters(statement, event);

                    // add the statement to the batch
                    statement.addBatch();
                } catch (final SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            // at the end, flush possible remaining events
            statement.executeBatch();
            dbCallCounter++;

            // output summary
            verifyResult(dbCallCounter, truncateTableAfterRun);
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}


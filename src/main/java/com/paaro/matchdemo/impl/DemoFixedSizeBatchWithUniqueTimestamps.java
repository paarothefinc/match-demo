package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DemoFixedSizeBatchWithUniqueTimestamps extends DemoBase {
    int batchSize = 100;

    public DemoFixedSizeBatchWithUniqueTimestamps withBatchSize(final int batchSize) {
        this.batchSize = batchSize;

        return this;
    }

    public void run(final boolean truncateTableAfterRun) {
        outputHeader("Fixed batch size with unique timestamps", String.format("Batch size: %d%n", batchSize));

        try (final PreparedStatement statement = CONNECTION.prepareStatement(SQL_INSERT_WITH_TIMESTAMP)) {
            int eventCounter = 0;

            // counter of DB calls needed to be made
            int dbCallCounter = 0;

            for (String line = READER.readLine(); line != null; line = READER.readLine()) {
                final Event event = new Event(line);

                try {
                    // executes batch when batchSize is reached
                    if (++eventCounter % batchSize == 0) {
                        statement.executeBatch();
                        dbCallCounter++;

                        // since we flushed the batch, reset eventCounter
                        eventCounter = 0;
                    }

                    // create statement for the event with the unique timestamp
                    setStatementParametersWithInsertTimestamp(statement, event, getUniqueTimestamp());

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


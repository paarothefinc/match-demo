package com.paaro.matchdemo.impl;

import com.paaro.matchdemo.model.Event;

import net.openhft.chronicle.bytes.MappedUniqueTimeProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class DemoBase implements AutoCloseable {
    static final String SOURCE_FILE = "src/main/resources/fo_random.txt";
    static final String SQL_INSERT
        = "INSERT INTO event (match_id, market_id, outcome_id, specifiers) VALUES (?, ?, ?, ?)";
    static final String SQL_INSERT_WITH_TIMESTAMP
        = "INSERT INTO event (match_id, market_id, outcome_id, specifiers, date_insert) VALUES (?, ?, ?, ?, ?)";
    static final String SQL_INSERT_PROCEDURE = "{CALL insert_events(?, ?)}";
    static final String SQL_DURATION
        = "SELECT MIN (date_insert) AS min_date_insert, MAX (date_insert) AS max_date_insert FROM event";
    //@formatter:off
    static final String SQL_CHECK_ORDER =
        "SELECT COUNT (*) " +
        "  FROM (SELECT match_id, " +
        "               market_id, " +
        "               outcome_id, " +
        "               specifiers, " +
        "               ROW_NUMBER () OVER (PARTITION BY match_id ORDER BY match_id, rn) AS rn " +
        "          FROM (SELECT match_id, market_id, outcome_id, specifiers, ROWNUM AS rn FROM event_source) " +
        "        MINUS " +
        "        SELECT match_id, " +
        "               market_id, " +
        "               outcome_id, " +
        "               specifiers, " +
        "               ROW_NUMBER () OVER (PARTITION BY match_id ORDER BY match_id, date_insert) AS rn " +
        "          FROM event)";
    //@formatter:on
    static final String SQL_TRUNCATE_TABLE = "TRUNCATE TABLE event";
    static final MappedUniqueTimeProvider TIME_PROVIDER = MappedUniqueTimeProvider.INSTANCE;

    final BufferedReader READER;
    final Connection CONNECTION;

    final Instant timer = Instant.now();

    public DemoBase() {
        READER = createReader();
        CONNECTION = createConnection();
    }

    private static BufferedReader createReader() {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(SOURCE_FILE));
            reader.readLine(); // skip header line

            return reader;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:oracle:thin:@<server>:1521:<sid>", "match_demo", "match_demo");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Timestamp getUniqueTimestamp() {
        return Timestamp.from(Instant.ofEpochSecond(0L, TIME_PROVIDER.currentTimeMicros() * 1000));
    }

    public abstract void run(int batchSize, final boolean truncateTableAfterRun);

    protected void setStatementParameters(final PreparedStatement statement, final Event event) throws SQLException {
        statement.setString(1, event.getMatchId());
        statement.setInt(2, event.getMarketId());
        statement.setString(3, event.getOutcomeId());

        if (event.getSpecifiers().isBlank()) {
            statement.setNull(4, Types.VARCHAR);
        } else {
            statement.setString(4, event.getSpecifiers());
        }
    }

    protected void setStatementParametersWithInsertTimestamp(final PreparedStatement statement, final Event event,
        final Timestamp dateInsert) throws SQLException {

        setStatementParameters(statement, event);
        statement.setTimestamp(5, dateInsert);
    }

    protected void outputHeader(final String title, final int batchSize) {
        System.out.println("-".repeat(50));
        System.out.println(title);
        System.out.println();
        System.out.printf("Batch/bag size: %d%n", batchSize);
    }

    protected void verifyResult(final int dbCallCounter, final boolean truncateTableAfterRun) throws SQLException {
        System.out.printf("Total run time: %d ms%n", ChronoUnit.MILLIS.between(timer, Instant.now()));

        PreparedStatement statement = CONNECTION.prepareStatement(SQL_DURATION);
        ResultSet rs = statement.executeQuery();
        rs.next();
        final LocalDateTime start = rs.getTimestamp(1).toLocalDateTime();
        final LocalDateTime end = rs.getTimestamp(2).toLocalDateTime();
        System.out.printf("Number of DB calls: %d%n", dbCallCounter);
        System.out.printf("First event inserted: %s%n", start);
        System.out.printf("Last event inserted: %s%n", end);
        System.out.printf("Difference: %d ms%n", ChronoUnit.MILLIS.between(start, end));

        statement = CONNECTION.prepareStatement(SQL_CHECK_ORDER);
        rs = statement.executeQuery();
        rs.next();
        final int result = rs.getInt(1);
        System.out.println(result == 0 ? "The order of produced data is ok." : "THE ORDER OF PRODUCED DATA IS WRONG!");

        if (truncateTableAfterRun) {
            System.out.println("Truncating table...");
            statement = CONNECTION.prepareStatement(SQL_TRUNCATE_TABLE);
            statement.execute();
        }

        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            READER.close();
            CONNECTION.close();
        } catch (final SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}


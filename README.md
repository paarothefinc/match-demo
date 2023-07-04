# Match Events Reader Demo

This project represents a demo of how streaming match events can be put into the database in an orderly manner as fast as possible.

## Stream Specification

Events in the stream have four properties/columns:
- `MATCH_ID` (required): a match unique identifier
- `MARKET_ID` (required): a market (country) where the match is taking place
- `OUTCOME_ID` (required): outcome identifier
- `SPECIFIERS` (optional): optional parameters

Events must be written to the output (DB) in such way that they keep a proper order within the respective MATCH_ID. This can be achieved by adding a DATE_INSERT timestamp column to the output table, which denotes the insertion timestamp and must be unique within the respective (unique) MATCH_ID.

## Installation

The project requires Oracle database. It was tested on version 11g. First, create a user.

```sql
CREATE USER <username> IDENTIFIED BY <password>;
```

By default, use `match-demo` for both username and password. The following script should be ok. Make sure to replace `<tablespace>` with the correct tablespace in your database.

```sql
CREATE USER match_demo IDENTIFIED BY match_demo
    DEFAULT TABLESPACE <tablespace>
    TEMPORARY TABLESPACE temp
    PROFILE DEFAULT
    ACCOUNT UNLOCK
/

GRANT CONNECT TO match_demo
/
GRANT RESOURCE TO match_demo
/

ALTER USER match_demo
    DEFAULT ROLE ALL
/

GRANT CREATE TABLE TO match_demo
/
GRANT UNLIMITED TABLESPACE TO match_demo
/

ALTER USER match_demo
    QUOTA UNLIMITED ON <tablespace>
/
```

Next, create the Oracle directory. Replace `<directory>` with a name of your choice. You don't need to create the directory, instead you can use one if already created. Then place the `src/main/resources/fo_random.txt` file in this directory. This is required because the `ORGANIZATION EXTERNAL` table will be created to compare the data with the inserted stream.

```sql
CREATE OR REPLACE DIRECTORY <directory> AS '/path/of/your/choice'
/
```

There is a file called `ddl.sql` in `src/main/resources` folder which creates the DB objects. You must edit it first and replace `<directory>` with the directory name you just created.

```sql
CREATE TABLE event
(
    match_id       VARCHAR2 (100) NOT NULL,
    market_id      INTEGER NOT NULL,
    outcome_id     VARCHAR2 (100) NOT NULL,
    specifiers     VARCHAR2 (1000),
    date_insert    TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE event_source
(
    match_id      VARCHAR2 (100),
    market_id     INTEGER,
    outcome_id    VARCHAR2 (100),
    specifiers    VARCHAR2 (1000)
)
ORGANIZATION EXTERNAL
    (DEFAULT DIRECTORY <directory>
     ACCESS PARAMETERS (RECORDS DELIMITED BY NEWLINE
                        SKIP 1
                        FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY "'" MISSING FIELD VALUES ARE NULL
                        (match_id,
                         market_id,
                         outcome_id,
                         specifiers))
     LOCATION ('fo_random.txt'))
    REJECT LIMIT UNLIMITED;

CREATE OR REPLACE TYPE event_type FORCE AS OBJECT
(
    match_id VARCHAR2 (100),
    market_id INTEGER,
    outcome_id VARCHAR2 (100),
    specifiers VARCHAR2 (1000)
)
/

CREATE OR REPLACE TYPE event_type_table FORCE AS TABLE OF event_type
/

CREATE OR REPLACE PROCEDURE insert_events (p_events IN event_type_table, p_date_insert TIMESTAMP)
IS
BEGIN
    FORALL i IN p_events.FIRST .. p_events.LAST
        INSERT INTO event (match_id,
                           market_id,
                           outcome_id,
                           specifiers,
                           date_insert)
             VALUES (p_events (i).match_id,
                     p_events (i).market_id,
                     p_events (i).outcome_id,
                     p_events (i).specifiers,
                     DECODE (p_date_insert, NULL, CURRENT_TIMESTAMP, p_date_insert));
END insert_events;
/
```

Voila! Database part is ready. Just make sure to edit the following method in `DemoBase.java` file and replace `<server>` and `<sid>` with proper values from your database.

```java
private static Connection createConnection() {
    try {
        return DriverManager.getConnection("jdbc:oracle:thin:@<server>:1521:<sid>", "match_demo", "match_demo");
    } catch (final SQLException e) {
        throw new RuntimeException(e);
    }
}
```

## Usage

The easiest way is to import the project into your favorite IDE and run it from there. Just run the `Main` class which will perform all the tests.

## Approach

I used a few approaches to the problem. Single thread, multi thread, batch/bag and stored procedure. For multi thread implementations, I used `ExecutorService` with 10 threads, but this can easily be changed.

```java
final ExecutorService executorService = Executors.newFixedThreadPool(10);
```

Tests for all implementations run in four variants: with 100, 200, 500 and 1000 bag/batch sizes, with the exception of DemoFixedSizeBatchWithUniqueTimestamps, which runs with 100, 1000, 10000 and 100000 batch sizes.

### Batch

JDBC driver supports batch inserts, which considerably speeds up writing to the database. `PreparedStatement` supports this implementation by adding statements to the batch via `.addBatch()` method and executing the batch with the call to `.executeBatch()`.

### Bag

These are not mutually exclusive. Bag implementation uses batches to write to the database, too. With bags, it is easier to separate events in a manner that a single bag only contains events with unique `MATCH_IDs`. Events, which are read from the source are put into a bag until it is full or if the event contains `MATCH_ID` which is already present in this bag. If a bag is full, it is flushed to the database and discarded. If it is not full, the event is put in the next bag (if it exists), which doesn't contain event's `MATCH_ID`. If such bag doesn't exist, a new bag is created.

## Implementations

### DemoMaximumBatchSizeWithUniqueMatchIds

This implementation is quite basic. It fills up the batch until the batch is full or until the batch already contains an event with the same `MATCH_ID` as the current event. In both cases, the batch is flushed to the database and a new batch is created. Since events with the same `MATCH_ID` are rarely stacked together in the source stream, batches are most often filled with multiple events, ensuring that the write to the database is quite fast.

### DemoFixedSizeBag

The advantage of the bag implementation over the batch only implementation is that bags are always full when flushed to the database, ensuring that batches are always executed to the maximum batch size. Because this example includes a finite stream, bags near the end of the stream tend to be smaller and smaller, because less and less events with different `MATCH_IDs` are left, forcing bags to become smaller. But in an indefinite stream, this would never have happened.

### DemoFixedSizeBagWithStoredProcedure

This implementation is basically the same as the previous one, with the difference of how data is written to the database. Instead of batches, data is collected into an Oracle array and then sent to the stored procedure for insertion.

### DemoFixedSizeBagMultiThreaded

This implementation is also almost the same as the `DemoFixedSizeBag`, but now we are spawning threads to (possibly) achieve greater speed of writing to the database. In all previous (single thread) implementations, `DATE_INSERT` timestamps are created by the database, because writes are sequential and this ensures the timestamp correctness. But with multi thread environment, we cannot assure the proper order of writes. Because of that, timestamps must be generated in Java at thread's creation time and not at flush time, as threads, which were created later, might be flushed sooner than older threads. There is a problem though, because threads may be spawned so fast that Java produces the exact same timestamps. To amend this problem, I used `https://github.com/OpenHFT/Chronicle-Bytes` library with the `MappedUniqueTimeProvider` provider, which states that `Timestamps are unique across threads/processes on a single machine.` Exactly what we needed.

### DemoFixedSizeBagWithStoredProcedureMultiThreaded

Again, this implementation is the same as the previous one bar writes to the database, which are done using the stored procedure. And again, timestamps must be generated in Java to assure proper order of events.

### DemoFixedSizeBatchWithUniqueTimestamps

Latest implementation, which takes advantage of unique generated timestamps in Java code. It is a single thread implementation with fixed batch size, but since all timestamps are unique, batches can be safely committed to the database.

## Output

This is a sample output of one of the tests:

```bash
Bags with unique match IDs

Batch/bag size: 200
Total run time: 2149 ms
Number of DB calls: 2115
First event inserted: 2023-07-03T06:53:03.207018
Last event inserted: 2023-07-03T06:53:05.328520
Difference: 2121 ms
The order of produced data is ok.
Truncating table...
```

Explanation:
- *Bags with unique match IDs*: Name of the implementation currently run.
- *Batch/bag size: 200*: The size of the batch/bag. By default, test are run with 5/10/100/200 sizes.
- *Total run time: 2149 ms*: Total time from the beginning of reading the stream to the last data being committed to the database.
- *Number of DB calls: 2115*: Number of calls (batches/stored procedure) to the database.
- *First event inserted: 2023-07-03T06:53:03.207018*: Timestamp of the first inserted event
- *Last event inserted: 2023-07-03T06:53:05.328520*: Timestamp of the last inserted event
- *Difference: 2121 ms*: The difference between both times. **Important note: with multi thread implementations, timestamps are created at the thread creation time, resulting in a slightly different timestamps as with the single thread implementations.**
- *The order of produced data is ok.*: At the end of each test, a SQL statement is run to check whether the data is inserted in the correct order.
- *Truncating table...*: After each test, the data from the `EVENT` table is truncated to give way for new tests. If you would like to run a single test and retain the data after the test is done, change the second parameter from `true` to `false`:

```java
try (final DemoMaximumBatchSizeWithUniqueMatchIds demo = new DemoMaximumBatchSizeWithUniqueMatchIds()) {
    demo.run(100, true);
}
```

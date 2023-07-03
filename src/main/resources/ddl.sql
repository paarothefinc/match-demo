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

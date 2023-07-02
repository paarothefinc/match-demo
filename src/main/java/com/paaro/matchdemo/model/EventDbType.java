package com.paaro.matchdemo.model;

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.Objects;

public class EventDbType extends Event implements SQLData {

    public static final String SQL_OBJECT_TYPE_NAME = "EVENT_TYPE";
    public static final String SQL_TABLE_TYPE_NAME = "EVENT_TYPE_TABLE";

    public EventDbType(final String line) {
        super(line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, marketId, outcomeId, specifiers);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EventDbType eventDbType = (EventDbType) o;
        return Objects.equals(matchId, eventDbType.matchId) && marketId == eventDbType.marketId && Objects.equals(
            outcomeId, eventDbType.outcomeId) && Objects.equals(specifiers, eventDbType.specifiers);
    }

    @Override
    public String getSQLTypeName() {
        return SQL_OBJECT_TYPE_NAME;
    }

    @Override
    public void readSQL(final SQLInput stream, final String typeName) throws SQLException {
        matchId = stream.readString();
        marketId = stream.readInt();
        outcomeId = stream.readString();
        specifiers = stream.readString();
    }

    @Override
    public void writeSQL(final SQLOutput stream) throws SQLException {
        stream.writeString(matchId);
        stream.writeInt(marketId);
        stream.writeString(outcomeId);
        stream.writeString(specifiers);
    }
}

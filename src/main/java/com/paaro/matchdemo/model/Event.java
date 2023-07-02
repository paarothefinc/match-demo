package com.paaro.matchdemo.model;

import java.util.StringJoiner;

public class Event {
    protected String matchId;
    protected int marketId;
    protected String outcomeId;
    protected String specifiers;

    public Event(final String line) {
        final String[] split = line.split("\\|", 4);

        final String matchId = removeQuotes(split[0]);
        final int marketId = Integer.parseInt(split[1]);
        final String outcomeId = removeQuotes(split[2]);
        final String specifiers = split[3].isEmpty() ? "" : removeQuotes(split[3]);

        this.matchId = matchId;
        this.marketId = marketId;
        this.outcomeId = outcomeId;
        this.specifiers = specifiers;
    }

    private static String removeQuotes(final String quotedText) {
        return quotedText.substring(1, quotedText.length() - 1);
    }

    public String getMatchId() {
        return matchId;
    }

    public int getMarketId() {
        return marketId;
    }

    public String getOutcomeId() {
        return outcomeId;
    }

    public String getSpecifiers() {
        return specifiers;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]").add("marketId=" + marketId).add(
                "matchId='" + matchId + "'").add("outcomeId='" + outcomeId + "'").add("specifiers='" + specifiers + "'")
            .toString();
    }
}

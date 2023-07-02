package com.paaro.matchdemo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Bag<T extends Event> implements Iterable<T> {
    Set<String> matchIdSet;
    List<T> eventList;
    int size;

    public Bag(final int size) {
        this.size = size;
        matchIdSet = new HashSet<>(size);
        eventList = new ArrayList<>(size);
    }

    public List<T> getEventList() {
        return eventList;
    }

    @Override
    public Iterator<T> iterator() {
        return eventList.iterator();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        eventList.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return eventList.spliterator();
    }

    public boolean isFull() {
        return matchIdSet.size() == size;
    }

    public boolean containsMatchId(final String matchId) {
        return matchIdSet.contains(matchId);
    }

    public void add(final T event) {
        matchIdSet.add(event.getMatchId());
        eventList.add(event);
    }
}

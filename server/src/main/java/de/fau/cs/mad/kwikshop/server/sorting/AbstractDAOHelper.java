package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractDAOHelper implements DAOHelper {

    private final ReentrantLock[] locks;
        // two of these locks have to be acquired to make sure no two threads modify the same edge at one

    public AbstractDAOHelper() {
        locks = new ReentrantLock[1000];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public int getNumberOfLocks() {
        return locks.length;
    }

    @Override
    public ReentrantLock getLockWithNumber(int number) {
        return locks[number];
    }

}

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

    protected void lockLockWithId(int id) {
        int n = getNumberOfLocks();
        int index = id % n;
        getLockWithNumber(index).lock();
    }

    protected void unlockLockWithId(int id) {
        int n = getNumberOfLocks();
        int index = id % n;
        getLockWithNumber(index).unlock();
    }

    protected void lockLocksWithIds(int id1, int id2) {
        int n = getNumberOfLocks();
        int index1 = id1 % n;
        int index2 = id2 % n;
        // global ordering of locks
        if (id1 <= id2) {
            getLockWithNumber(index1).lock();
            getLockWithNumber(index2).lock();
        } else {
            getLockWithNumber(index2).lock();
            getLockWithNumber(index1).lock();
        }
    }

    protected void unlockLocksWithIds(int id1, int id2) {
        int n = getNumberOfLocks();
        int index1 = id1 % n;
        int index2 = id2 % n;
        // unlocking order does not matter
        getLockWithNumber(index1).unlock();
        getLockWithNumber(index2).unlock();
    }
}

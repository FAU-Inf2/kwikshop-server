package de.fau.cs.mad.kwikshop.server.sorting;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

public abstract class AbstractDAOHelper implements DAOHelper {

    private final ReentrantLock[] locks;
        // two of these locks have to be acquired to make sure no two threads modify the same edge at one

    private final HashMap<String, SoftReference<ItemGraph>> itemGraphCache = new HashMap<>();
        // cache for ItemGraphs, so they are not created multiple times per supermarket, if they are still reference-able

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

    @Override
    public ItemGraph getItemGraphForSupermarket(Supermarket supermarket) {
        if(supermarket == null) {
            throw new ArgumentNullException("supermarket");
        }
        ItemGraph itemGraph = null;
        synchronized (supermarket) {
            // make sure only one item graph per supermarket is created
            SoftReference<ItemGraph> reference = itemGraphCache.get(supermarket.getPlaceId());
            if (reference != null) {
                itemGraph = reference.get();
            }
            if (itemGraph == null) {
                itemGraph = ItemGraph.callItemGraphConstructor(this, supermarket);
                itemGraphCache.put(supermarket.getPlaceId(), new SoftReference<>(itemGraph));
            }
        }
        return itemGraph;
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

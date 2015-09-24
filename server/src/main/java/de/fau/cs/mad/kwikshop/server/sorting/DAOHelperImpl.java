package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketChainDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketDAO;

public class DAOHelperImpl extends AbstractDAOHelper {

    private BoughtItemDAO boughtItemDAO;
    private EdgeDAO edgeDAO;
    private SupermarketDAO supermarketDAO;
    private SupermarketChainDAO supermarketChainDAO;

    public DAOHelperImpl(BoughtItemDAO boughtItemDAO, EdgeDAO edgeDAO,
                         SupermarketDAO supermarketDAO, SupermarketChainDAO supermarketChainDAO) {
        super();
        this.boughtItemDAO = boughtItemDAO;
        this.edgeDAO = edgeDAO;
        this.supermarketDAO = supermarketDAO;
        this.supermarketChainDAO = supermarketChainDAO;
        this.supermarketChainDAO.setUp();
    }

    @Override
    public Supermarket getSupermarketByPlaceID(String placeId) {
        synchronized (supermarketDAO) {
            return supermarketDAO.getByPlaceId(placeId);
        }
    }

    @Override
    public List<SupermarketChain> getAllSupermarketChains() {
        synchronized (supermarketChainDAO) {
            return supermarketChainDAO.getAll();
        }
    }

    @Override
    public void createSupermarket(Supermarket supermarket) {
        synchronized (supermarketDAO) {
            supermarketDAO.createSupermarket(supermarket);
        }
    }

    @Override
    public List<Edge> getEdgesBySupermarket(Supermarket supermarket) {
        synchronized (edgeDAO) {
            return edgeDAO.getBySupermarket(supermarket);
        }
    }

    @Override
    public Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
        try {
            lockLocksWithIds(from.getId(), to.getId());
            return edgeDAO.getByFromTo(from, to, supermarket);
        } finally {
            unlockLocksWithIds(from.getId(), to.getId());
        }
    }

    @Override
    public List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket) {
        try {
            lockLockWithId(boughtItem.getId());
            return edgeDAO.getByTo(boughtItem, supermarket);
        } finally {
            unlockLockWithId(boughtItem.getId());
        }
    }

    @Override
    public Edge createEdge(Edge edge) {
        int id1 = edge.getFrom().getId();
        int id2 = edge.getTo().getId();
        try {
            lockLocksWithIds(id1, id2);
            return edgeDAO.createEdge(edge);
        } finally {
            unlockLocksWithIds(id1, id2);
        }
    }

    @Override
    public void deleteEdge(Edge edge) {
        int id1 = edge.getFrom().getId();
        int id2 = edge.getTo().getId();
        try {
            lockLocksWithIds(id1, id2);
            edgeDAO.deleteEdge(edge);
        } finally {
            unlockLocksWithIds(id1, id2);
        }
    }

    @Override
    public BoughtItem getStartBoughtItem() {
        synchronized (boughtItemDAO) {
            return boughtItemDAO.getStart();
        }
    }

    @Override
    public BoughtItem getEndBoughtItem() {
        synchronized (boughtItemDAO) {
            return boughtItemDAO.getEnd();
        }
    }

    @Override
    public BoughtItem getBoughtItemByName(String name) {
        synchronized (boughtItemDAO) {
            return boughtItemDAO.getByName(name);
        }
    }

    @Override
    public BoughtItem getBoughtItemByNameIncludingStartAndEnd(String name) {
        synchronized (boughtItemDAO) {
            return boughtItemDAO.getByNameIncludingStartAndEnd(name);
        }
    }

    @Override
    public void createBoughtItem(BoughtItem boughtItem) {
        synchronized (boughtItemDAO) {
            boughtItemDAO.createBoughtItem(boughtItem);
        }
    }

    @Override
    public Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain) {
        synchronized (supermarketDAO) {
            return supermarketDAO.getGlobalBySupermarketChain(supermarketChain);
        }
    }

    @Override
    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        synchronized (supermarketChainDAO) {
            return supermarketChainDAO.getGlobalSupermarket(supermarketChain);
        }
    }
}

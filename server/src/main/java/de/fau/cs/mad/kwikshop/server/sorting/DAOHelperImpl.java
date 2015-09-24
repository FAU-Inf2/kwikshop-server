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
        return supermarketDAO.getByPlaceId(placeId);
    }

    @Override
    public List<SupermarketChain> getAllSupermarketChains() {
        return supermarketChainDAO.getAll();
    }

    @Override
    public void createSupermarket(Supermarket supermarket) {
        supermarketDAO.createSupermarket(supermarket);
    }

    @Override
    public List<Edge> getEdgesBySupermarket(Supermarket supermarket) {
        return edgeDAO.getBySupermarket(supermarket);
    }

    @Override
    public Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
        return edgeDAO.getByFromTo(from, to, supermarket);
    }

    @Override
    public List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket) {
        return edgeDAO.getByTo(boughtItem, supermarket);
    }

    @Override
    public Edge createEdge(Edge edge) {
        return edgeDAO.createEdge(edge);
    }

    @Override
    public void deleteEdge(Edge edge) {
        edgeDAO.deleteEdge(edge);
    }

    @Override
    public BoughtItem getStartBoughtItem() {
        return boughtItemDAO.getStart();
    }

    @Override
    public BoughtItem getEndBoughtItem() {
        return boughtItemDAO.getEnd();
    }

    @Override
    public BoughtItem getBoughtItemByName(String name) {
        return boughtItemDAO.getByName(name);
    }

    @Override
    public BoughtItem getBoughtItemByNameIncludingStartAndEnd(String name) {
        return boughtItemDAO.getByNameIncludingStartAndEnd(name);
    }

    @Override
    public void createBoughtItem(BoughtItem boughtItem) {
        boughtItemDAO.createBoughtItem(boughtItem);
    }

    @Override
    public Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain) {
        return supermarketDAO.getGlobalBySupermarketChain(supermarketChain);
    }

    @Override
    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        return supermarketChainDAO.getGlobalSupermarket(supermarketChain);
    }
}

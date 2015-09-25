package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public interface DAOHelper {
    String START_ITEM = "START_ITEM";
    String END_ITEM = "END_ITEM";
    Supermarket getSupermarketByPlaceID(String placeId);
    List<SupermarketChain> getAllSupermarketChains();
    void createSupermarket(Supermarket supermarket);
    List<Edge> getEdgesBySupermarket(Supermarket supermarket);
    Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket);
    List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket);
    Edge createEdge(Edge edge);
    void deleteEdge(Edge edge);
    BoughtItem getStartBoughtItem();
    BoughtItem getEndBoughtItem();
    BoughtItem getBoughtItemByName(String name);
    BoughtItem getBoughtItemByNameIncludingStartAndEnd(String name);
    void createBoughtItem(BoughtItem boughtItem);
    Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain);
    Supermarket getGlobalSupermarket(SupermarketChain supermarketChain);
    int getNumberOfLocks();
    ReentrantLock getLockWithNumber(int number);
    ItemGraph getItemGraphForSupermarket(Supermarket supermarket);
}

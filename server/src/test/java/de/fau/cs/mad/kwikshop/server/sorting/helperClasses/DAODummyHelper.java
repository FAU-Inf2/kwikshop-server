package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.AbstractDAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;

/*This class should be used only for testing!!!*/
public class DAODummyHelper extends AbstractDAOHelper {

    private final String ONE = "ONE";
    private final String TWO = "TWO";
    private final String THREE = "THREE";
    private final String FOUR = "FOUR";
    private final String CHAIN_ONE = "CHAIN_ONE";
    private final String CHAIN_TWO = "CHAIN_TWO";

    private final Supermarket defaultSupermarketOne;
    private final Supermarket defaultSupermarketTwo;
    private final Supermarket defaultSupermarketThree;
    private final Supermarket defaultSupermarketFour;
    private final SupermarketChain defaultSupermarketChainOne;
    private final Supermarket defaultSupermarketChainOneGlobalSupermarket;
    private final SupermarketChain defaultSupermarketChainTwo;
    private final Supermarket defaultSupermarketChainTwoGlobalSupermarket;

    private final HashMap<String, List<Edge>> edges;
    private final HashMap<String, Supermarket> supermarkets;

    private final BoughtItem startBoughtItem;
    private final BoughtItem endBoughtItem;
    private final HashMap<String, BoughtItem> boughtItems;

    public DAODummyHelper() {
        super();
        defaultSupermarketChainOne = new SupermarketChain();
        defaultSupermarketChainOne.setId(1);
        defaultSupermarketChainOne.setName(CHAIN_ONE);
        defaultSupermarketChainOneGlobalSupermarket = new Supermarket();
        defaultSupermarketChainOneGlobalSupermarket.setId(-1);
        defaultSupermarketChainOneGlobalSupermarket.setPlaceId(CHAIN_ONE);
        defaultSupermarketChainOneGlobalSupermarket.setSupermarketChain(defaultSupermarketChainOne);

        defaultSupermarketChainTwo = new SupermarketChain();
        defaultSupermarketChainTwo.setId(2);
        defaultSupermarketChainTwo.setName(CHAIN_TWO);
        defaultSupermarketChainTwoGlobalSupermarket = new Supermarket();
        defaultSupermarketChainTwoGlobalSupermarket.setId(-2);
        defaultSupermarketChainTwoGlobalSupermarket.setPlaceId(CHAIN_TWO);
        defaultSupermarketChainTwoGlobalSupermarket.setSupermarketChain(defaultSupermarketChainTwo);

        defaultSupermarketOne = new Supermarket();
        defaultSupermarketOne.setId(1);
        defaultSupermarketOne.setPlaceId(ONE);
        defaultSupermarketOne.setSupermarketChain(defaultSupermarketChainOne);

        defaultSupermarketTwo = new Supermarket();
        defaultSupermarketTwo.setId(2);
        defaultSupermarketTwo.setPlaceId(TWO);
        defaultSupermarketTwo.setSupermarketChain(defaultSupermarketChainTwo);

        defaultSupermarketThree = new Supermarket();
        defaultSupermarketThree.setId(3);
        defaultSupermarketThree.setPlaceId(THREE);
        defaultSupermarketThree.setSupermarketChain(defaultSupermarketChainOne);

        defaultSupermarketFour = new Supermarket();
        defaultSupermarketFour.setId(4);
        defaultSupermarketFour.setPlaceId(FOUR);

        supermarkets = new HashMap<>();
        supermarkets.put(ONE, defaultSupermarketOne);
        supermarkets.put(TWO, defaultSupermarketTwo);
        supermarkets.put(THREE, defaultSupermarketThree);
        supermarkets.put(FOUR, defaultSupermarketFour);

        edges = new HashMap<>();

        startBoughtItem = new BoughtItem(START_ITEM);
        startBoughtItem.setServerInternalItem(true);
        endBoughtItem = new BoughtItem(END_ITEM);
        endBoughtItem.setServerInternalItem(true);
        boughtItems = new HashMap<>();
        // don't put startBoughtItem and andBoughtItem into boughtItems, as they are server-internal items
        //boughtItems.put(START_ITEM, startBoughtItem);
        //boughtItems.put(END_ITEM, endBoughtItem);
    }

    @Override
    public Supermarket getSupermarketByPlaceID(String placeId) {
        synchronized (supermarkets) {
            return supermarkets.get(placeId);
        }
    }

    @Override
    public List<SupermarketChain> getAllSupermarketChains() {
        List<SupermarketChain> supermarketChains = new ArrayList<>(2);
        supermarketChains.add(0, defaultSupermarketChainOne);
        supermarketChains.add(1, defaultSupermarketChainTwo);
        return supermarketChains;
    }

    @Override
    public void createSupermarket(Supermarket supermarket) {
        synchronized (supermarkets) {
            if (supermarkets.containsKey(supermarket.getPlaceId())) {
                throw new IllegalArgumentException("Supermarket already created");
            }
            supermarkets.put(supermarket.getPlaceId(), supermarket);
        }
    }

    @Override
    public List<Edge> getEdgesBySupermarket(Supermarket supermarket) {
        if(supermarket == null) {
            return new ArrayList<>();
        }
        synchronized (edges) {
            List<Edge> edges = this.edges.get(supermarket.getPlaceId());
            if (edges == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(edges);
        }
    }

    @Override
    public Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
        try {
            lockLocksWithIds(to.getId(), from.getId());

            List<Edge> edges = getEdgesBySupermarket(supermarket);
            for (Edge edge : edges) {
                if (edge.getFrom().equals(from) && edge.getTo().equals(to)) {
                    return edge;
                }
            }
            return null;
        } finally {
            // gets called before the return in the try-block is really executed
            unlockLocksWithIds(to.getId(), from.getId());
        }
    }

    @Override
    public List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket) {
        try {
            lockLockWithId(boughtItem.getId());
            List<Edge> allEdges = getEdgesBySupermarket(supermarket);
            List<Edge> foundEdges = new ArrayList<>();
            for (Edge edge : allEdges) {
                if (edge.getTo().equals(boughtItem)) {
                    foundEdges.add(edge);
                }
            }
            return foundEdges;
        } finally {
            unlockLockWithId(boughtItem.getId());
        }

    }

    @Override
    public Edge createEdge(Edge edge) {
        int id1 = edge.getFrom().getId();
        int id2 = edge.getTo().getId();
        String supermarketPlaceId = edge.getSupermarket().getPlaceId();
        try {
            lockLocksWithIds(id1, id2);
            List<Edge> edges = this.edges.get(supermarketPlaceId);
            if (edges == null) {
                // the specified supermarket doesn't have edges yet
                edges = new ArrayList<>();
                this.edges.put(supermarketPlaceId, edges);
            }
            edges.add(edge);
            return edge;
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
            List<Edge> edges = this.edges.get(edge.getSupermarket().getPlaceId());
            if (edges != null) {
                edges.remove(edge);
            }
        } finally {
            unlockLocksWithIds(id1, id2);
        }

    }

    @Override
    public BoughtItem getStartBoughtItem() {
        return startBoughtItem;
    }

    @Override
    public BoughtItem getEndBoughtItem() {
        return endBoughtItem;
    }

    @Override
    public BoughtItem getBoughtItemByName(String name) {
        synchronized (boughtItems) { // visibility synchronization
            return boughtItems.get(name);
        }
    }

    @Override
    public BoughtItem getBoughtItemByNameIncludingStartAndEnd(String name) {
        synchronized (boughtItems) { // visibility synchronization
            return boughtItems.get(name);
        }
    }

    @Override
    public void createBoughtItem(BoughtItem boughtItem) {
        try {
            lockLockWithId(boughtItem.getId());
            if (!boughtItems.containsValue(boughtItem)) {
                boughtItems.put(boughtItem.getName(), boughtItem);
            }
        } finally {
            unlockLockWithId(boughtItem.getId());
        }
    }

    @Override
    public Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain) {
        if (supermarketChain.getName().equals(CHAIN_ONE)) {
            return defaultSupermarketChainOneGlobalSupermarket;
        } else if (supermarketChain.getName().equals(CHAIN_TWO)) {
            return defaultSupermarketChainTwoGlobalSupermarket;
        } else {
            return null;
        }
    }

    @Override
    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        return getGlobalSupermarketBySupermarketChain(supermarketChain);
    }
}

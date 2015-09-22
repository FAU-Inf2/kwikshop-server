package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;

public class ItemGraph {

    private Set<BoughtItem> vertices;
    private Set<Edge> edges;

    private Supermarket supermarket;

    private DAOHelper daoHelper;

    public ItemGraph(DAOHelper daoHelper) {
        this.daoHelper = daoHelper;
    }

    public DAOHelper getDaoHelper() {
        return daoHelper;
    }

    public Set<BoughtItem> getVertices() {
        if(vertices == null)
            vertices = new HashSet<>();
        return vertices;
    }

    public Set<Edge> getEdges() {
        if(edges == null) {
            List<Edge> edgeList = daoHelper.getEdgesBySupermarket(supermarket);
            if (edgeList != null)
                edges = new HashSet<>(edgeList);
            else
                edges = new HashSet<>();
        }
        return edges;
    }

    public boolean setSupermarket(String placeId, String supermarketName) {

        boolean isNewSupermarket = false;

        this.supermarket = daoHelper.getSupermarketByPlaceID(placeId);

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if(supermarket == null) {
            isNewSupermarket = true;
            supermarket = new Supermarket(placeId);

            for(SupermarketChain supermarketChain : daoHelper.getAllSupermarketChains()) {
                /* If the supermarket's name contains the name of a chain, it (most likely) belongs to that chain */
                if(supermarketName.toLowerCase().contains(supermarketChain.getName().toLowerCase())) {
                    supermarket.setSupermarketChain(supermarketChain);
                    break;
                }
            }
            daoHelper.createSupermarket(supermarket);
        }

        return isNewSupermarket;

    }

    private void setSupermarket(Supermarket supermarket) {
        this.supermarket = supermarket;
    }

    public Supermarket getSupermarket() {
        return supermarket;
    }

    public void update() {
        wrappedUpdate(false);
    }

    public void updateGlobalItemGraph() {
        wrappedUpdate(true);
    }

    private void wrappedUpdate(boolean isGlobal) {

        /* Update() loads all Edges and Vertices of one specific supermarket -> supermarket may not be null */
        if(supermarket == null) {
            throw new ArgumentNullException("supermarket");
        }

        /* Load Edges */
        List<Edge> edgeList = daoHelper.getEdgesBySupermarket(supermarket);
        if(edgeList != null)
            edges = new HashSet<>(edgeList);
        else
            edges = new HashSet<>();

        /* Load Vertices */
        vertices = new HashSet<>();
        for(Edge edge : edgeList) {
            if(!vertices.contains(edge.getFrom()))
                vertices.add(edge.getFrom());

            if(!vertices.contains(edge.getTo()))
                vertices.add(edge.getTo());
        }

        if(!isGlobal) {
            /* If there are no Vertices for this Supermarket but it does belong to a SupermarketChain, copy the data from this SupermarketChain */
            if (vertices.size() == 0 && supermarket.getSupermarketChain() != null) {
                copyDataFromItemGraph(daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain()));
            }
        }

        /* Debug output */
        System.out.println(String.format("ItemGraph refreshed, containing %s Edges and %s vertices.", edgeList.size(), vertices.size()));
        System.out.println("Vertices:");
        for(BoughtItem boughtItem : vertices) {
            System.out.println(boughtItem.getName());
        }
        System.out.println("Edges:");
        System.out.println("");
        System.out.println("digraph G {");
        for(Edge edge: edgeList) {
            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);
            System.out.println(String.format("%s -> %s [label=\"%s\"]", edge.getFrom().getName(), edge.getTo().getName(), String.valueOf(Math.round(currentWeightDistanceRatio*1000.0)/1000.0)));
        }
        System.out.println("}");
        System.out.println("");
    }

    /* Create or update an Edge for the given combination of BoughtItems and Supermarket */
    public Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket) {

        /* Supermarket must be included because different supermarkets have different edges */
        Edge edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);

        if(edge == null) {

            /* Check if there is an Edge in the opposite direction */
            edge = daoHelper.getEdgeByFromTo(i2, i1, supermarket);

            if(edge != null) {
                /* Edit existing edge - decrease weight */
                //edge.setWeight(edge.getWeight()-1);

                //decrease weight of all edges to direct parent nodes (minimum distance) of the first item (which comes after the second one in the graph)
                for(BoughtItem parent : getParents(i1)) {
                    //only decrement if the parent node is connected with the other item
                    if (edgeFromToExists(i2, parent) || parent.equals(i2)) {
                        Edge edgeToParentNode = daoHelper.getEdgeByFromTo(parent, i1, supermarket);
                        edgeToParentNode.setWeight(edgeToParentNode.getWeight() - 1);

                    /* Create edge in the opposite direction */
                        if (edgeToParentNode.getWeight() <= 0) {
                            for (Edge toBeRemoved : daoHelper.getEdgesByTo(i1, supermarket)) {
                                if(toBeRemoved.getFrom().equals(i2) || toBeRemoved.getFrom()    .equals(parent)) {
                                    daoHelper.deleteEdge(toBeRemoved);
                                }
                            }
                            daoHelper.createEdge(new Edge(i1, i2, supermarket));
                            edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                            break;
                        }
                    }
                }


            } else {
                /* Create new edge */
                daoHelper.createEdge(new Edge(i1, i2, supermarket));
                edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
            }

        } else {
            /* Edit existing edge - increase weight */
            edge.setWeight(edge.getWeight() + 1);
        }

        System.out.println("Calling insertIndirectEdges for node: " + i2.getName());
        insertIndirectEdges(i2, i1, supermarket);

        return edge;

    }

    /* Connects a BoughtItem currentNode with all ancestors of his parent and sets distance
     if it's lower than the existing distance for a given Supermarket*/
    public void insertIndirectEdges(BoughtItem currentNode, BoughtItem parent, Supermarket supermarket) {
        for (Edge edgeToParent : daoHelper.getEdgesByTo(parent, supermarket)) {
            BoughtItem ancestor = edgeToParent.getFrom();
            System.out.println("Ancestor found: " + ancestor.getName());
            Edge existingEdge;
            if ((existingEdge = daoHelper.getEdgeByFromTo(ancestor, currentNode, supermarket)) != null) {
                //update distance
                System.out.println("Existing edge found: " + ancestor.getName() +  "->" + currentNode.getName());
                existingEdge.setWeight(existingEdge.getWeight() + 1);
                if (existingEdge.getDistance() > edgeToParent.getDistance() + 1) existingEdge.setDistance(edgeToParent.getDistance() + 1);

            } else {
                //new Edge
                System.out.println("Created new indirect edge: " + ancestor.getName() + " -> " + currentNode.getName());
                existingEdge = new Edge(ancestor, currentNode, supermarket);
                existingEdge.setDistance(edgeToParent.getDistance() + 1);
                daoHelper.createEdge(existingEdge);
            }
        }
    }


    /* Adds the start and end Items for each Supermarket */
    private List<BoughtItem> addStartEnd(List<BoughtItem> boughtItemList) {
        String lastPlaceId = boughtItemList.get(0).getSupermarketPlaceId();
        String lastSupermarketName = boughtItemList.get(0).getSupermarketName();

        /* Add the very first start item and the very last end item */
        BoughtItem first = new BoughtItem(DAOHelper.START_ITEM, lastPlaceId, lastSupermarketName);
        first.setServerInternalItem(true);
        BoughtItem last  = new BoughtItem(DAOHelper.END_ITEM, boughtItemList.get(boughtItemList.size()-1).getSupermarketPlaceId(), boughtItemList.get(boughtItemList.size()-1).getSupermarketName());
        last.setServerInternalItem(true);
        boughtItemList.add(0, first);
        boughtItemList.add(boughtItemList.size(), last);

        for(int i = 0; i < boughtItemList.size(); i++) {
            BoughtItem current = boughtItemList.get(i);

            if(current.equals(daoHelper.getStartBoughtItem()) || current.equals(daoHelper.getEndBoughtItem()))
                continue;

            if(!current.getSupermarketPlaceId().equals(lastPlaceId)) {
                BoughtItem startItem = new BoughtItem(DAOHelper.START_ITEM, current.getSupermarketPlaceId(), current.getSupermarketName());
                startItem.setServerInternalItem(true);
                BoughtItem endItem   = new BoughtItem(DAOHelper.END_ITEM, lastPlaceId, lastSupermarketName);
                endItem.setServerInternalItem(true);
                boughtItemList.add(i, startItem);
                boughtItemList.add(i, endItem);

                lastPlaceId = current.getSupermarketPlaceId();
                lastSupermarketName = current.getSupermarketName();
            }
        }

        /*for(BoughtItem item : boughtItemList) {
            System.out.println(item.getName() + " - (" + item.getSupermarketName() + ")");
        }*/

        return boughtItemList;

    }

    public void addBoughtItems(List<BoughtItem> newBoughtItems) {

        List<BoughtItem> boughtItems = new ArrayList<>(newBoughtItems);

        /* Add start and end Items for every Supermarket */
        boughtItems = addStartEnd(boughtItems);

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            if(daoHelper.getBoughtItemByName(boughtItem.getName()) == null && !boughtItem.isServerInternalItem())
                daoHelper.createBoughtItem(boughtItem);
        }


        /* Save all new edges */
        for(int i = 0; i < boughtItems.size()-1; i++) {
            /* BoughtItems need to be loaded from the DB, otherwise Hibernate complains about unsaved objects */
            BoughtItem i1 = daoHelper.getBoughtItemByName(boughtItems.get(i).getName());
            BoughtItem i2 = daoHelper.getBoughtItemByName(boughtItems.get(i + 1).getName());

            if (i == 0) {
                i1 = daoHelper.getStartBoughtItem();
            } else if (i + 1 == boughtItems.size() - 1) {
                i2 = daoHelper.getEndBoughtItem();
            }

            /* Continue if the Items are not from the same Supermarket. Here we have to use the parameter boughtItems because the placeId is not stored in the DB */
            if(!boughtItems.get(i).getSupermarketPlaceId().equals(boughtItems.get(i + 1).getSupermarketPlaceId())) {
                continue;
            }

            /* Load / create the Supermarket */
            setSupermarket(boughtItems.get(i).getSupermarketPlaceId(), boughtItems.get(i).getSupermarketName());

            createOrUpdateEdge(i1, i2, supermarket);

            /* If this supermarkt belongs to a chain, apply the Edge to this chain's global graph */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = daoHelper.getGlobalSupermarketBySupermarketChain(supermarket.getSupermarketChain());
                createOrUpdateEdge(i1, i2, globalSupermarket);
            }
        }

        update();

    }

    /* Create a new ItemGraph, load all Edges and Vertices from 'supermarket' and copy them to this ItemGraph */
    private void copyDataFromItemGraph(Supermarket supermarket) {
        if(supermarket == null)
            return;
        
        ItemGraph globalItemGraph = new ItemGraph(this.daoHelper);
        globalItemGraph.setSupermarket(supermarket);
        globalItemGraph.updateGlobalItemGraph();
        this.edges = globalItemGraph.getEdges();
        this.vertices = globalItemGraph.getVertices();
        for(Edge edge : this.edges) {
            daoHelper.createEdge(edge);
        }
        for(BoughtItem boughtItem : this.vertices) {
            daoHelper.createBoughtItem(boughtItem);
        }
    }

    public ShoppingListServer sort(Algorithm algorithm, ShoppingListServer shoppingList, SortingRequest sortingRequest) {

        /* setSupermarket() returns true if this supermarket is new - in this case, try to use the SupermarketChain's ItemGraph */
        if(setSupermarket(sortingRequest.getPlaceId(), sortingRequest.getSupermarketName()) == true) {
            Supermarket globalSupermarket = daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain());

            /* If the Supermarket is new and does not belong to a chain, we can't sort the ShoppingList - just return the ShoppingList in this case */
            if(globalSupermarket == null)
                return shoppingList;
            else {
                /* Copy all Vertices and Edges from the global ItemGraph to this ItemGraph */
                copyDataFromItemGraph(globalSupermarket);
            }

            System.out.println("Using global ItemGraph for SupermarketChain " + supermarket.getSupermarketChain().getName());
        }

        /* Load the ItemGraph and sort the ShoppingList */
        update();
        algorithm.setUp(this);
        return (ShoppingListServer) algorithm.execute(shoppingList);

    }

    public void executeAlgorithm(Algorithm algorithm, List<BoughtItem> boughtItemList) {
        if(algorithm instanceof MagicSort) {
            throw new UnsupportedOperationException("Please use sort() to execute MagicSort");
        }
        update();
        algorithm.setUp(this);
        algorithm.execute(boughtItemList);
        update();
    }

    public List<BoughtItem> getParents(BoughtItem child) {
        List<BoughtItem> parents = new ArrayList<>();

        for(Edge edge: daoHelper.getEdgesByTo(child, supermarket)) {
            if(edge.getTo().equals(child) && edge.getDistance() == 0)
                parents.add(edge.getFrom());
        }

        return parents;
    }

    public List<BoughtItem> getChildren(BoughtItem parent) {
        List<BoughtItem> children = new ArrayList<>();

        for(Edge edge: getEdges()) {
            if(edge.getFrom().equals(parent) && edge.getDistance() == 0)
                children.add(edge.getTo());
        }

        return children;
    }

    public List<BoughtItem> getSiblings(BoughtItem child) {
        List<BoughtItem> siblings = new ArrayList<>();

        for(BoughtItem parent: getParents(child)) {
            siblings.addAll(getChildren(parent));
        }

        /* Remove all occurrences of this child */
        while(siblings.remove(child)) {}

        return siblings;
    }

    public Set<Edge> getEdgesFrom(BoughtItem boughtItem) {
        Set<Edge> edges = new HashSet<>();

        for(Edge edge: getEdges()) {
            if(edge.getFrom().equals(boughtItem))
                edges.add(edge);
        }

        return edges;
    }

    //checks if there is an edge from node from to node to
    public boolean edgeFromToExists(BoughtItem from, BoughtItem to){
        for(Edge edge : getEdgesFrom(from)) {
            if(edge.getTo().equals(to))
                return true;
        }

        return false;
    }

}

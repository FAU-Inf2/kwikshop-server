package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;

public class MagicSort implements Algorithm<ShoppingListServer, ShoppingListServer> {

    private ItemGraph itemGraph;
    private BoughtItemDAO boughtItemDAO;
    private EdgeDAO edgeDAO;

    private ShoppingListServer shoppingList;

    List<BoughtItem> knownItems; /* Items that exist on the ShoppingList and in this ItemGraph */
    List<BoughtItem> unknownItems; /* Items that exist on the ShoppingList but NOT in this ItemGraph */
    List<BoughtItem> sortedList; /* Sorted BoughtItems */

    @Override
    public void setUp(ItemGraph itemGraph) {
        this.itemGraph = itemGraph;
        this.boughtItemDAO = itemGraph.getBoughtItemDAO();
        this.edgeDAO = itemGraph.getEdgeDAO();
    }

    @Override
    public ShoppingListServer execute(ShoppingListServer shoppingListArg) {
        this.shoppingList = shoppingListArg;
        knownItems = new ArrayList<BoughtItem>();
        unknownItems = new ArrayList<BoughtItem>();
        sortedList = new ArrayList<BoughtItem>();

        sortedList.add(boughtItemDAO.getStart());

        /* Split the ShoppingList's Items into known and unknown Items */
        for(Item item: shoppingList.getItems()) {
            if(item.getDeleted())
                continue;

            BoughtItem boughtItem = boughtItemDAO.getByName(item.getName());

            /* Item doesn't exist in the DB */
            if(boughtItem == null) {
                unknownItems.add(boughtItem);
                continue;
            }

            boughtItem.setItemId(item.getServerId()); /* Set ItemId - used to map BoughtItems to ShoppingList Items */

            if(itemGraph.getVertices().contains(boughtItem)) {
                knownItems.add(boughtItem);
            } else {
                unknownItems.add(boughtItem);
            }
        }

        /* Step 3-5 */
        traverse(boughtItemDAO.getStart());

        System.out.println("----------");
        for(BoughtItem item: sortedList) {
            System.out.println("---> " + item.getName());
        }
        System.out.println("----------");

        /* Step 6 */
        while(knownItems.size() > 0)
            addMissingItems(knownItems.get(0));

        /* Add unknown Items at the end */
        sortedList.addAll(unknownItems);

        sortedList.remove(boughtItemDAO.getStart());

        for(BoughtItem item: sortedList) {
            System.out.println("===> " + item.getName());
        }

        /* Apply the order of BoughtItems to the ShoppingList */
        applyOrderToShoppingList();

        return shoppingList;
    }

    /* Steps 3 - 5 (Sorting Algorithm.md) */
    private void traverse(BoughtItem currentItem) {

        Set<Edge> currentEdges = itemGraph.getEdgesFrom(currentItem);
        double maxWeightDistanceRatio = 0;
        BoughtItem nextItem = null;

        for(Edge edge: currentEdges) {
            if(edge.getDistance() > 0)
                continue;

            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);

            if( (currentWeightDistanceRatio > maxWeightDistanceRatio) &&
                    (knownItems.contains(edge.getTo())) ) {
                maxWeightDistanceRatio = currentWeightDistanceRatio;
                nextItem = edge.getTo();
            }
        }

        if(nextItem == null)
            return;

        knownItems.remove(nextItem);
        sortedList.add(nextItem);

        traverse(nextItem);
    }

    private void addMissingItems(BoughtItem item) {
        System.out.println("MISSING ITEM: " + item.getName());

        double maxWeightDistanceRatio = 0;

        BoughtItem insertAfter = null;
        List<Edge> parentEdges = edgeDAO.getByTo(item, itemGraph.getSupermarket());

        for(Edge edge: parentEdges) {
            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);

            System.out.println("MISSING ITEM EDGE: " + edge.getFrom().getName() + " -> " + edge.getTo().getName() + " (" + currentWeightDistanceRatio + ")");

            if(currentWeightDistanceRatio > maxWeightDistanceRatio) /*&&
                    (sortedList.contains(edge.getFrom())) )*/ {
                maxWeightDistanceRatio = currentWeightDistanceRatio;
                insertAfter = edge.getFrom();
            }
        }

        knownItems.remove(item);

        if(insertAfter != null) {

            if(!sortedList.contains(insertAfter)) {
                knownItems.add(0, insertAfter);
                addMissingItems(knownItems.get(0));
            }

            System.out.println("INSERT AFTER: " + insertAfter.getName());
            sortedList.add(sortedList.indexOf(insertAfter) + 1, item);
        } else {
            if(knownItems.size() == 0) {
                unknownItems.add(item);
                return;
            }

            knownItems.add(knownItems.size(), item); /* Insert this Item at the end */
        }

    }

    private void applyOrderToShoppingList() {
        int i = 0;
        for(BoughtItem boughtItem: sortedList) {
            Item item = shoppingList.getItem(boughtItem.getItemId());
            if(item == null)
                continue;

            System.out.println("Setting order for: "+item.getName());
            item.setOrder(i);
            item.setVersion(item.getVersion()+1);
            i++;
        }
        shoppingList.setVersion(shoppingList.getVersion()+1);
        shoppingList.setLastModifiedDate(new Date());
    }


}
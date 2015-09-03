package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;

public class MagicSort implements Algorithm<ShoppingListServer, ShoppingListServer> {

    private ItemGraph itemGraph;
    private BoughtItemDAO boughtItemDAO;

    List<BoughtItem> knownItems;
    List<BoughtItem> unknownItems;
    List<BoughtItem> sortedList;

    @Override
    public void setUp(ItemGraph itemGraph) {
        this.itemGraph = itemGraph;
        this.boughtItemDAO = itemGraph.getBoughtItemDAO();
    }

    @Override
    public ShoppingListServer execute(ShoppingListServer shoppingList) {
        knownItems = new ArrayList<BoughtItem>(); /* Items that exist on the ShoppingList and in this ItemGraph */
        unknownItems = new ArrayList<BoughtItem>(); /* Items that exist on the ShoppingList but NOT in this ItemGraph */
        sortedList = new ArrayList<BoughtItem>(); /* Sorted BoughtItems */

        /* Split the ShoppingList's Items into known and unknown Items */
        for(Item item: shoppingList.getItems()) {
            BoughtItem boughtItem = boughtItemDAO.getByName(item.getName());

            /* Item doesn't exist in the DB */
            if(boughtItem == null) {
                unknownItems.add(boughtItem);
                continue;
            }

            if(itemGraph.getVertices().contains(boughtItem)) {
                knownItems.add(boughtItem);
            } else {
                unknownItems.add(boughtItem);
            }
        }

        traverse(boughtItemDAO.getStart());

        // Todo: Step 6


        sortedList.addAll(unknownItems);

        // Todo: sort ShoppingList according to sortedList
        
        return shoppingList;
    }

    /* Steps 3 - 5 (Sorting Algorithm.md) */
    private boolean traverse(BoughtItem currentItem) {

        Set<Edge> currentEdges = itemGraph.getEdgesFrom(currentItem);
        double maxWeightDistanceRatio = 0;
        BoughtItem nextItem = null;

        for(Edge edge: currentEdges) {
            double currentWeightDistanceRatio = (double)edge.getWeight()+1 / (double)edge.getDistance()+1;

            if( (currentWeightDistanceRatio > maxWeightDistanceRatio) &&
                    (knownItems.contains(edge.getTo())) ) {
                maxWeightDistanceRatio = currentWeightDistanceRatio;
                nextItem = edge.getTo();
            }
        }

        if(nextItem == null)
            return true;

        sortedList.add(nextItem);

        return traverse(nextItem);
    }


}

package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketChainDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketDAO;

public class IndirectEdgeInsertion implements Algorithm<List<BoughtItem>, ItemGraph> {

    private ItemGraph itemGraph;
    private DAOHelper daoHelper;

    @Override
    public void setUp(ItemGraph itemGraph) {
        this.itemGraph = itemGraph;
        this.daoHelper = itemGraph.getDaoHelper();
    }

    private void createEdge(BoughtItem startItem, BoughtItem endItem, Supermarket supermarket, int distance) {
        Edge edge = daoHelper.getEdgeByFromTo(startItem, endItem, supermarket);

        System.out.println("INDIRECT EDGE - " + startItem.getName() + " -> " + endItem.getName() + "(" + distance + ")");

        if(edge != null) {
            edge.setWeight(edge.getWeight()+1);
            edge.addDistance(distance);
        } else {
            edge = new Edge(startItem, endItem, supermarket);
            edge.addDistance(distance);
            daoHelper.createEdge(edge);
        }
    }

    @Override
    public ItemGraph execute(List<BoughtItem> boughtItemList) {

        for(int start = 0; start < boughtItemList.size()-1; start++) {
            for(int end = start+1; end < boughtItemList.size(); end++) {

                int distance = end-start-1;
                if(distance < 1)
                    continue;

                BoughtItem startItem = daoHelper.getBoughtItemByName(boughtItemList.get(start).getName());
                BoughtItem endItem   = daoHelper.getBoughtItemByName(boughtItemList.get(end).getName());

                if(!boughtItemList.get(start).getSupermarketPlaceId().equals(boughtItemList.get(end).getSupermarketPlaceId())) {
                    break;
                }

                Supermarket supermarket = daoHelper.getSupermarketByPlaceID(boughtItemList.get(start).getSupermarketPlaceId());

                createEdge(startItem, endItem, supermarket, distance);

                /* Also add the indirect Edge to the global SupermarketChain's ItemGraph */
                if(supermarket.getSupermarketChain() != null) {
                    createEdge(startItem, endItem, daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain()), distance);
                }

            }
        }

        return itemGraph;

    }
}

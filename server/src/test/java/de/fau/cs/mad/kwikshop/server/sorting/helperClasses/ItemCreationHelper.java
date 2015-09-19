package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class ItemCreationHelper {

    public List<BoughtItem> createBoughtItems(int numberOfItemsToCreate, String supermarketPlaceId) {
        List<BoughtItem> items = new ArrayList<>(numberOfItemsToCreate);
        for (int i = 0; i < numberOfItemsToCreate; i++) {
            BoughtItem item = createBoughtItemWithIdAndSupermarket(i, supermarketPlaceId);
            item.setId(i);
            items.add(item);
        }
        return items;
    }

    public ShoppingListServer createShoppingListServerWithNItems(int n) {
        ArrayList<Item> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Item item = createItemWithId(i);
            items.add(item);
        }
        return new ShoppingListServer(0, items);
    }

    public ShoppingListServer createShoppingListServerWithNItemsMixedUp(int n) {
        List<Item> orderedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Item item = createItemWithId(i);
            orderedItems.add(item);
        }

        Random random = new Random(n*n); // random generator with some random seed

        ArrayList<Item> randomItems = new ArrayList<>();
        while (!orderedItems.isEmpty()) {
            int index = random.nextInt(orderedItems.size());
            Item item = orderedItems.remove(index);
            randomItems.add(item);
        }
        return new ShoppingListServer(0, randomItems);
    }

    public Item createItemWithId(int id) {
        Item item = new Item();
        item.setName("i" + id);
        item.setID(id);
        item.setServerId(id);
        return item;
    }

    public BoughtItem createBoughtItemWithIdAndSupermarket(int id, String supermarketPlaceId) {
        BoughtItem item = new BoughtItem("i" + id, supermarketPlaceId, supermarketPlaceId);
        item.setId(id);
        return item;
    }
}

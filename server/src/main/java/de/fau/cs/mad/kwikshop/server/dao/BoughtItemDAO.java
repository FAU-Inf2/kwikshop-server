package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import io.dropwizard.hibernate.AbstractDAO;

public class BoughtItemDAO extends AbstractDAO<BoughtItem> {

    public final String END_ITEM = "END_ITEM";
    public final String START_ITEM = "START_ITEM";


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public BoughtItemDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    public BoughtItem getByName(String name) {
        return getByName(name, false);
    }

    public BoughtItem getByNameIncludingStartAndEnd(String name) {
        return getByName(name, true);
    }

    private BoughtItem getByName(String name, boolean serverInternalItem) {
        Query query = namedQuery(NamedQueryConstants.BOUGHTITEM_GET_BY_NAME)
                .setParameter(NamedQueryConstants.BOUGHTITEM_NAME, name);

        List<BoughtItem> items = list(query);
        List<BoughtItem> filteredItems = new ArrayList<>(1);
        if (serverInternalItem) {
            for (BoughtItem item : items) {
                if (!item.isServerInternalItem()) {
                    continue; // ignore non-server internal items
                }
                filteredItems.add(item);
            }
        } else {
            for (BoughtItem item : items) {
                if (item.isServerInternalItem()) {
                    continue; // ignore server internal items
                }
                filteredItems.add(item);
            }
        }

        if(filteredItems.size() == 0) {
            return null;
        } else if(filteredItems.size() == 1) {
            return filteredItems.get(0);
        } else {
            throw new UnsupportedOperationException("Query for BoughtItem by Name yielded more than one result");
        }
    }

    public BoughtItem getStart() {
        BoughtItem start = getByName(START_ITEM, true);
        if(start == null) {
            BoughtItem newBoughtItem = new BoughtItem(START_ITEM);
            newBoughtItem.setServerInternalItem(true);
            createBoughtItem(newBoughtItem);
            start = getByName(START_ITEM);
        }
        return start;
    }

    public BoughtItem getEnd() {
        BoughtItem end = getByName(END_ITEM, true);
        if(end == null) {
            BoughtItem newBoughtItem = new BoughtItem(END_ITEM);
            newBoughtItem.setServerInternalItem(true);
            createBoughtItem(newBoughtItem);
            end = getByName(END_ITEM);
        }
        return end;
    }

    public void createBoughtItem(BoughtItem boughtItem) {
        currentSession().saveOrUpdate(boughtItem);
    }

}

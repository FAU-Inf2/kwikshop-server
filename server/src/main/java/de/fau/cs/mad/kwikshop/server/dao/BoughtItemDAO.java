package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

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
        Query query = namedQuery(NamedQueryConstants.BOUGHTITEM_GET_BY_NAME)
                .setParameter(NamedQueryConstants.BOUGHTITEM_NAME, name);

        List<BoughtItem> items = list(query);

        if(items.size() == 0) {
            return null;
        } else if(items.size() == 1) {
            return items.get(0);
        } else {
            throw new UnsupportedOperationException("Query for BoughtItem by Name yielded more than one result");
        }
    }

    public BoughtItem getStart() {
        BoughtItem start = getByName(START_ITEM);
        if(start == null) {
            createBoughtItem(new BoughtItem(START_ITEM));
            start = getByName(START_ITEM);
        }
        return start;
    }

    public BoughtItem getEnd() {
        BoughtItem end = getByName(END_ITEM);
        if(end == null) {
            createBoughtItem(new BoughtItem(END_ITEM));
            end = getByName(END_ITEM);
        }
        return end;
    }

    public void createBoughtItem(BoughtItem boughtItem) {
        currentSession().saveOrUpdate(boughtItem);
    }

}

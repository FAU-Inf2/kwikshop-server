package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;

public class SupermarketHelper {

    private final DAODummyHelper daoDummyHelper;

    public SupermarketHelper(DAODummyHelper daoDummyHelper) {
        if (daoDummyHelper == null) {
            daoDummyHelper = new DAODummyHelper();
        }
        this.daoDummyHelper = daoDummyHelper;
    }

    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        return daoDummyHelper.getGlobalSupermarket(supermarketChain);
    }
}

package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

public class ServerEqualityComparer extends EqualityComparer {

    @Override
    protected boolean idEquals(Group group1, Group group2) {
        return group1.getServerId() == group2.getServerId();
    }

    @Override
    protected boolean idEquals(Unit unit1, Unit unit2) {
        return unit1.getServerId() == unit2.getServerId();
    }

    @Override
    protected boolean idEquals(LastLocation location1, LastLocation location2) {
        return location1.getServerId() == location2.getServerId();
    }

}

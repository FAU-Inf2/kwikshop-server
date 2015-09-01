package de.fau.cs.mad.kwikshop.server.sorting;

public interface Algorithm<TI, TO> {

    void setUp(ItemGraph itemGraph);
    TO execute(TI ti);

}

package com.hereticpurge.inventorymanager.model;

public class DebugProductItemFactory {

    // Simple class that adds debug product items to the application with randomized data for
    // testing purposes.

    public static ProductItem getDebugProduct() {
        String barcode = String.valueOf(Math.random() * 10000);
        return getDebugProduct(barcode);
    }

    public static ProductItem getDebugProduct(String barcode) {
        String customId = String.valueOf(Math.random() * 10000);
        String name = "Debug Item";
        String cost = "4.00";
        String retail = "9.99";
        int currentStock = Double.valueOf(Math.random() * 1000).intValue();
        int targetStock = Double.valueOf(Math.random() * 1000).intValue();
        boolean tracked = Math.random() * 10 < 5;

        return new ProductItem(barcode, customId, name, cost, retail, currentStock, targetStock, tracked);
    }
}

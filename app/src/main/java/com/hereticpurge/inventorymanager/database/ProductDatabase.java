package com.hereticpurge.inventorymanager.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.hereticpurge.inventorymanager.model.ProductItem;

@Database(entities = {ProductItem.class}, version = 1)
public abstract class ProductDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "inventory_manager";

    // Simple singleton to handle the product database via room.
    private static ProductDatabase database;

    public abstract ProductDao productDao();

    public static ProductDatabase getDatabase(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    ProductDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public static void destroyInstance() {
        database = null;
    }

}

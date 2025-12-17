package com.example.assignment1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ComputerInventory.db";
    // Increment to 6 for Date Field Fix
    private static final int DATABASE_VERSION = 6;

    // Table Brands
    public static final String TABLE_BRAND = "brands";
    public static final String COLUMN_BRAND_ID = "_id";
    public static final String COLUMN_BRAND_NAME = "name";
    public static final String COLUMN_BRAND_DESC = "description";

    // Table Computers (Now effectively Machines)
    public static final String TABLE_COMPUTER = "computers";
    public static final String COLUMN_COMPUTER_ID = "_id";
    public static final String COLUMN_COMPUTER_MODEL = "model"; // This will start storing "Name"
    public static final String COLUMN_COMPUTER_SERIAL = "serial_number"; // New
    public static final String COLUMN_COMPUTER_LOCATION = "location"; // New
    public static final String COLUMN_COMPUTER_IMAGE = "image_uri";
    public static final String COLUMN_COMPUTER_BRAND_ID = "brand_id";
    public static final String COLUMN_COMPUTER_STATUS = "status";
    public static final String COLUMN_COMPUTER_DATE = "date_added"; // New Column

    private static final String CREATE_TABLE_BRAND = "CREATE TABLE " + TABLE_BRAND + "(" +
            COLUMN_BRAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_BRAND_NAME + " TEXT," +
            COLUMN_BRAND_DESC + " TEXT" +
            ")";

    private static final String CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_COMPUTER + "(" +
            COLUMN_COMPUTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_COMPUTER_MODEL + " TEXT," +
            COLUMN_COMPUTER_SERIAL + " TEXT," +
            COLUMN_COMPUTER_LOCATION + " TEXT," +
            COLUMN_COMPUTER_IMAGE + " TEXT," +
            COLUMN_COMPUTER_BRAND_ID + " INTEGER," +
            COLUMN_COMPUTER_STATUS + " TEXT," +
            COLUMN_COMPUTER_DATE + " TEXT," +
            "FOREIGN KEY(" + COLUMN_COMPUTER_BRAND_ID + ") REFERENCES " + TABLE_BRAND + "(" + COLUMN_BRAND_ID + ")" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_BRAND);
            db.execSQL(CREATE_TABLE_COMPUTER);
            seedBrands(db);
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Error seeding database", e);
        }
    }

    private void seedBrands(SQLiteDatabase db) {
        String[] brands = { "Dell", "HP", "Apple", "Lenovo", "Asus" };
        for (String brand : brands) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BRAND_NAME, brand);
            values.put(COLUMN_BRAND_DESC, "Description for " + brand);
            db.insert(TABLE_BRAND, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRAND);
        onCreate(db);
    }

    // --- Brand Operations ---
    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRAND, null, null, null, null, null, COLUMN_BRAND_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Brand brand = new Brand(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND_DESC)));
                brands.add(brand);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return brands;
    }

    public long addBrand(String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRAND_NAME, name);
        values.put(COLUMN_BRAND_DESC, description);
        return db.insert(TABLE_BRAND, null, values);
    }

    public int updateBrand(long id, String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRAND_NAME, name);
        values.put(COLUMN_BRAND_DESC, description);
        return db.update(TABLE_BRAND, values, COLUMN_BRAND_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void deleteBrand(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BRAND, COLUMN_BRAND_ID + " = ?", new String[] { String.valueOf(id) });
    }

    // --- Computer (Machine) Operations ---
    public long addComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPUTER_MODEL, computer.getModel());
        values.put(COLUMN_COMPUTER_SERIAL, computer.getSerialNumber());
        values.put(COLUMN_COMPUTER_LOCATION, computer.getLocation());
        values.put(COLUMN_COMPUTER_IMAGE, computer.getImageUri());
        values.put(COLUMN_COMPUTER_BRAND_ID, computer.getBrandId());
        values.put(COLUMN_COMPUTER_STATUS, computer.getStatus());
        values.put(COLUMN_COMPUTER_DATE, computer.getDateAdded());

        try {
            return db.insertOrThrow(TABLE_COMPUTER, null, values);
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Error adding machine", e);
            return -1;
        }
    }

    public Computer getComputer(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT c.*, b." + COLUMN_BRAND_NAME + " as brand_name " +
                "FROM " + TABLE_COMPUTER + " c " +
                "LEFT JOIN " + TABLE_BRAND + " b ON c." + COLUMN_COMPUTER_BRAND_ID + " = b." + COLUMN_BRAND_ID +
                " WHERE c." + COLUMN_COMPUTER_ID + " = " + id;

        Cursor cursor = db.rawQuery(selectQuery, null);
        Computer computer = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                computer = new Computer(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_MODEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_SERIAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IMAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_DATE))); // Added Date
                computer.setBrandName(cursor.getString(cursor.getColumnIndexOrThrow("brand_name")));

                int statusIdx = cursor.getColumnIndex(COLUMN_COMPUTER_STATUS);
                if (statusIdx != -1) {
                    computer.setStatus(cursor.getString(statusIdx));
                } else {
                    computer.setStatus("Active");
                }
            }
            cursor.close();
        }
        return computer;
    }

    public int updateComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPUTER_MODEL, computer.getModel());
        values.put(COLUMN_COMPUTER_SERIAL, computer.getSerialNumber());
        values.put(COLUMN_COMPUTER_LOCATION, computer.getLocation());
        values.put(COLUMN_COMPUTER_IMAGE, computer.getImageUri());
        values.put(COLUMN_COMPUTER_BRAND_ID, computer.getBrandId());
        values.put(COLUMN_COMPUTER_STATUS, computer.getStatus());
        values.put(COLUMN_COMPUTER_DATE, computer.getDateAdded());

        return db.update(TABLE_COMPUTER, values, COLUMN_COMPUTER_ID + " = ?",
                new String[] { String.valueOf(computer.getId()) });
    }

    public void deleteComputer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMPUTER, COLUMN_COMPUTER_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public List<Computer> getAllComputers() {
        List<Computer> computers = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT c.*, b." + COLUMN_BRAND_NAME + " as brand_name " +
                "FROM " + TABLE_COMPUTER + " c " +
                "LEFT JOIN " + TABLE_BRAND + " b ON c." + COLUMN_COMPUTER_BRAND_ID + " = b." + COLUMN_BRAND_ID +
                " ORDER BY c." + COLUMN_COMPUTER_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Computer computer = new Computer(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_MODEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_SERIAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IMAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_DATE)));
                computer.setBrandName(cursor.getString(cursor.getColumnIndexOrThrow("brand_name")));

                int statusIdx = cursor.getColumnIndex(COLUMN_COMPUTER_STATUS);
                if (statusIdx != -1) {
                    computer.setStatus(cursor.getString(statusIdx));
                } else {
                    computer.setStatus("Active");
                }

                computers.add(computer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return computers;
    }

    public List<Computer> searchComputers(String keyword) {
        List<Computer> computers = new ArrayList<>();
        String selectQuery = "SELECT c.*, b." + COLUMN_BRAND_NAME + " as brand_name " +
                "FROM " + TABLE_COMPUTER + " c " +
                "LEFT JOIN " + TABLE_BRAND + " b ON c." + COLUMN_COMPUTER_BRAND_ID + " = b." + COLUMN_BRAND_ID +
                " WHERE c." + COLUMN_COMPUTER_MODEL + " LIKE ? OR c." + COLUMN_COMPUTER_SERIAL + " LIKE ? OR b."
                + COLUMN_BRAND_NAME + " LIKE ?" +
                " ORDER BY c." + COLUMN_COMPUTER_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,
                new String[] { "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%" });

        if (cursor.moveToFirst()) {
            do {
                Computer computer = new Computer(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_MODEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_SERIAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IMAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_DATE)));
                computer.setBrandName(cursor.getString(cursor.getColumnIndexOrThrow("brand_name")));

                int statusIdx = cursor.getColumnIndex(COLUMN_COMPUTER_STATUS);
                if (statusIdx != -1) {
                    computer.setStatus(cursor.getString(statusIdx));
                } else {
                    computer.setStatus("Active");
                }

                computers.add(computer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return computers;
    }

    public boolean isBrandExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRAND, new String[] { COLUMN_BRAND_ID },
                COLUMN_BRAND_NAME + " = ?", new String[] { name }, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isBrandUsed(long brandId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPUTER, new String[] { COLUMN_COMPUTER_ID },
                COLUMN_COMPUTER_BRAND_ID + " = ?", new String[] { String.valueOf(brandId) }, null, null, null);
        boolean used = (cursor.getCount() > 0);
        cursor.close();
        return used;
    }
}

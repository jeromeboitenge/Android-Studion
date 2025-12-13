package com.example.assignment1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ComputerInventory.db";
    private static final int DATABASE_VERSION = 1;

    // Table Brands
    public static final String TABLE_BRAND = "brands";
    public static final String COLUMN_BRAND_ID = "_id";
    public static final String COLUMN_BRAND_NAME = "name";
    public static final String COLUMN_BRAND_DESC = "description";

    // Table Computers
    public static final String TABLE_COMPUTER = "computers";
    public static final String COLUMN_COMPUTER_ID = "_id";
    public static final String COLUMN_COMPUTER_MODEL = "model";
    public static final String COLUMN_COMPUTER_PRICE = "price";
    public static final String COLUMN_COMPUTER_DATE = "purchase_date";
    public static final String COLUMN_COMPUTER_IS_LAPTOP = "is_laptop";
    public static final String COLUMN_COMPUTER_IMAGE = "image_uri";
    public static final String COLUMN_COMPUTER_BRAND_ID = "brand_id";

    private static final String CREATE_TABLE_BRAND = "CREATE TABLE " + TABLE_BRAND + "(" +
            COLUMN_BRAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_BRAND_NAME + " TEXT," +
            COLUMN_BRAND_DESC + " TEXT" +
            ")";

    private static final String CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_COMPUTER + "(" +
            COLUMN_COMPUTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_COMPUTER_MODEL + " TEXT," +
            COLUMN_COMPUTER_PRICE + " REAL," +
            COLUMN_COMPUTER_DATE + " TEXT," +
            COLUMN_COMPUTER_IS_LAPTOP + " INTEGER," +
            COLUMN_COMPUTER_IMAGE + " TEXT," +
            COLUMN_COMPUTER_BRAND_ID + " INTEGER," +
            "FOREIGN KEY(" + COLUMN_COMPUTER_BRAND_ID + ") REFERENCES " + TABLE_BRAND + "(" + COLUMN_BRAND_ID + ")" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BRAND);
        db.execSQL(CREATE_TABLE_COMPUTER);

        // Seed some brands
        seedBrands(db);
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

    // --- Computer Operations ---
    public long addComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPUTER_MODEL, computer.getModel());
        values.put(COLUMN_COMPUTER_PRICE, computer.getPrice());
        values.put(COLUMN_COMPUTER_DATE, computer.getPurchaseDate());
        values.put(COLUMN_COMPUTER_IS_LAPTOP, computer.isLaptop() ? 1 : 0);
        values.put(COLUMN_COMPUTER_IMAGE, computer.getImageUri());
        values.put(COLUMN_COMPUTER_BRAND_ID, computer.getBrandId());

        return db.insert(TABLE_COMPUTER, null, values);
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
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IS_LAPTOP)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IMAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_BRAND_ID)));
                computer.setBrandName(cursor.getString(cursor.getColumnIndexOrThrow("brand_name")));
            }
            cursor.close();
        }
        return computer;
    }

    public int updateComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPUTER_MODEL, computer.getModel());
        values.put(COLUMN_COMPUTER_PRICE, computer.getPrice());
        values.put(COLUMN_COMPUTER_DATE, computer.getPurchaseDate());
        values.put(COLUMN_COMPUTER_IS_LAPTOP, computer.isLaptop() ? 1 : 0);
        values.put(COLUMN_COMPUTER_IMAGE, computer.getImageUri());
        values.put(COLUMN_COMPUTER_BRAND_ID, computer.getBrandId());

        return db.update(TABLE_COMPUTER, values, COLUMN_COMPUTER_ID + " = ?",
                new String[] { String.valueOf(computer.getId()) });
    }

    public void deleteComputer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMPUTER, COLUMN_COMPUTER_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public List<Computer> getAllComputers() {
        List<Computer> computers = new ArrayList<>();
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
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IS_LAPTOP)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_IMAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPUTER_BRAND_ID)));
                computer.setBrandName(cursor.getString(cursor.getColumnIndexOrThrow("brand_name")));
                computers.add(computer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return computers;
    }
}

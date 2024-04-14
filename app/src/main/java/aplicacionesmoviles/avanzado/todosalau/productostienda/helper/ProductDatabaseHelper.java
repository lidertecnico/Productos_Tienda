package aplicacionesmoviles.avanzado.todosalau.productostienda.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import aplicacionesmoviles.avanzado.todosalau.productostienda.model.Product;

public class ProductDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";

    public ProductDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PRICE + " REAL)";
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public void addProduct(Product product) {
        if (existeProducto(product.getId())) {
            Log.i("ProductDatabaseHelper", "Producto con ID " + product.getId() + " ya existe.");
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, product.getId());
            values.put(COLUMN_NAME, product.getName());
            values.put(COLUMN_PRICE, product.getPrice());
            db.insert(TABLE_PRODUCTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            int priceIndex = cursor.getColumnIndex(COLUMN_PRICE);

            if (nameIndex != -1 && priceIndex != -1 && idIndex != -1) {
                do {
                    String id = cursor.getString(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    products.add(new Product(id, name, price));
                } while (cursor.moveToNext());
            } else {
                Log.e("ProductDatabaseHelper", "Columnas no encontradas.");
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return products;
    }

    public void updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());

        int rowsUpdated = db.update(TABLE_PRODUCTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(product.getId())});
        db.close();

        if (rowsUpdated == 0) {
            Log.e("ProductDatabaseHelper", "No se actualizó ninguna fila. ID de producto inválido.");
        }
    }

    public void deleteProduct(String id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_PRODUCTS, COLUMN_ID + "=?", new String[]{id});

            if (rowsDeleted > 0) {
                Log.i("ProductDatabaseHelper", "Producto eliminado exitosamente.");
            } else {
                Log.e("ProductDatabaseHelper", "No se eliminó ningún producto. ID no válido: " + id);
            }
        } catch (Exception e) {
            Log.e("ProductDatabaseHelper", "Error al eliminar el producto", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean existeProducto(String id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean existe = false;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_ID + "=?", new String[]{id});

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                existe = count > 0;
            }
        } catch (Exception e) {
            Log.e("ProductDatabaseHelper", "Error verificando existencia de producto", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return existe;
    }
}

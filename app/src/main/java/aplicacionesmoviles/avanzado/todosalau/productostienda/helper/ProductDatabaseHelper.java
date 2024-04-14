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

    // Nombre de la base de datos y versión
    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y columnas
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";

    // Constructor
    public ProductDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método para crear la tabla de productos en la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PRICE + " REAL)";
        db.execSQL(createTableSQL);
    }

    // Método para manejar la actualización de la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    // Método para agregar un producto a la base de datos
    public void addProduct(Product product) {
        // Verificar si el producto ya existe antes de agregarlo
        if (productExists(product.getId())) {
            Log.i("ProductDatabaseHelper", "Producto con ID " + product.getId() + " ya existe.");
            return; // Detener la ejecución si el producto ya existe
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
            Log.e("ProductDatabaseHelper", "Error al agregar producto", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para obtener todos los productos de la base de datos
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Índices de columnas
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            int priceIndex = cursor.getColumnIndex(COLUMN_PRICE);

            if (idIndex != -1 && nameIndex != -1 && priceIndex != -1) {
                do {
                    // Obtener datos de cada producto
                    String id = cursor.getString(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    // Agregar producto a la lista
                    products.add(new Product(id, name, price));
                } while (cursor.moveToNext());
            } else {
                Log.e("ProductDatabaseHelper", "Columnas no encontradas.");
            }
        }

        // Cerrar el cursor y la base de datos
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return products;
    }

    // Método para actualizar un producto en la base de datos
    public void updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());

        // Actualizar el producto en la base de datos
        int rowsUpdated = db.update(TABLE_PRODUCTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(product.getId())});
        db.close();

        // Verificar si la actualización fue exitosa
        if (rowsUpdated == 0) {
            Log.e("ProductDatabaseHelper", "No se actualizó ninguna fila. ID de producto inválido.");
        }
    }

    // Método para eliminar un producto de la base de datos
    public void deleteProduct(String id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            // Eliminar el producto
            int rowsDeleted = db.delete(TABLE_PRODUCTS, COLUMN_ID + "=?", new String[]{id});

            // Verificar si la eliminación fue exitosa
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

    // Método para verificar si un producto existe en la base de datos
    public boolean productExists(String id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;

        try {
            db = this.getReadableDatabase();
            // Consulta para verificar si el producto existe
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_ID + "=?", new String[]{id});

            // Si el cursor tiene datos, obtener el conteo
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                exists = count > 0;
            }
        } catch (Exception e) {
            Log.e("ProductDatabaseHelper", "Error verificando existencia de producto", e);
        } finally {
            // Cerrar el cursor y la base de datos
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return exists;
    }
}
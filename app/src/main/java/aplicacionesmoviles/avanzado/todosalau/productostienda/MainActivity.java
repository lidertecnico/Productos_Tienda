package aplicacionesmoviles.avanzado.todosalau.productostienda;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aplicacionesmoviles.avanzado.todosalau.productostienda.adapter.ProductAdapter;
import aplicacionesmoviles.avanzado.todosalau.productostienda.helper.ProductDatabaseHelper;
import aplicacionesmoviles.avanzado.todosalau.productostienda.helper.ProductFirebaseHelper;
import aplicacionesmoviles.avanzado.todosalau.productostienda.model.Product;
import aplicacionesmoviles.avanzado.todosalau.productostienda.monitor.NetworkMonitor;

public class MainActivity extends AppCompatActivity {

    private EditText editTextId, editTextName, editTextPrice;
    private Button buttonAdd, buttonGetFirebase, buttonSichronized;
    private ListView listViewProducts;
    private ProductDatabaseHelper databaseHelper;
    private ProductAdapter productAdapter;
    private ProductFirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar los componentes de la interfaz de usuario
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonGetFirebase = findViewById(R.id.buttonGetFirebase);
        buttonSichronized = findViewById(R.id.buttonSichronized);
        listViewProducts = findViewById(R.id.listViewProducts);

        // Inicializar la base de datos y el adaptador
        databaseHelper = new ProductDatabaseHelper(this);
        List<Product> products = databaseHelper.getAllProducts();
        productAdapter = new ProductAdapter(this, R.layout.list_item_product, products);
        listViewProducts.setAdapter(productAdapter);

        //instancia de networkmonitor
        NetworkMonitor networkMonitor = new NetworkMonitor(this);

        // Configurar eventos del botón "Ver SQLite"
        Button buttonGetSqlite = findViewById(R.id.buttonGetSqlite);
        buttonGetSqlite.setOnClickListener(v -> loadProductsFromDatabase());

        //Autorización usaurio anonimo
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // El inicio de sesión anónimo fue exitoso
                        FirebaseUser user = auth.getCurrentUser();
                        // Puedes usar el usuario para otorgar permisos
                        // Por ejemplo, puedes usar el user.getUid() para identificar al usuario
                    } else {
                        // El inicio de sesión anónimo falló
                        Toast.makeText(MainActivity.this, "Error al iniciar sesión anónimo", Toast.LENGTH_SHORT).show();
                    }
                });

        //Configurar eventos del boton sincronizhed
        buttonSichronized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar si hay conexión a internet
                if (!networkMonitor.isNetworkAvailable()) {
                    // Mostrar aviso de falta de conexión
                    Toast.makeText(MainActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
                    return; // Salir del método sin sincronizar
                }

                // Si hay conexión a internet, continuar con la sincronización
                // Llama primero a la función synchronizeAndRemoveData()
                synchronizeAndRemoveData();

                // Luego, llama a la función synchronizeAndLoadData()
                synchronizeAndLoadData();
            }
        });     //Cargar los datos que estan en sqlite pero no en firebase

        // Inicializar la instancia de ProductFirebaseHelper
        firebaseHelper = new ProductFirebaseHelper();

        // Configurar evento del botón "Ver Firebase"
        buttonGetFirebase.setOnClickListener(v -> loadProductsFromFirebase(true));

        // Configurar evento del botón "Agregar"
        buttonAdd.setOnClickListener(v -> {
            if (buttonAdd.getText().toString().equalsIgnoreCase("Agregar")) {
                addProduct();
            } else {
                saveProduct();
            }
        });

    }


    private void loadProductsFromDatabase() {
        // Cargar productos de la base de datos SQLite
        List<Product> products = databaseHelper.getAllProducts();

        // Establecer la visibilidad de los botones en falso para hacerlos visibles
        productAdapter.setHideButtons(false);

        // Limpiar el adaptador y añadir todos los productos
        productAdapter.clear();
        productAdapter.addAll(products);

        // Notificar al adaptador que los datos han cambiado
        productAdapter.notifyDataSetChanged();
    }

    private void synchronizeAndLoadData() {
        // Paso 1: Obtener los productos de SQLite
        List<Product> productsFromSQLite = databaseHelper.getAllProducts();

        // Paso 2: Sincronizar los productos con Firebase
        for (Product product : productsFromSQLite) {
            // Verificar si el producto ya existe en Firebase
            firebaseHelper.checkIfProductExists(product.getId(), new ProductFirebaseHelper.ProductExistsCallback() {
                @Override
                public void onProductExists(boolean exists) {
                    if (exists) {
                        // Si el producto existe, actualízalo en Firebase
                        firebaseHelper.updateProduct(product);
                    } else {
                        // Si el producto no existe, agrégalo a Firebase
                        firebaseHelper.addProduct(product, new ProductFirebaseHelper.AddProductCallback() {
                            @Override
                            public void onSuccess() {
                                // Producto agregado exitosamente
                            }

                            @Override
                            public void onError(Exception e) {
                                // Manejar error
                                Toast.makeText(MainActivity.this, "Error al agregar producto a Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    // Manejar error
                    Toast.makeText(MainActivity.this, "Error al verificar existencia del producto en Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Paso 3: Cargar productos de Firebase y actualizarlos en el ListView
        loadProductsFromFirebase(true);
    }

    private void synchronizeAndRemoveData() {
        // Obtener todos los productos de Firebase
        firebaseHelper.getAllProducts(new ProductFirebaseHelper.GetProductsCallback() {
            @Override
            public void onProductsRetrieved(List<Product> productsFromFirebase) {
                // Obtener todos los productos de SQLite
                List<Product> productsFromSQLite = databaseHelper.getAllProducts();

                // Crear un conjunto de IDs de productos de SQLite
                Set<String> sqliteProductIds = new HashSet<>();
                for (Product sqliteProduct : productsFromSQLite) {
                    sqliteProductIds.add(sqliteProduct.getId());
                }

                // Lista de productos para eliminar de Firebase
                List<Product> productsToDeleteFromFirebase = new ArrayList<>();

                // Identificar productos de Firebase que no están en SQLite
                for (Product firebaseProduct : productsFromFirebase) {
                    if (!sqliteProductIds.contains(firebaseProduct.getId())) {
                        // Si el producto de Firebase no está en SQLite, añadirlo a la lista para eliminar
                        productsToDeleteFromFirebase.add(firebaseProduct);
                    }
                }

                // Eliminar los productos identificados de Firebase
                for (Product productToDelete : productsToDeleteFromFirebase) {
                    firebaseHelper.deleteProduct(productToDelete.getId(), new ProductFirebaseHelper.DeleteProductCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, "Producto eliminado de Firebase", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "Error al eliminar producto de Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Cargar productos actualizados desde Firebase
                loadProductsFromFirebase(true);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsFromFirebase(boolean hideButtons) {
        firebaseHelper.getAllProducts(new ProductFirebaseHelper.GetProductsCallback() {
            @Override
            public void onProductsRetrieved(List<Product> products) {
                productAdapter.clear();
                productAdapter.addAll(products);
                productAdapter.setHideButtons(hideButtons); // Pasa el parámetro para ocultar o mostrar botones
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProduct() {
        // Verifica si los campos de texto están vacíos
        if (verificarCamposVacios()) {
            // Si hay campos vacíos, no realiza la operación de agregar producto
            return;
        }

        // Si todos los campos tienen datos, continúa con la operación de agregar producto
        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());

        // Crear nuevo producto
        Product newProduct = new Product(name, price);

        // Agregar producto a la base de datos
        databaseHelper.addProduct(newProduct);

        // Actualizar la lista de productos
        loadProductsFromDatabase();

        // Limpiar campos de entrada
        clearInputFields();
        Toast.makeText(this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void saveProduct() {
        // Verifica si los campos de texto están vacíos
        if (verificarCamposVacios()) {
            // Si hay campos vacíos, no realiza la operación de guardar producto
            return;
        }

        // Si todos los campos tienen datos, continúa con la operación de guardar producto
        String id = editTextId.getText().toString();
        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());

        // Crear producto a actualizar
        Product product = new Product(id, name, price);

        // Actualizar producto en la base de datos
        databaseHelper.updateProduct(product);

        // Actualizar la lista de productos
        loadProductsFromDatabase();

        // Cambiar el botón de "Guardar" a "Agregar"
        buttonAdd.setText("Agregar");

        // Limpiar campos de entrada
        clearInputFields();
        Toast.makeText(this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private boolean verificarCamposVacios() {
        // Verifica si los campos de EditText están vacíos
        if (    editTextName.getText().toString().trim().isEmpty() ||
                editTextPrice.getText().toString().trim().isEmpty()) {
            // Si algún campo está vacío, muestra un mensaje de advertencia
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void clearInputFields() {
        editTextId.setText("");
        editTextName.setText("");
        editTextPrice.setText("");
    }

    public void editProduct(Product product) {
        // Cargar datos del producto seleccionado en los campos de entrada
        editTextId.setText(product.getId());
        editTextName.setText(product.getName());
        editTextPrice.setText(String.valueOf(product.getPrice()));

        // Cambiar el texto del botón a "Guardar"
        buttonAdd.setText("Guardar");
    }

    public void deleteProduct(Product product) {
        // Eliminar producto de la base de datos
        if (product.isDeleted()) {
            Toast.makeText(this, "Producto ya está eliminado", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.deleteProduct(product.getId());

            // Actualizar la lista de productos
            loadProductsFromDatabase();
            Toast.makeText(this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}
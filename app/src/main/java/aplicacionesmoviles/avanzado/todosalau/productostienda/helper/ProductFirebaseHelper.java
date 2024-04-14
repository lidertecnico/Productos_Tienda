package aplicacionesmoviles.avanzado.todosalau.productostienda.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import aplicacionesmoviles.avanzado.todosalau.productostienda.model.Product;

public class ProductFirebaseHelper {
    private DatabaseReference databaseReference;

    public ProductFirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
    }

    public interface AddProductCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface DeleteProductCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void addProduct(Product product, AddProductCallback callback) {
        if (product.getId() == null || product.getId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            product.setId(newId);
        }

        databaseReference.child(product.getId()).setValue(product)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void updateProduct(Product product) {
        // Verificar si el ID del producto es nulo o está vacío
        if (product == null || product.getId() == null || product.getId().isEmpty()) {
            System.out.println("El ID del producto es nulo o vacío. No se puede actualizar el producto.");
            return; // Detiene la ejecución si el ID es nulo o vacío
        }

        // Proceder a actualizar el producto en Firebase
        databaseReference.child(product.getId()).updateChildren(product.toMap());
    }

    public void deleteProduct(String id, DeleteProductCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("ID del producto es nulo o vacío."));
            return;
        }

        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Interfaz para la devolución de llamada de los productos
    public interface GetProductsCallback {
        void onProductsRetrieved(List<Product> products);
        void onError();
    }

    // Método para obtener todos los productos de Firebase
    public void getAllProducts(final GetProductsCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                callback.onProductsRetrieved(products);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError();
            }
        });

    }

    public void getProductById(String productId, GetProductByIdCallback callback) {
        // Obtiene una referencia al producto específico por ID
        DatabaseReference productReference = databaseReference.child(productId);

        // Añade un oyente para obtener el producto
        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Intenta obtener el producto
                Product product = dataSnapshot.getValue(Product.class);

                // Llama al callback con el producto obtenido
                callback.onProductRetrieved(product);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, llama al callback de error
                callback.onError(databaseError.toException());
            }
        });
    }

    // Interfaz para el callback de obtener un producto por ID
    public interface GetProductByIdCallback {
        void onProductRetrieved(@Nullable Product product);
        void onError(Exception e);
    }
    public void checkIfProductExists(String productId, ProductExistsCallback callback) {
        DatabaseReference productRef = databaseReference.child("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onProductExists(exists);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError();
            }
        });
    }

    public interface ProductExistsCallback {
        void onProductExists(boolean exists);
        void onError();
    }

}
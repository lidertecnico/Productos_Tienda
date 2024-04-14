package aplicacionesmoviles.avanzado.todosalau.productostienda.model;

import java.util.HashMap;
import java.util.Map;

public class Product {
    // Atributos de la clase Product
    private String id; // Identificador único del producto
    private String name; // Nombre del producto
    private double price; // Precio del producto
    private boolean deleted; // Indica si el producto ha sido eliminado

    // Constructor vacío necesario para ciertas integraciones como Firebase
    public Product() {
    }

    // Constructor con parámetros para inicializar un producto con un ID, nombre y precio
    public Product(String id, String name, double price) {
        this.id = id; // Asigna el ID proporcionado
        this.name = name; // Asigna el nombre proporcionado
        this.price = price; // Asigna el precio proporcionado
    }

    // Constructor sin ID, para inicializar un producto solo con nombre y precio
    public Product(String name, double price) {
        this.name = name; // Asigna el nombre proporcionado
        this.price = price; // Asigna el precio proporcionado
    }

    // Getters y setters para los atributos

    // Obtiene el ID del producto
    public String getId() {
        return id;
    }

    // Establece el ID del producto
    public void setId(String id) {
        this.id = id;
    }

    // Obtiene el nombre del producto
    public String getName() {
        return name;
    }

    // Establece el nombre del producto
    public void setName(String name) {
        this.name = name;
    }

    // Obtiene el precio del producto
    public double getPrice() {
        return price;
    }

    // Establece el precio del producto
    public void setPrice(double price) {
        this.price = price;
    }

    // Obtiene el estado de eliminación del producto
    public boolean isDeleted() {
        return deleted;
    }

    // Establece el estado de eliminación del producto
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Convierte las propiedades del producto en un mapa para almacenamiento o envío
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id); // Agrega el ID al mapa
        result.put("name", name); // Agrega el nombre al mapa
        result.put("price", price); // Agrega el precio al mapa
        return result; // Devuelve el mapa con las propiedades del producto
    }
}
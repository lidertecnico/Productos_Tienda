package aplicacionesmoviles.avanzado.todosalau.productostienda.model;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String id;
    private String name;
    private double price;
    private boolean deleted;

    public Product() {
        // Constructor vacío para Firebase
    }

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Método para obtener el estado de eliminado
    public boolean isDeleted() {
        return deleted;
    }

    // Método para establecer el estado de eliminado
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Método toMap para mapear las propiedades del producto
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("price", price);
        return result;
    }
}
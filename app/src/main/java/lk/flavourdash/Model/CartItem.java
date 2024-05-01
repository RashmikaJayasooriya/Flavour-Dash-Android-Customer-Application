package lk.flavourdash.Model;


import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Map;

public class CartItem {

    private String id;
    private String dishId;
    private Map<String, Double> portionPrices;
    private List<String> options;
    private int noOfItems;

    public CartItem() {
    }

    public CartItem(String dishId, Map<String, Double> portionPrices, List<String> options, int noOfItems) {
        this.dishId = dishId;
        this.portionPrices = portionPrices;
        this.options = options;
        this.noOfItems = noOfItems;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public Map<String, Double> getPortionPrices() {
        return portionPrices;
    }

    public void setPortionPrices(Map<String, Double> portionPrices) {
        this.portionPrices = portionPrices;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getNoOfItems() {
        return noOfItems;
    }

    public void setNoOfItems(int noOfItems) {
        this.noOfItems = noOfItems;
    }
    @Exclude
    public void updateQuantity(int additionalQuantity) {
        this.noOfItems += additionalQuantity;
    }
}

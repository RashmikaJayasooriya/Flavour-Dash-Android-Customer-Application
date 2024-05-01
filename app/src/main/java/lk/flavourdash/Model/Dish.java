package lk.flavourdash.Model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Dish implements Serializable {
    private String id;
    private String category;
    private String subCategory;
    private String name;
    private String description;
    private Map<String, Double> portionPrices;
    private Double rating;
    private Boolean availability;
    private List<String> options;
    private  List<String> images;

    public Dish() {
    }

    public Dish(String category, String subCategory, String name, String description, Map<String, Double> portionPrices, Double rating, Boolean availability, List<String> options, List<String> images) {
        this.category = category;
        this.subCategory = subCategory;
        this.name = name;
        this.description = description;
        this.portionPrices = portionPrices;
        this.rating = rating;
        this.availability = availability;
        this.options = options;
        this.images = images;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Double> getPortionPrices() {
        return portionPrices;
    }

    public void setPortionPrices(Map<String, Double> portionPrices) {
        this.portionPrices = portionPrices;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}


package lk.flavourdash.Model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private String description;
    private String image;
    private boolean isActive;

    public Category() {
    }

    public Category(String name, String description, String image, boolean isActive) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.isActive = isActive;
    }

    @Exclude
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

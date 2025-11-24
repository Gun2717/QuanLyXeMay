package iuh.fit.se.common;

import java.io.Serializable;
import java.sql.Timestamp;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private Timestamp createdAt;

    public Category() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

package com.ase.model;

import lombok.Data;

@Data
public class Person {
    private String name;
    private String parent;
    private String tooltip;

    public Person(String name, String parent, String tooltip) {
        this.name = name;
        this.parent = parent;
        this.tooltip = tooltip;
    }

    // Add getters and setters
}


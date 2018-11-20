package com.maxfill.services.worktime;

import java.util.Objects;

/**
 *
 * @author maksim
 */
public class DayType {
    private final Integer id;
    private final String name;
    private final String iconName;

    public DayType(Integer id, String name, String iconName) {        
        this.name = name;
        this.iconName = iconName;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }

    public Integer getId() {
        return id;
    }
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DayType other = (DayType) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DayType{" + "name=" + name + '}';
    }
        
}

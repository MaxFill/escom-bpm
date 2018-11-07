/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.beans.users.settings;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author maksim
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class DashBoardSettings {
    @XmlElement(name = "Widget")
    private String widget;
    
    @XmlElement(name = "ItemIndex")
    private Integer itemIndex;

    @XmlElement(name = "ColIndex")
    private Integer colIndex;
    
    public DashBoardSettings() {
    }
    
    public DashBoardSettings(String widget, Integer itemIndex, Integer colIndex) {
        this.widget = widget;
        this.itemIndex = itemIndex;
        this.colIndex = colIndex;
    }

    public String getWidget() {
        return widget;
    }
    public void setWidget(String widget) {
        this.widget = widget;
    }

    public Integer getItemIndex() {
        return itemIndex;
    }
    public void setItemIndex(Integer itemIndex) {
        this.itemIndex = itemIndex;
    }

    public Integer getColIndex() {
        return colIndex;
    }
    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }
    
    @Override
    public String toString() {
        return "DashBoardSettings{" + "widget=" + widget + ", itemIndex=" + itemIndex + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.widget);
        hash = 43 * hash + Objects.hashCode(this.itemIndex);
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
        final DashBoardSettings other = (DashBoardSettings) obj;
        if (!Objects.equals(this.widget, other.widget)) {
            return false;
        }
        if (!Objects.equals(this.itemIndex, other.itemIndex)) {
            return false;
        }
        return true;
    }
    
    
}

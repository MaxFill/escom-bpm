package com.maxfill.escom.beans.core;

import com.maxfill.model.basedict.BaseDict;
import java.util.Map;

/**
 * Интерфейс view бина
 */
public interface BaseView   {
    void doBeforeOpenCard(Map<String, String> params);
    BaseDict getSourceItem();
    
    default boolean isReadOnly(){
        return false;
    }
}

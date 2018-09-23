package com.maxfill.escom.beans.core;

import com.maxfill.model.BaseDict;
import java.util.Map;

/**
 * Интерфейс view бина
 */
public interface BaseView   {
    void doBeforeOpenCard(Map<String, String> params);
    BaseDict getSourceItem();
}

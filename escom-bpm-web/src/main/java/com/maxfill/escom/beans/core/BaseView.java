package com.maxfill.escom.beans.core;

import com.maxfill.escom.beans.SessionBean;
import com.maxfill.model.BaseDict;
import java.util.Map;

/**
 * Интерфейс view бина
 */
public interface BaseView {
    SessionBean getSessionBean();
    void doBeforeOpenCard(Map<String, String> params);
    BaseDict getSourceItem();
}

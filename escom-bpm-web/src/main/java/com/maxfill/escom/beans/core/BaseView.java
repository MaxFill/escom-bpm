package com.maxfill.escom.beans.core;

import com.maxfill.escom.beans.SessionBean;
import java.util.Map;

/**
 * Интерфейс view бина
 */
public interface BaseView {
    SessionBean getSessionBean();
    String getBeanName();
    void doBeforeOpenCard(Map<String, String> params);
}

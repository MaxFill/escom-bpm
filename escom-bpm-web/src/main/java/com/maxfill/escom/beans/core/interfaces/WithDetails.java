package com.maxfill.escom.beans.core.interfaces;

import com.maxfill.model.basedict.BaseDict;
import java.util.List;
import org.primefaces.event.SelectEvent;

/**
 * Интерфейс для бинов карточек, содержащих на форме таблицу детальных объектов (User -> Assistant)
 * @author maksim
 * @param <T>
 * @param <B>
 */
public interface WithDetails<T extends BaseDict> {
    List<T> getDetails();
    List<T> getCheckedDetails();
    void setCheckedDetails(List<T> checkedDetails);
    void onDeleteCheckedDetails();
    void onCreateDetail();
    void afterCloseDetailItem(SelectEvent event);
    void onDeleteDetail(T item);
    void onOpenDetail(T item);
    T getSelectedDetail();
    void setSelectedDetail(T selectedDetail);
}

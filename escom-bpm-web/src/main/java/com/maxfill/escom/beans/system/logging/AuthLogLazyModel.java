package com.maxfill.escom.beans.system.logging;

import com.maxfill.facade.AuthLogFacade;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.utils.DateUtils;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.*;

public class AuthLogLazyModel extends LazyDataModel<Authlog> {

    private List<Authlog> datasource;
    private final AuthLogBean bean;

    public AuthLogLazyModel(List <Authlog> datasource, AuthLogBean bean) {
        this.datasource = datasource;
        this.bean = bean;
    }

    @Override
    public Authlog getRowData(String rowKey) {
        for(Authlog item : datasource) {
            if(item.getId().equals(rowKey))
                return item;
        }
        return null;
    }

    @Override
    public Object getRowKey(Authlog item) {
        return item.getId();
    }

    @Override
    public List<Authlog> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        datasource = bean.loadEvents(first, pageSize, sortField, sortOrder, filters);

        int dataSize = bean.countEvents(filters);
        this.setRowCount(dataSize);

        return datasource;
    }
}

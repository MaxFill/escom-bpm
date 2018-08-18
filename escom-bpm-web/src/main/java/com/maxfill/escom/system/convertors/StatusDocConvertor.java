package com.maxfill.escom.system.convertors;

import com.maxfill.model.statuses.StatusesDoc;
import javax.faces.convert.FacesConverter;

@FacesConverter("statusesDocConvertor")
public class StatusDocConvertor extends BaseBeanConvertor<StatusesDoc>{
    @Override
    protected String getBeanName() {
        return "statusesDocBean";
    }
}

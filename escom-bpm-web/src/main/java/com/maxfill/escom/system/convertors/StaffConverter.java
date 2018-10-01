package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.staff.Staff;
import javax.faces.convert.FacesConverter;

@FacesConverter("staffConvertor")
public class StaffConverter extends BaseBeanConvertor<Staff>{
    @Override
    protected String getBeanName() {
        return "staffBean";
    }
}

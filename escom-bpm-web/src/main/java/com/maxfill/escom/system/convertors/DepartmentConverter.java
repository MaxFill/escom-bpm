package com.maxfill.escom.system.convertors;

import com.maxfill.model.departments.Department;
import javax.faces.convert.FacesConverter;

@FacesConverter("departmentConvertor")
public class DepartmentConverter extends BaseBeanConvertor<Department>{
    @Override
    protected String getBeanName() {
        return "departmentBean";
    }
}

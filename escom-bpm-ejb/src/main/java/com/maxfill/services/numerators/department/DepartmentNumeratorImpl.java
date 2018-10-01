package com.maxfill.services.numerators.department;

import com.maxfill.model.basedict.department.DepartmentFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.services.numerators.NumeratorBase;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/* Нумератор для Подразделений */
@Stateless
public class DepartmentNumeratorImpl extends NumeratorBase implements DepartmentNumeratorService{                      
    
    @EJB
    private DepartmentFacade departmentFacade;
    
    @Override
    protected String doGetCounterName(BaseDict item) {
        Company company = departmentFacade.findCompany((Department) item);        
        return "department_" + company.getId();
    }

}
package com.maxfill.services.numerators.department;

import com.maxfill.facade.treelike.DepartmentFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.services.numerators.NumeratorBase;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/* Нумератор для контрагентов */
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
package com.maxfill.model.basedict.department.numerator;

import com.maxfill.model.basedict.department.DepartmentFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.services.numerators.NumeratorBase;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/* Нумератор для Подразделений */
@Stateless
public class DepartmentNumeratorImpl extends NumeratorBase implements DepartmentNumerator{                      
    
    @EJB
    private DepartmentFacade facade;
    
    @Override
    protected String doGetCounterName(BaseDict item, NumeratorPattern numPattern) {
        Company company = facade.findCompany((Department) item);        
        return "department_" + company.getId();
    }

    @Override
    protected NumeratorPattern getNumeratorPattern(BaseDict item) {
        return facade.getMetadatesObj().getNumPattern();
    }

}
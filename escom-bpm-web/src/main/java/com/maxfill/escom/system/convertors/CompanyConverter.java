package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.company.Company;
import javax.faces.convert.FacesConverter;

@FacesConverter("companyConvertor")
public class CompanyConverter extends BaseBeanConvertor<Company>{
    @Override
    protected String getBeanName() {
        return "companyBean";
    }
}

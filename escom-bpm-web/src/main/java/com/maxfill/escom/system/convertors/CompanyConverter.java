package com.maxfill.escom.system.convertors;

import com.maxfill.model.companies.Company;
import javax.faces.convert.FacesConverter;

@FacesConverter("companyConvertor")
public class CompanyConverter extends BaseBeanConvertor<Company>{
    @Override
    protected String getBeanName() {
        return "companyBean";
    }
}

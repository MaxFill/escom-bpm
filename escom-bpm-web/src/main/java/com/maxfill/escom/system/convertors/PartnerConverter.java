package com.maxfill.escom.system.convertors;


import com.maxfill.model.basedict.partner.Partner;
import javax.faces.convert.FacesConverter;

@FacesConverter("partnersConvertor")
public class PartnerConverter extends BaseBeanConvertor<Partner>{
    @Override
    protected String getBeanName() {
        return "partnersBean";
    }
}

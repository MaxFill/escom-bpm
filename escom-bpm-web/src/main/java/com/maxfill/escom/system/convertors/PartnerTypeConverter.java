package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.partnerTypes.PartnerTypes;
import javax.faces.convert.FacesConverter;

@FacesConverter("partnerTypesConvertor")
public class PartnerTypeConverter extends BaseBeanConvertor<PartnerTypes>{
    @Override
    protected String getBeanName() {
        return "partnerTypesBean";
    }
}

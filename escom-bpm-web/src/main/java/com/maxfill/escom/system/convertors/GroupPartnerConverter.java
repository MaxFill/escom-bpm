package com.maxfill.escom.system.convertors;

import com.maxfill.model.partners.groups.PartnerGroups;
import javax.faces.convert.FacesConverter;

@FacesConverter("groupsPartnerConvertor")
public class GroupPartnerConverter extends BaseBeanConvertor<PartnerGroups>{
    @Override
    protected String getBeanName() {
        return "partnersGroupsBean";
    }
}

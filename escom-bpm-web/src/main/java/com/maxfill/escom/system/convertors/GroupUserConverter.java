package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.userGroups.UserGroups;
import javax.faces.convert.FacesConverter;

@FacesConverter("groupsUserConvertor")
public class GroupUserConverter extends BaseBeanConvertor<UserGroups>{
    @Override
    protected String getBeanName() {
        return "userGroupsBean";
    }
}

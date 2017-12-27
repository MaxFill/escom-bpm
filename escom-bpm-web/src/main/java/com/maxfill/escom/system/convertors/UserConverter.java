package com.maxfill.escom.system.convertors;

import com.maxfill.model.users.User;
import javax.faces.convert.FacesConverter;

@FacesConverter("usersConvertor")
public class UserConverter extends BaseBeanConvertor<User>{
    @Override
    protected String getBeanName() {
        return "userBean";
    }
}

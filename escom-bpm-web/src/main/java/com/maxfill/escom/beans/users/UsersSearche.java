package com.maxfill.escom.beans.users;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Расширение для поиска в пользователях
 */
public class UsersSearche extends SearcheModel{
    private static final long serialVersionUID = -869171711508429153L;

    private String searcheLogin;

    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        if (StringUtils.isNotBlank(searcheLogin)) {
            addParams.put("login", searcheLogin);
        }
    }

    public String getSearcheLogin() {
        return searcheLogin;
    }
    public void setSearcheLogin(String searcheLogin) {
        this.searcheLogin = searcheLogin;
    }
}
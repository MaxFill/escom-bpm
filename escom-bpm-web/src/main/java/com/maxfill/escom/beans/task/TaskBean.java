package com.maxfill.escom.beans.task;

/* Сервисный бин "Поручения" */

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.users.User;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@SessionScoped
public class TaskBean extends BaseDetailsBean<Task, User>{
    private static final long serialVersionUID = -3502688297345485315L;

    @Inject
    private UserBean userBean;

    @EJB
    private TaskFacade taskFacade;

    @Override
    public List<User> getGroups(Task item) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return userBean;
    }

    @Override
    public Class <User> getOwnerClass() {
        return User.class;
    }

    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getFacade() {
        return taskFacade;
    }
}

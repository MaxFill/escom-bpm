package com.maxfill.escom.beans.staffs;

import com.maxfill.facade.StaffFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.PostFacade;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.users.User;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.EJB;

/* Карточка Штатные единицы */
@Named
@ViewScoped
public class StaffCardBean extends BaseCardBeanGroups<Staff, Department> {
    private static final long serialVersionUID = -977912654006193660L;
    
    @EJB
    private PostFacade postFacade;
    @EJB
    private StaffFacade itemFacade;
    
    private List<Post> posts;
    
    @Override
    public StaffFacade getItemFacade() {
        return itemFacade;
    }
         
    /* Обработка события на выбор сотрудника   */
    public void onEmployeeSelected(SelectEvent event){
        List<User> items = (List<User>) event.getObject();
        if (items.isEmpty()) {return;}
        User item = items.get(0);
        onItemChange();
        getEditedItem().setEmployee(item);
    }
    public void onEmployeeSelected(ValueChangeEvent event){
        User user = (User) event.getNewValue();
        getEditedItem().setEmployee(user);
    }
    
    /* Событие изменение на форме поля выбора должности  */
    public void onPostSelected(SelectEvent event){
        List<Post> items = (List<Post>) event.getObject();
        if (items.isEmpty()) {return;}
        Post item = items.get(0);;
        if (item != null) {
            getEditedItem().setPost(item);
            onItemChange();
            if (!posts.contains(item)) {
                posts.add(item);
            }
        }
    }
    public void onPostSelected(ValueChangeEvent event){
        Post post = (Post) event.getNewValue();
        getEditedItem().setPost(post);
    }
        
    /* Формирование наименования шт. единицы */
    public void makeName(){
        Staff staff = getEditedItem();
        Post post = staff.getPost();
        StringBuilder staffName = new StringBuilder();
        if (post != null && StringUtils.isNoneBlank(post.getName())){
            staffName.append(post.getName());
            User user = staff.getEmployee();
            if (user != null && StringUtils.isNoneBlank(user.getShortFIO())){
                staffName.append(" ").append(user.getShortFIO());
            }
        } else {
            staffName.append(EscomBeanUtils.getBandleLabel("Vacant"));
        }
        getEditedItem().setName(staffName.toString());
    }
    
    public List<Post> getPosts() {
        if (posts == null){
            posts = postFacade.findAll().stream()
                .filter(item -> preloadCheckRightView(item))
                .collect(Collectors.toList());
        }
        return posts;
    }    

    @Override
    public List<Department> getGroups(Staff item) {
        List<Department> groups = new ArrayList<>();
        if (item.getOwner() != null){
            Department department = item.getOwner();
            groups.add(department);
        }
        return groups;
    }

    @Override
    protected void afterCreateItem(Staff item) {        
    }

    @Override
    public Class<Staff> getItemClass() {
        return Staff.class;
    }
}

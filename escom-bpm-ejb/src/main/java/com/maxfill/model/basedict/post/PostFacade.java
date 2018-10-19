package com.maxfill.model.basedict.post;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.staff.Staff_;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import com.maxfill.model.basedict.user.User;
import org.apache.commons.lang.StringUtils;

/**
 * Фасад для сущности "Должности"
 */
@Stateless
public class PostFacade extends BaseDictFacade<Post, Post, PostLog, PostStates>{    

    public PostFacade() {
        super(Post.class, PostLog.class, PostStates.class);
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_POST;
    }
    
    /**
     * Ищет должность по имени и если не найдена то создаёт новую должность 
     * @param postName Наименование искомой должности
     * @param currentUser
     * @return Должность
     */
    public Post onGetPostByName(String postName, User currentUser){
        if (StringUtils.isBlank(postName)){ 
            return null;
        }
        for (Post post : findAll(currentUser)){
            if (Objects.equals(post.getName(), postName)){
                return post;
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", postName);
        Post post = createItem(userFacade.getAdmin(), null, null, params);
        create(post);
        return post;
    }

    /**
     * Замена должности на другую в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(Post oldItem, Post newItem) {
        int count = replacePostInStaffs(oldItem, newItem);
        return count;
    }

    /**
     * Замена должности в штатных единицах
     * @param oldItem
     * @param newItem
     * @return 
     */
    private int replacePostInStaffs(Post oldItem, Post newItem){
        CriteriaBuilder builder = em.getCriteriaBuilder(); 
        CriteriaUpdate<Staff> update = builder.createCriteriaUpdate(Staff.class);    
        Root root = update.from(Staff.class);  
        update.set(Staff_.post, newItem);
        Predicate predicate = builder.equal(root.get(Staff_.post), oldItem);
        update.where(predicate);
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }
    
}

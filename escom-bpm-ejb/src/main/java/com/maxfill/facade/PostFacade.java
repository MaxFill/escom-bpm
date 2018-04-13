package com.maxfill.facade;

import com.maxfill.model.posts.Post;
import com.maxfill.model.posts.PostLog;
import com.maxfill.model.staffs.Staff;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.posts.PostStates;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.maxfill.model.staffs.Staff_;
import org.apache.commons.lang.StringUtils;

/**
 * Фасад для сущности "Должности"
 */
@Stateless
public class PostFacade extends BaseDictFacade<Post, Post, PostLog, PostStates> {  
    
    @EJB
    private UserFacade userFacade;

    public PostFacade() {
        super(Post.class, PostLog.class, PostStates.class);
    }

    @Override
    public Class<Post> getItemClass() {
        return Post.class;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.POST.toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_POST;
    }
    
    /**
     * Ищет должность по имени и если не найдена то создаёт новую должность 
     * @param postName Наименование искомой должности
     * @return Должность
     */
    public Post onGetPostByName(String postName){
        if (StringUtils.isBlank(postName)){ 
            return null;
        }
        for (Post post : findAll()){
            if (Objects.equals(post.getName(), postName)){
                return post;
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", postName);
        Post post = createItem(userFacade.getAdmin(), null, params);
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
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Staff> update = builder.createCriteriaUpdate(Staff.class);    
        Root root = update.from(Staff.class);  
        update.set(Staff_.post, newItem);
        Predicate predicate = builder.equal(root.get(Staff_.post), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
}

package com.maxfill.facade;

import com.maxfill.model.posts.Post;
import com.maxfill.model.posts.PostLog;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.facade.UserFacade;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.users.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Maxim
 */
@Stateless
public class PostFacade extends BaseDictFacade<Post, Post, PostLog> {
    protected static final Logger LOG = Logger.getLogger(PostFacade.class.getName());    
    
    @EJB
    private UserFacade userFacade;

    public PostFacade() {
        super(Post.class, PostLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.POST.toLowerCase();
    }

    @Override
    public void pasteItem(Post pasteItem, BaseDict target, Set<String> errors){        
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {      
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
        Post post = createItem(null, userFacade.getAdmin());
        post.setName(postName);
        create(post);
        LOG.log(Level.INFO, "Create post = {0}", postName);
        return post;
    }

    /**
     * Замена должности в штатных единицах
     * @param oldItem
     * @param newItem
     * @return 
     */
    @Override
    public Map<String, Integer> replaceItem(Post oldItem, Post newItem) {
        Map<String, Integer> rezultMap = new HashMap<>();
        rezultMap.put("Staffs", replacePostInStaffs(oldItem, newItem));
        return rezultMap;
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
        update.set("post", newItem);
        Predicate predicate = builder.equal(root.get("post"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
}

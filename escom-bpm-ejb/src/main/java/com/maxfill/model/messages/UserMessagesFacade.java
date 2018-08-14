package com.maxfill.model.messages;

import com.maxfill.Configuration;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.facade.BaseLazyLoadFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.messages.UserMessages;
import com.maxfill.model.messages.UserMessages_;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

@Stateless
public class UserMessagesFacade extends BaseLazyLoadFacade<UserMessages>{

    @EJB
    private Configuration conf;
    
    @EJB
    private MailBoxFacade mailBoxFacade;    
    
    public UserMessagesFacade() {
        super(UserMessages.class);
    }

    public List<UserMessages> findUnReadMessageByUser(User addressee){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserMessages.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserMessages> cq = builder.createQuery(UserMessages.class);
        Root<UserMessages> root = cq.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), addressee);
        Predicate crit2 = builder.isNull(root.get(UserMessages_.dateReading));
        cq.select(root).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(root.get(UserMessages_.dateSent)));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    public List<UserMessages> findMessageByUser(User addressee){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserMessages.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserMessages> cq = builder.createQuery(UserMessages.class);
        Root<UserMessages> c = cq.from(UserMessages.class);
        Predicate crit1 = builder.equal(c.get(UserMessages_.addressee), addressee);
        cq.select(c).where(builder.and(crit1));
        cq.orderBy(builder.asc(c.get(UserMessages_.dateSent)));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Создание системного сообщения
     * @param addressee
     * @param subject
     * @param content
     * @param tuple
     */
    public void createSystemMessage(User addressee, String subject, String content, Tuple tuple){        
        String senderName = ItemUtils.getBandleLabel("System", conf.getServerLocale());
        createMessage(addressee, senderName, conf.getDefaultSenderEmail(), subject, content, tuple);
    }

    /**
     * Создание нового сообщения
     * @param addressee
     * @param senderName
     * @param senderEmail
     * @param subject
     * @param content
     * @param tuple
     */
    public void createMessage(User addressee, String senderName, String senderEmail, String subject, String content, Tuple tuple){        
        UserMessages message = new UserMessages();
        message.setName(subject);
        message.setAddressee(addressee);
        message.setDateSent(new Date());        
        if (tuple != null){
            message.setDocument((Doc)tuple.a);
            message.setTask((Task)tuple.b);
        }
        message.setSender(senderName);
        message.setImportance(1);
        create(message);
        if (addressee.isDuplicateMessagesEmail()){
            String emailAdress = addressee.getEmail();
            if (StringUtils.isNotBlank(emailAdress)){
                mailBoxFacade.createMailBox(subject, emailAdress, senderEmail, content);
            }
        }
    }
    
    public Integer getCountUnReadMessage(User addressee){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<UserMessages> root = cq.from(UserMessages.class);        
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), addressee);
        Predicate crit2 = builder.isNull(root.get(UserMessages_.dateReading));
        cq.select(builder.count(root));
        cq.where(builder.and(crit1, crit2));        
        Query query = getEntityManager().createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }

    public int removeMessageByUser(User user){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<UserMessages> cd = builder.createCriteriaDelete(UserMessages.class);
        Root root = cd.from(UserMessages.class);
        Predicate crit1 = builder.equal(root.get(UserMessages_.addressee), user);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
}
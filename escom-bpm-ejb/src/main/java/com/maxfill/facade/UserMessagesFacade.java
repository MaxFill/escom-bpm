package com.maxfill.facade;

import com.maxfill.Configuration;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.messages.UserMessages;
import com.maxfill.model.messages.UserMessages_;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

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
    
    public void createSystemMessage(User addressee, String subject, String content, Doc doc){        
        String senderName = ItemUtils.getBandleLabel("System", conf.getServerLocale());
        createMessage(addressee, senderName, conf.getDefaultSenderEmail(), subject, content, doc);
    }

    /**
     * Создание нового сообщения
     * @param addressee
     * @param senderName
     * @param senderEmail
     * @param subject
     * @param content
     * @param doc
     */
    public void createMessage(User addressee, String senderName, String senderEmail, String subject, String content, Doc doc){        
        UserMessages message = new UserMessages();
        message.setName(subject);
        message.setAddressee(addressee);
        message.setDateSent(new Date());
        message.setDocument(doc);
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

    @Override
    protected SingularAttribute<UserMessages, Date> getFieldDateCrit() {
        return UserMessages_.dateSent;
    }
}

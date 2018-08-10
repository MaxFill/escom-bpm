package com.maxfill.model.process;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.process.types.ProcessTypesFacade;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Map;
import java.util.Set;

/**
 * Фасад для сущности "Процессы"
 */
@Stateless
public class ProcessFacade extends BaseDictWithRolesFacade<Process, ProcessType, ProcessLog, ProcessStates>{

    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private DocFacade docFacade;
        
    public ProcessFacade() {
        super(Process.class, ProcessLog.class, ProcessStates.class);
    }

    @Override
    public Class <Process> getItemClass() {
        return Process.class;
    }

    @Override
    public int replaceItem(Process oldItem, Process newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 20;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PROCESS.toLowerCase();
    }

    @Override
    public void setSpecAtrForNewItem(Process process, Map<String, Object> params) {       
        if (params.containsKey("documents")){
            List<Doc> docs = (List<Doc>)params.get("documents");
            if (!docs.isEmpty()){
                process.setDocs(docs);
            }
            makeProcName(process);            
        }
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(process, numeratorPattern, null, new Date());
        process.setRegNumber(number);
    }

    /**
     * Формирование дефолтного наименования для процесса
     * @param process 
     */
    public void makeProcName(Process process){
        StringBuilder sb = new StringBuilder();
        if (process.getOwner() != null){
            sb.append(process.getOwner().getName());
        }
        if (process.getDocs() != null && !process.getDocs().isEmpty()){
            Iterator iterator = process.getDocs().iterator(); 
            Doc doc = (Doc)iterator.next();            
            if (doc != null){
                sb.append(" <").append(doc.getFullName()).append(">");
            }
        }
        process.setName(sb.toString());
    }
    
    /**
     * Получение прав доступа к процессу
     * @param item
     * @param user
     * @return 
     */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user);
        }
        if (item.getOwner() != null) {
            Rights childRight = processTypesFacade.getRightForChild(item.getOwner());
            if (childRight != null) {
                return childRight;
            }
        }
        return getDefaultRights(item);
    }
    
    @Override
    public Tuple findDublicateExcludeItem(Process item){       
        return new Tuple(false, null);        
    }
        
    /**
     * Создание процесса c созданием и прикреплением к нему документов созданных из файлов
     * @param owner
     * @param author
     * @param attaches
     * @param errors
     * @return 
     */
    public Process createProcFromFile(ProcessType owner, User author, List<Attaches> attaches, Set<String> errors){
        if (owner == null){
            errors.add("DoNotSpecifyTypeProcess");
            return null;
        }
        Folder folder = author.getInbox();
        if (folder == null){
            errors.add("NoDefaultUserFolderSpecified");
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        List<Doc> docs = new ArrayList<>();
        attaches.forEach(attach->{
            Doc doc = docFacade.createDocInUserFolder(attach.getName(), author, folder, attach);
            docs.add(doc);
        });        
        params.put("documents", docs);
        Process process = createItem(author, owner, params);
        makeRightItem(process, author);
        return process;
    }
    
    
}
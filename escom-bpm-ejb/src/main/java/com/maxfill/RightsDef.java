package com.maxfill;

import com.maxfill.model.core.metadates.MetadatesFacade;
import com.maxfill.model.core.rights.RightFacade;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.core.rights.Rights;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Сервис формирования дефолтных прав объектов
 */
@Singleton
@LocalBean
public class RightsDef{
    private static final Logger LOGGER = Logger.getLogger(RightsDef.class.getName());
    private final ConcurrentHashMap<String, Rights> defRights = new ConcurrentHashMap<>();

    @EJB
    private MetadatesFacade metadatesFacade;
    @EJB
    private RightFacade rightFacade;

    /**
     *  Загрузка дефолтных прав доступа всех объектов
     */
    @PostConstruct
    private void init() {
        List<Metadates> metadates = metadatesFacade.findAll();
        metadates.stream().forEach(metadatesObj -> {
            Rights rights = rightFacade.getObjectDefaultRights(metadatesObj);
            defRights.put(metadatesObj.getObjectName(), rights);
        });
    }

    /**
     * Возвращает дефолтные права доступа
     * metadateId - имя класса сущности
     * @param metadateId
     * @return 
     */
    public Rights getDefaultRights(String metadateId){
        Rights rights = defRights.get(metadateId);
        if (rights == null){
            throw new NullPointerException("EscomErr: for object " + metadateId + " no have default rights!");
        }
        return rights;
    }

    /**
     * Перезагружает дефолтные права объекта метаданных
     * @param metadatesObj
     */
    public void reloadDefaultRight(Metadates metadatesObj){
        Rights freshRights = rightFacade.getObjectDefaultRights(metadatesObj);
        defRights.replace(metadatesObj.getObjectName(), freshRights);
    }
}
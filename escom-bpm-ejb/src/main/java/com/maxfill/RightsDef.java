package com.maxfill;

import com.maxfill.facade.MetadatesFacade;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Rights;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Логика получения дефолтных права объектов
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
     */
    public Rights getDefaultRights(String metadateId){
        Rights rights = defRights.get(metadateId);
        if (rights == null){
            throw new NullPointerException("EscomErr: for object " + metadateId + " no have default rights!");
        }
        return rights;
    }
}

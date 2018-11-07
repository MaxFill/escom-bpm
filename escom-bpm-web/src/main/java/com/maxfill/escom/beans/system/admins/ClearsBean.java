package com.maxfill.escom.beans.system.admins;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.attaches.AttacheFacade;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.ejb.EJB;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.PrimeFaces;

/* Контролер формы очистки базы */
@Named
@ViewScoped
public class ClearsBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -7160350484665477991L;        
    private BigDecimal progress = new BigDecimal(0);
    private BigDecimal totalFileCount;
    
    @EJB
    private AttacheFacade attacheFacade;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){         
    }
    
    public void onCleanUpFileStorage(){
        try {
            progress = new BigDecimal(1);
            if (totalFileCount == null){
                initFileCount();
            }
            progress = new BigDecimal(5);
            AtomicInteger countDel = new AtomicInteger(0);
            if (totalFileCount.intValue() > 0){
                BigDecimal s = new BigDecimal(90);
                BigDecimal incr = s.divide(totalFileCount, 8, BigDecimal.ROUND_HALF_UP);
                //LOGGER.log(Level.INFO, "incr=" + incr.toString() , "");
                try (Stream<Path> paths = Files.walk(Paths.get(conf.getUploadPath()))) {
                    paths.filter(Files::isRegularFile)
                            .forEach(p -> {
                                //try {
                                String name = p.toFile().getName();
                                String fileNameWithOutExt = FilenameUtils.removeExtension(name);
                                List ids = attacheFacade.findAttachesByGUID(fileNameWithOutExt);
                                if (ids.isEmpty()){
                                    countDel.incrementAndGet();
                                    p.toFile().delete();
                                }
                                progress = progress.add(incr);
                                //LOGGER.log(Level.INFO, "progress=" + getProgress().toString() , "");
                                /*
                                TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException ex) {
                                Logger.getLogger(ClearsBean.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                */
                            });
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            progress = new BigDecimal(95);
            initFileCount(Paths.get(conf.getUploadPath()));
            progress = new BigDecimal(100);
            TimeUnit.SECONDS.sleep(1);
            PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
            PrimeFaces.current().executeScript("PF('progressDlg').hide();");
            progress = new BigDecimal(0);
            MsgUtils.succesFormatMsg("CountFilesWereDeleted", new Object[]{countDel.intValue(), totalFileCount.intValue()});
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void onComplete() {        
    }
    
    public void initFileCount(){
        initFileCount(Paths.get(conf.getUploadPath()));
    }
    private void initFileCount(Path dir) { 
        try {
            totalFileCount = new BigDecimal(Files.walk(dir)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory())
                    .count());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }        
  }
        
    /* GETS & SETS */

    public Integer getProgress() {
        return progress.intValue();
    }
    
    public String getTotalFileCount() { 
        if (totalFileCount == null) return "?";
        return totalFileCount.toString();
    }
        
    @Override
    public String getFormName() {
        return DictFrmName.FRM_ADMIN_CLEANS;
    }        

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Administation");
    }
}

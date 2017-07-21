package com.maxfill.escom.system.scaner;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.services.files.FileService;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

@Named
@ViewScoped
public class ScanBean extends BaseDialogBean {
    private static final long serialVersionUID = 2945123427931425280L;
    private static final int JNDI_MAX_FILE_SIZE_DEFAULT = 10 * 1024 * 1024;
    private static final Logger LOGGER = Logger.getLogger(ScanBean.class.getName());

    private ScanViewModel viewModel;
    private final DWTwainHelper dwTwainHelper = new DWTwainHelper(JNDI_MAX_FILE_SIZE_DEFAULT);
    
    @EJB
    private FileService fileService;
    
    public String onSaveAndClose(){
        byte[] data = viewModel.getData();
        Attaches attache = new Attaches();

        int length = data.length;

        attache.setName(viewModel.getFullFileName());
        attache.setExtension(viewModel.getFileExtensionValue());
        attache.setType("image/jpeg");
        attache.setSize(length);
        attache.setAuthor(sessionBean.getCurrentUser());
        attache.setDateCreate(new Date());
        fileService.uploadScan(attache, data);
        return super.onFinalCloseCard(attache);
    }
    
    @Override
    public void onOpenCard(){        
    }
    
    protected Logger getLogger() {
        return LOGGER;
    }

    public ScanViewModel getModel() {
        return viewModel;
    }

    public DWTwainHelper getScanHelper() {
        return dwTwainHelper;
    }

    @Override
    protected void initBean(){
        viewModel = new ScanViewModel(dwTwainHelper.getScanFileTypesLabels());
    }

    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }
    
    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_SCANING;
    }
}
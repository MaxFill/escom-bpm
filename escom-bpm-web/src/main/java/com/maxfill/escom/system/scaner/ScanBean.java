package com.maxfill.escom.system.scaner;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.services.files.FileService;
import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ScanBean extends BaseDialogBean {
    private static final long serialVersionUID = 2945123427931425280L;

    private ScanViewModel viewModel;
    
    @EJB
    private FileService fileService;
    
    public String onSaveAndClose(){
        byte[] data = viewModel.getData();
        Attaches attache = new Attaches();

        int length = data.length;

        attache.setName(viewModel.getFullFileName());
        attache.setExtension(viewModel.getFileExtensionValue().toLowerCase());
        attache.setType("image/jpeg");
        attache.setSize(Integer.toUnsignedLong(length));
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

    @Override
    protected void initBean(){
        viewModel = new ScanViewModel();
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
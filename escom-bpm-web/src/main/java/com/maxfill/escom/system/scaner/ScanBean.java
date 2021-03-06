package com.maxfill.escom.system.scaner;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.services.files.FileService;
import java.util.Date;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ScanBean extends BaseViewBean{
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
        //fileService.uploadScan(attache, data);
        return super.onCloseCard();
    }

    public ScanViewModel getModel() {
        return viewModel;
    }

    @Override
    protected void initBean(){
        viewModel = new ScanViewModel();
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_SCANING;
    }
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Scan");
    }
}
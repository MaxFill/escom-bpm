package com.maxfill.escom.system.scaner;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

@Named
@ViewScoped
public class ScanBean implements Serializable {
    private static final long serialVersionUID = 2945123427931425280L;
    private static final int JNDI_MAX_FILE_SIZE_DEFAULT = 10 * 1024 * 1024;
    private static final Logger LOGGER = Logger.getLogger(ScanBean.class.getName());

    private ScanViewModel viewModel;
    private final DWTwainHelper dwTwainHelper = new DWTwainHelper(JNDI_MAX_FILE_SIZE_DEFAULT);

    @PostConstruct
    protected void onInit() {
        viewModel = new ScanViewModel(dwTwainHelper.getScanFileTypesLabels());
    }

    /**
     * Сохранение изображений и закрытие окна сканирования
     * @return 
     */
    public String onSaveAndClose(){
        //TODO сохранение!
        return onClose();
    }
    
    public String onClose(){
        RequestContext.getCurrentInstance().closeDialog(null);
        return "/view/index?faces-redirect=true";    
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
}
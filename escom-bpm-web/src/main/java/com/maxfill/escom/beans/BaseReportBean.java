package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.services.print.PrintService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.PieChartModel;

public abstract class BaseReportBean extends BaseDialogBean{
    private static final long serialVersionUID = -6471738532714818611L;
    
    @EJB
    protected PrintService printService;
            
    private Date dateStart;
    private Date dateEnd;
    
    protected PieChartModel pieModel;
    protected TreeNode[] selectedNodes;
      
    @Override
    protected void initBean(){        
    }
    
    @Override
    public void onOpenCard(){
    }
    
    @Override
    public String onCloseCard(){
        return super.onFinalCloseCard(null);
    }
    
    public void dateStartChange(){        
    }
    
    public void dateEndChange(){
        
    }
       
    public void onPreViewReport() {
        Map<String, Object> params = prepareReportParams(new HashMap<>());
        ArrayList<Object> dataReport = prepareReportData(new ArrayList<>());
        preViewReport(dataReport, params, getFormName());        
    }
   
    protected abstract ArrayList<Object> prepareReportData(ArrayList<Object> reportData);
    
    protected Map<String, Object> prepareReportParams(Map<String, Object> parameters){     
        parameters.put("USER_LOGIN", getCurrentUser().getLogin());
        String key = getReportBandleKey();
        parameters.put("REPORT_TITLE", EscomBeanUtils.getBandleLabel(key));
        return parameters;
    }
    
    protected void preViewReport(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        printService.doPrint(dataReport, parameters, reportName);
        String pdfFile = new StringBuilder()
                    .append(conf.getTempFolder())
                    .append(reportName)
                    .append("_")
                    .append(getCurrentUser().getLogin())
                    .append(".pdf").toString();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(pdfFile);
        paramMap.put("path", pathList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }
    
    protected abstract String getReportBandleKey();
    
    /* GETS & SETS */

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }
    public Date getDateStart() {
        return dateStart;
    }
    
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
    public Date getDateEnd() {
        return dateEnd;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }
    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }
    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }
    
    
}

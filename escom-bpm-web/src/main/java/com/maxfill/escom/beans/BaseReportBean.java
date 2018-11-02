package com.maxfill.escom.beans;

import com.maxfill.escom.beans.core.BaseView;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.PieChartModel;

public abstract class BaseReportBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -6471738532714818611L;    
            
    private Date dateStart;
    private Date dateEnd;
    
    protected PieChartModel pieModel;
    protected TreeNode[] selectedNodes;
    
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
        parameters.put("REPORT_TITLE", MsgUtils.getBandleLabel(key));
        return parameters;
    }
    
    protected void preViewReport(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        sessionBean.preViewReport(dataReport, parameters, reportName);       
    }
    
    protected abstract String getReportBandleKey();
    
    @Override
    public boolean isFullPageMode(){
        return false;
    }
    
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

package com.maxfill.escom.beans.processes.remarks;

import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.inject.Inject;

/* Сервисный бин "Замечания" */
@Named
@SessionScoped
public class RemarkBean extends BaseTableBean<Remark>{
    private static final long serialVersionUID = -257932838724865134L;
    
    @Inject
    private ProcessBean processBean;
    
    @EJB
    private RemarkFacade remarkFacade;   
        
    @Override
    public RemarkFacade getLazyFacade() {
        return remarkFacade;
    }            

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return processBean;
    }
    
    public void onPreViewRemarks(Doc doc){
        if (doc == null) return;
        
        StringBuilder sb = new StringBuilder(getLabelFromBundle("RemarksDocument"));
        sb.append(" ").append(doc.getFullName());
        
        Map<String, Object> params = prepareReportParams();
        params.put("REPORT_TITLE", sb.toString());
        
        List<Object> dataReport = remarkFacade.findActualDetailItems(doc, 0, 0, null, null, getCurrentUser()) 
                .stream()
                .map(remark->{
                    String data = DateUtils.dateToString(remark.getDateCreate(),  DateFormat.SHORT, null, getLocale());
                    String fio = remark.getAuthorName();                 
                    String stateName = getLabelFromBundle(remark.getStateName());
                    return new ReportData(fio, stateName, data, remark.getContent());
                })
                .collect(Collectors.toList());
        printService.doPrint(dataReport, params, DictPrintTempl.REPORT_REMARKS);
        sessionBean.onViewReport(DictPrintTempl.REPORT_REMARKS);
    }
    
    public class ReportData{
        private final String fio;
        private final String result;
        private final String date;        
        private final String remark;
        
        public ReportData(String fio, String result, String date, String remark) {
            this.fio = fio;
            this.result = result;
            this.date = date;            
            this.remark = remark;
        }

        public String getFio() {
            return fio;
        }

        public String getResult() {
            return result;
        }

        public String getDate() {
            return date;
        }                
        
        public String getRemark(){
            return remark;
        }
    }
}

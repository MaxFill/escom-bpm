package com.maxfill.escom.beans.docs.reports;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseReportBean;
import com.maxfill.escom.beans.docs.docsTypes.docTypeGroups.DocTypeGroupsBean;
import com.maxfill.escom.beans.system.reports.ReportPieData;
import com.maxfill.escom.beans.users.settings.UserReportsSettings;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.utils.DateUtils;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;import java.util.Map;
import java.util.Map.Entry;
;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Tuple;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.PieChartModel;

@Named
@ViewScoped
public class DocReportBean extends BaseReportBean{
    private static final long serialVersionUID = -6471738532714818611L;        
    
    @EJB
    private DocFacade docFacade;
    
    private TreeNode tree;
    
    @Inject
    private DocTypeGroupsBean docTypeGroupsBean;

    @Override
    protected void initBean() {
        ConcurrentHashMap<String, UserReportsSettings> settings = sessionBean.getUserSettings().getReportSetting();
        if (settings.containsKey(getFormName())){
            UserReportsSettings userReportsSettings = settings.get(getFormName());
            XMLGregorianCalendar dateBegin = (XMLGregorianCalendar) userReportsSettings.getSetting().get("dateStart");
            XMLGregorianCalendar dateEnd = (XMLGregorianCalendar) userReportsSettings.getSetting().get("dateEnd");
            setDateStart(dateBegin.toGregorianCalendar().getTime());
            setDateEnd(dateEnd.toGregorianCalendar().getTime());
        } else {
            settings.put(getFormName(), new UserReportsSettings());
        }
    }

    @Override
    public String onCloseCard() {
        try {
            ConcurrentHashMap<String, UserReportsSettings> settings = sessionBean.getUserSettings().getReportSetting();
            UserReportsSettings reportSettings = new UserReportsSettings();
            GregorianCalendar dateBegin = DateUtils.dateToGregorianCalendar(getDateStart());
            GregorianCalendar dateEnd = DateUtils.dateToGregorianCalendar(getDateEnd());
            XMLGregorianCalendar dateBeginXML = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateBegin);
            XMLGregorianCalendar dateEndXML = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateEnd);
            reportSettings.getSetting().put("dateStart", dateBeginXML);
            reportSettings.getSetting().put("dateEnd", dateEndXML);
            settings.put(getFormName(), reportSettings);             
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(DocReportBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard();
    }    
    
    public void makeReport(){
        if (selectedNodes == null || selectedNodes.length == 0){
            EscomBeanUtils.ErrorMsgAdd("Error", "NeedSelectOneGroup", null);
            return;
        }
                
        List<DocTypeGroups> docTypeGroups = new ArrayList<>();
        for (TreeNode node : selectedNodes){
            DocTypeGroups group = (DocTypeGroups) node.getData();
            docTypeGroups.add(group);            
        }
        
        List<Tuple> docTypes = docFacade.countDocByDocTypeGroups(docTypeGroups, getDateStart(), getDateEnd(), docTypeGroups);
        if (docTypes.isEmpty()){
            EscomBeanUtils.WarnMsgAdd("Warning", "NO_SEARCHE_FIND");        
            if (pieModel != null) pieModel = null;
            return;
        }
        
        tree.getChildren().get(0).setExpanded(false);
        
        pieModel = new PieChartModel();
        docTypes.stream().forEach(tuple -> pieModel.set((String)tuple.get(0), (Long)tuple.get(1)));
        pieModel.setTitle(EscomBeanUtils.getBandleLabel("DocReportCountTypes"));
        pieModel.setLegendPosition("w");
        pieModel.setShowDataLabels(true); 
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.REP_DOC_COUNT_TYPES;
    }
    
    private TreeNode makeTree(){
        return docTypeGroupsBean.makeTree();
    }
    
    @Override
    protected Map<String, Object> prepareReportParams( Map<String, Object> parameters){
        parameters.put("DATE_START", getDateStart());
        parameters.put("DATE_END", getDateEnd());
        return super.prepareReportParams(parameters);
    }
    
    /* GETS & SETS */

    public TreeNode getTree() {
        if (tree == null){
            tree = makeTree();
            tree.setExpanded(true);
            tree.getChildren().get(0).setExpanded(true);
        }
        return tree;
    }
    public void setTree(TreeNode tree) {
        this.tree = tree;
    }

    @Override
    protected String getReportBandleKey() {
        return "DocReportCountTypes";
    }

    @Override
    protected ArrayList<Object> prepareReportData(ArrayList<Object> reportData) {
        Map<String, Number> data = pieModel.getData();
        data.entrySet().stream().forEach(record -> reportData.add(new ReportPieData(record.getKey(), record.getValue())));        
        return reportData;
    }
    
    
}

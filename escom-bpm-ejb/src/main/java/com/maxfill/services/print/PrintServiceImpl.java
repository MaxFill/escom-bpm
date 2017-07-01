package com.maxfill.services.print;

import com.maxfill.Configuration;
import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Stateless
public class PrintServiceImpl implements PrintService{
    @EJB
    protected Configuration conf;

    public PrintServiceImpl() {
    }
            
    @Override
    public void doPrint(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){    
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataReport);
        //ClassLoader classLoader = getClass().getClassLoader();
        //String fileName = classLoader.getResource(reportName).getFile();
        //File reportPattern = new File(fileName);
        //JasperDesign jasperDesign;
        try {
            //jasperDesign = JRXmlLoader.load(reportPattern);
            //JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            String reportFile = new StringBuilder()
                    .append(conf.getJasperReports())
                    .append(reportName)
                    .append(".jasper").toString();
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportFile, parameters, beanColDataSource);

            String targetFile = new StringBuilder()
                    .append(conf.getTempFolder())
                    .append(reportName)
                    .append("_")
                    .append(parameters.get("USER_LOGIN"))
                    .append(".pdf").toString();
            JasperExportManager.exportReportToPdfFile(jasperPrint, targetFile);
        } catch (JRException ex) {
            Logger.getLogger(PrintServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
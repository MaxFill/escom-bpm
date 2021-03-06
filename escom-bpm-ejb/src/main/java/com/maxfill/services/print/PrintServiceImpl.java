package com.maxfill.services.print;

import com.maxfill.Configuration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRFont;
import net.sf.jasperreports.engine.JRPropertiesUtil;
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
    public void doPrint(List<Object> dataReport, Map<String, Object> parameters, String reportName){    
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
            final JRPropertiesUtil jrProps = JRPropertiesUtil.getInstance(DefaultJasperReportsContext.getInstance());
            jrProps.setProperty(JRFont.DEFAULT_PDF_FONT_NAME, conf.getJasperReports() + conf.getPdfFont());
            jrProps.setProperty(JRFont.DEFAULT_PDF_ENCODING, conf.getPdfEncoding());
            jrProps.setProperty(JRFont.DEFAULT_PDF_EMBEDDED, "TRUE");
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
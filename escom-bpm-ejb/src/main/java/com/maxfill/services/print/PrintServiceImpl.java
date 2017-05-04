
package com.maxfill.services.print;

import com.maxfill.model.BaseDict;
import com.maxfill.dictionary.SysParams;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 *
 * @author Maxim
 */
@Stateless
public class PrintServiceImpl implements PrintService{
    private static final String FILE_PATTERN = SysParams.PRINT_FORM_TEMPLATE + "card-item.jrxml";
    private static final String FILE_SAVING  = SysParams.UPLOAD_PATCH + "TestResult.pdf";

    public PrintServiceImpl() {
    }
            
    @Override
    public void doPrint(BaseDict item){    
        ArrayList<BaseDict> dataReport = new ArrayList<>();
        dataReport.add(item);
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataReport);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("DATE", new Date());
        ClassLoader classLoader = getClass().getClassLoader();
        String fileName = classLoader.getResource(FILE_PATTERN).getFile();
        File reportPattern = new File(fileName);
        JasperDesign jasperDesign;
        try {
            jasperDesign = JRXmlLoader.load(reportPattern);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, beanColDataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, FILE_SAVING);
        } catch (JRException ex) {
            Logger.getLogger(PrintServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

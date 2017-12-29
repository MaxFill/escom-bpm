package com.maxfill.services.searche;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

/**
 * Поиск
 * @author Maxim
 */
@Stateless
public class SearcheServiceImpl implements SearcheService {    
    private static final Logger LOGGER = Logger.getLogger(SearcheServiceImpl.class.getName());    
    private static final String MAX_ROW = "1000";
    
    @EJB 
    private Configuration conf;
    
    @Override
    @SuppressWarnings("empty-statement")
    public Set<Integer> fullSearche(String keyword){
        Set<Integer> result = new HashSet<>();
        keyword = keyword.replace("%", "*");
        Connection connection = conf.getFullTextSearcheConnection();
        try (Statement statement = connection.createStatement()){                                  
            searcheInDocs(result, keyword, statement);            
            return result;
	} catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return result;	
        }
    }
    
    private void searcheInDocs(Set<Integer> result, String keyword, Statement statement) throws SQLException{
        try (ResultSet resultSet = statement.executeQuery("SELECT DocId FROM escom_docs_index WHERE MATCH('" + keyword + "') LIMIT " + MAX_ROW)) {
            while (resultSet.next()) {
                result.add(resultSet.getInt(1));
            }
        }
    }
    
    @Asynchronous
    @Override
    public void deleteFullTextIndex(Doc doc){
        String sql = "DELETE FROM escom_docs_index WHERE Id= ?";
        Connection connection = conf.getFullTextSearcheConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){ 
            preparedStatement.setInt(1, doc.getId());
            preparedStatement.execute();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);          
        }
    }
    
    @Asynchronous
    @Override
    public void addFullTextIndex(Doc doc){
        String sql = "INSERT INTO escom_docs_index VALUES (?, ?, ?, ?)";
        executeChangeIndex(doc, sql);
    }
    
    @Asynchronous
    @Override
    public void updateFullTextIndex(Doc doc){
        String sql = "REPLACE INTO escom_docs_index VALUES (?, ?, ?, ?)";
        executeChangeIndex(doc, sql);
    }

    private void executeChangeIndex(Doc doc, String sql){
        Connection connection = conf.getFullTextSearcheConnection();
        if (connection != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, doc.getId());
                preparedStatement.setString(2, doc.getName());
                preparedStatement.setString(3, loadContentFromPDF(doc.getMainAttache()));
                preparedStatement.setInt(4, doc.getId());
                preparedStatement.execute();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Получение текстового контента из файла pdf */
    private String loadContentFromPDF(Attaches attache){
        if (attache == null) return "";
        String content = "";
        StringBuilder sb = new StringBuilder();
        String basePath = sb.append(conf.getUploadPath()).append(attache.getGuid()).toString();
        String pdfFileName = basePath + ".pdf";
        try {            
            CommandLine commandLine = CommandLine.parse("pdftotxt.cmd");            
            commandLine.addArgument(pdfFileName);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);    
            String txtFileName = basePath + ".txt";
            File txtFile = new File(txtFileName);
            if (txtFile.exists()){
                byte[] encoded = Files.readAllBytes(Paths.get(txtFileName));
                Charset encoding = StandardCharsets.UTF_8;
                content = new String(encoded, encoding);
                txtFile.delete();                
            }            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return content;
    }
}

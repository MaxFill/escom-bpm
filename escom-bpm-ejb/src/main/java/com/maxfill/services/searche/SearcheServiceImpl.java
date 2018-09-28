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
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;

/**
 * Сервис Поиска
 * @author Maxim
 */
@Stateless
public class SearcheServiceImpl implements SearcheService {    
    private static final Logger LOGGER = Logger.getLogger(SearcheServiceImpl.class.getName());    
    private static final String MAX_ROW = "1000";
    
    @EJB 
    private Configuration conf;
    
    @Override
    public Set<Integer> fullSearche(String keyword){
        Set<Integer> result = new HashSet<>();
        keyword = keyword.replace("%", "*");
        try (Connection connection = getFullTextSearcheConnection()) {
            if(connection != null) {
                try (Statement statement = connection.createStatement()) {
                    searcheInDocs(result, keyword, statement);
                    return result;
                }
            }
        }catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
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
        try (Connection connection = getFullTextSearcheConnection()) {
            if(connection != null) {
                String sql = "DELETE FROM escom_docs_index WHERE Id= ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, doc.getId());
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
            
    @Asynchronous
    @Override
    public void updateFullTextIndex(Doc doc){
        try (Connection connection = getFullTextSearcheConnection()) {
            if(connection != null) {
                String sql;
                if (checkExistIndex(doc, connection)){
                    sql = "REPLACE INTO escom_docs_index VALUES (?, ?, ?, ?)";                    
                } else {
                    sql = "INSERT INTO escom_docs_index VALUES (?, ?, ?, ?)";
                }
                executeChangeIndex(doc, sql, connection);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Проверка наличия полнотекстового индекса для указанного документа
     * @param doc
     * @return 
     */
    private boolean checkExistIndex(Doc doc, Connection connection) throws SQLException{
        String sql = "SELECT id FROM escom_docs_index WHERE id = " + doc.getId();                
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        return preparedStatement.executeQuery().next();        
    }
            
    /* Изменение потнотекстового индекса */
    private void executeChangeIndex(Doc doc, String sql, Connection connection) throws SQLException{
        Attaches attache = doc.getMainAttache();                                    
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, doc.getId());
        preparedStatement.setString(2, doc.getName());
        preparedStatement.setString(3, loadAttacheContent(attache));
        preparedStatement.setInt(4, doc.getId());
        preparedStatement.execute();
    }

    public String loadAttacheContent(Attaches attache){
        if (attache == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(conf.getUploadPath());
        attache.getShortName(sb);
        sb.append(".");
        if ("txt".equals(attache.getExtension().toLowerCase())){
            sb.append(attache.getExtension());            
            return loadContentFromTXT(new File(sb.toString()));
        } else {            
            return loadContentFromPDF(sb.toString(), attache.getExtension());
        }
    } 
    
    private String loadContentFromTXT(File txtFile){
        String content = "";
        if (txtFile.exists()){
            try {
                    String path = txtFile.getPath();
                    byte[] encoded = Files.readAllBytes(Paths.get(path));
                    Charset encoding = StandardCharsets.UTF_8;
                    content = new String(encoded, encoding);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        return content;
    }
    
    /* Получение текстового контента из файла pdf */
    private String loadContentFromPDF(String basePath, String ext){        
        String pdfFileName = basePath + "pdf";                  
        
        String convertTXT = conf.getConvertorTXT();
        if (org.apache.commons.lang.StringUtils.isEmpty(convertTXT)) return "";
        
        String content = "";
        String txtFileName = basePath + "txt";
        try {
            CommandLine commandLine = CommandLine.parse(convertTXT);
            commandLine.addArgument(pdfFileName);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);          //создан временный файл txt    
            File txtFile = new File(txtFileName);   
            content = loadContentFromTXT(txtFile);
            txtFile.delete();                       //удалён временный файл
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return content;
    }
    
    @Override
    public Connection getFullTextSearcheConnection() throws SQLException {
        if (StringUtils.isBlank(conf.getFullSearcheConnect())) return null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(conf.getFullSearcheConnect(), "", "");
        } catch (ClassNotFoundException ex) {
            System.out.println("JDBC Driver not found!");
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

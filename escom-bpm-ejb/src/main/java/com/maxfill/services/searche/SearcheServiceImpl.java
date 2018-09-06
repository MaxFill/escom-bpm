package com.maxfill.services.searche;

import com.maxfill.Configuration;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.files.FileService;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
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
    @EJB
    private FileService fileService;
    
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
    public void addFullTextIndex(Doc doc){
        if (doc == null){
            LOGGER.log(Level.SEVERE, null, "ERROR: AddFullTextIndex doc is NULL!");
            return;
        }
        String sql = "INSERT INTO escom_docs_index VALUES (?, ?, ?, ?)";
        executeChangeIndex(doc, sql);
    }
    
    @Asynchronous
    @Override
    public void updateFullTextIndex(Doc doc){
        String sql = "REPLACE INTO escom_docs_index VALUES (?, ?, ?, ?)";
        executeChangeIndex(doc, sql);
    }

    /* Изменение потнотекстового индекса */
    private void executeChangeIndex(Doc doc, String sql){        
        try (Connection connection = getFullTextSearcheConnection()) {            
            if(connection != null) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {                    
                    preparedStatement.setInt(1, doc.getId());
                    preparedStatement.setString(2, doc.getName());
                    preparedStatement.setString(3, fileService.loadAttacheContent(doc.getMainAttache()));
                    preparedStatement.setInt(4, doc.getId());
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {            
            LOGGER.log(Level.SEVERE, null, ex);
        }
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

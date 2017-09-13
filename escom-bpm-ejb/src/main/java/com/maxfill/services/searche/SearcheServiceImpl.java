package com.maxfill.services.searche;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;

/**
 * Поиск
 * @author Maxim
 */
@Stateless
public class SearcheServiceImpl implements SearcheService {    
    private static final Logger LOGGER = Logger.getLogger(SearcheServiceImpl.class.getName());    
    private static final String MAX_ROW = "1000";
    
    @Override
    @SuppressWarnings("empty-statement")
    public Set<Integer> fullSearche(String keyword){
        Set<Integer> result = new HashSet<>();
        keyword = keyword.replace("%", "*");
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:9306?characterEncoding=utf8&maxAllowedPacket=512000","", "");;
            try (Statement statement = connection.createStatement()) {
                searcheInAttaches(result, keyword, statement);
                searcheInDocs(result, keyword, statement);
            }
            return result;
	} catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return result;
	}  finally {
		try {
                    if(connection != null){
                        connection.close();
                    }
		} catch (SQLException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			return result;
		}
        }
    }
    
    private void searcheInAttaches(Set<Integer> result, String keyword, Statement statement) throws SQLException{               
        try (ResultSet resultSet = statement.executeQuery("SELECT Doc FROM escom_attaches_index WHERE MATCH('" + keyword + "') LIMIT " + MAX_ROW)) {
            while (resultSet.next()) {
                result.add(resultSet.getInt(1));
            }
        }
    }
    
    private void searcheInDocs(Set<Integer> result, String keyword, Statement statement) throws SQLException{        
        try (ResultSet resultSet = statement.executeQuery("SELECT Id FROM escom_docs_index WHERE MATCH('" + keyword + "') LIMIT " + MAX_ROW)) {
            while (resultSet.next()) {
                result.add(resultSet.getInt(1));
            }
        }
    }
}

package com.maxfill.services.searche;

import com.maxfill.model.basedict.doc.Doc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public interface SearcheService {

    Set<Integer> fullSearche(String keyword);
    void deleteFullTextIndex(Doc doc);
    void updateFullTextIndex(Doc doc);
    Connection getFullTextSearcheConnection() throws SQLException;
}

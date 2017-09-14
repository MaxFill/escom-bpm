/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.services.searche;

import com.maxfill.model.docs.Doc;
import java.util.Set;

/**
 *
 * @author Maxim
 */
public interface SearcheService {

    Set<Integer> fullSearche(String keyword);
    public void deleteFullTextIndex(Doc doc);
    public void addFullTextIndex(Doc doc);
    public void updateFullTextIndex(Doc doc);
}

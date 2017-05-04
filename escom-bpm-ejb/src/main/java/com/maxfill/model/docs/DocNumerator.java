/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.model.docs;

import com.maxfill.services.numerator.NumeratorService;

/**
 *
 * @author mfilatov
 */
public interface DocNumerator extends NumeratorService{
    void doRegistDoc(Doc doc);
}

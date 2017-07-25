/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.dictionary;

/**
 *
 * @author mfilatov
 */
public final class DictRights {

    private DictRights() {
    }
    
    public static final int RIGHT_VIEW = 8;
    public static final int RIGHT_EDIT = 16;
    public static final int RIGHT_DELETE = 32;
    public static final int RIGHT_CHANGE_RIGHT = 64;
    public static final int RIGHT_CREATE = 128;
    public static final int RIGHT_ADD_CHILDS = 256;
    
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;
    
    public static final int ACTUALISE_IN_GROUP = 0;
    public static final int ACTUALISE_IN_CARD = 1;
}

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
    
    public static final Integer RIGHT_VIEW = 8;
    public static final Integer RIGHT_EDIT = 16;
    public static final Integer RIGHT_DELETE = 32;
    public static final Integer RIGHT_CHANGE_RIGHT = 64;
    public static final Integer RIGHT_CREATE = 128;
    
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_USER = 1;
}

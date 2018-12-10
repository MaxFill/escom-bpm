/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.dictionary;

/**
 *
 * @author Maxim
 */
public final class DictTaskStatus {

    private DictTaskStatus() {
    }
    
    public static final int EXE_IN_TIME = 0;
    public static final int EXE_IN_OVERDUE = 1;
    public static final int FINISH_IN_TIME = 2;
    public static final int FINISH_IN_OVERDUE = 3;
    public static final int EXE_PLAN_TODAY = 4;
}

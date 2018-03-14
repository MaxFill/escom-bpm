package com.maxfill.dictionary;

/**
 * Словарь для событий
 * @author mfilatov
 */
public final class DictLogEvents {

    private DictLogEvents() {
    }
    
    public static final String CHANGE_EVENT = "ObjectModified";
    public static final String DELETE_EVENT = "ObjectMoveInTrash";
    public static final String CREATE_EVENT = "ObjectCreate";
    public static final String SAVE_EVENT = "ObjectSaved";

    public static final String ENTER_EVENT = "UserEnter";
    public static final String EXIT_EVENT = "UserExit";
}

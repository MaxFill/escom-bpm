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
    
    public static final String PROCESS_START = "ProcessStarted";
    public static final String PROCESS_CANCELED = "ProcessСanceled";
    public static final String PROCESS_FINISHED = "ProcessFinished";
    
    public static final String TASK_FINISHED = "TaskIsDone";
    public static final String TASK_ASSIGNED = "TaskIsAssigned";
    public static final String TASK_CANCELLED = "TaskСanceled";
    
}

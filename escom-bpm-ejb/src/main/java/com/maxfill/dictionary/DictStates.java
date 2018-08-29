package com.maxfill.dictionary;

public final class DictStates {

    private DictStates() {
    }
    
    public final static int STATE_DRAFT = 5;
    public final static int STATE_ARHIVAL = 6;
    public final static int STATE_VALID = 7;
    public final static int STATE_EDITED = 8;
    public final static int STATE_RUNNING = 9;
    public final static int STATE_COMPLETED = 10;
    public final static int STATE_CANCELLED = 11;
    public final static int STATE_ISSUED = 12;
    public final static int STATE_CONFIRMED = 13;
    
    public final static Integer MOVED_AUTO = 0;
    public final static Integer MOVED_MANUAL = 1;
    public final static Integer MOVED_ANY = 2;
}

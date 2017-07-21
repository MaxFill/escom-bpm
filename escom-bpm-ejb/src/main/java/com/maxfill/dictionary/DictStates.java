package com.maxfill.dictionary;

public final class DictStates {

    private DictStates() {
    }
    
    public final static Integer STATE_DRAFT = 5;
    public final static Integer STATE_ARHIVAL = 6;
    public final static Integer STATE_VALID = 7;
    public final static Integer STATE_EDITED = 8;
    
    public final static Integer MOVED_AUTO = 0;
    public final static Integer MOVED_MANUAL = 1;
    public final static Integer MOVED_ANY = 2;
}

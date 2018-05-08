package com.maxfill.escom.beans;

import com.maxfill.model.process.schemes.task.Task;

/**
 *
 */
public interface ContainsTask {
  Task getTask();
  Boolean isShowExtTaskAtr();
}

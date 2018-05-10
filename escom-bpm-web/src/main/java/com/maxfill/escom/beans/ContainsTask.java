package com.maxfill.escom.beans;

import com.maxfill.model.task.Task;

/**
 *
 */
public interface ContainsTask {
  Task getTask();
  Boolean isShowExtTaskAtr();
  void onOpenTask(String beanId);
}

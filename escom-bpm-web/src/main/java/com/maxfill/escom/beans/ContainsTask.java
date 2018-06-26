package com.maxfill.escom.beans;

import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.model.task.Task;

/**
 *
 */
public interface ContainsTask extends BaseView {
  Task getTask();
  Boolean isShowExtTaskAtr();
  void onOpenTask(String beanId);
}

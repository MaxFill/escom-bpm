package com.maxfill.escom.beans.companies;

import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.model.BaseDict;
import org.primefaces.model.TreeNode;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/* Расширение контролёра обозревателя оргструктуры */
@Named
@ViewScoped
public class CompanyExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 4688543715184790711L;

    protected void initBean() {
        typeRoot = "Company";
    }

    /**
     * Определяет, может ли быть скопирован объект
     * @return
     */
    @Override
    public boolean isCanCopyTreeItem(){
        return getTreeSelectedNode() == null;
    }

    /**
     * Определяет доступность кнопки "Удалить" в дереве
     * @return
     */
    @Override
    public boolean isCanDeleteTreeItem(){
        return getTreeSelectedNode() == null;
    }

    @Override
    public TreeNode addNewItemInTree(BaseDict item, TreeNode parentNode){
        return super.addNewItemInTree(item, null);
    }
}
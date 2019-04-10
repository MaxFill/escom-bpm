package com.maxfill.escom.beans.companies;

import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.model.basedict.BaseDict;
import org.primefaces.model.TreeNode;

import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/* Расширение контролёра обозревателя для оргструктуры */
@Named
@ViewScoped
public class CompanyExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 4688543715184790711L;

    @Override
    protected void initBean() {
        typeRoot = "Company";
    }

    /**
     * Определяет, может ли быть скопирован объект
     * @return
     */
    @Override
    public boolean isCanCopyTreeItem(){
        return getTreeSelectedNode() != null;
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
        if (isItemRootType(item)){
            return super.addNewItemInTree(item, tree);
        } else {
            return super.addNewItemInTree(item, parentNode);
        }
    }
}
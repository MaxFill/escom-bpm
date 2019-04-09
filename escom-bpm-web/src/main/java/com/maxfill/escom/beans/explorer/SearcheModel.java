package com.maxfill.escom.beans.explorer;

import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.user.User;
import com.maxfill.utils.DateUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* Базовая модель данных для поиска объектов */
public class SearcheModel implements Serializable {
    private static final long serialVersionUID = 6832545210562543525L;    
    
    private String nameSearche;             //поиск в поле name
    private User authorSearche;             //поиск в поле автор
    private List<State> stateSearche = new ArrayList<>();       //поиск по состояниям
    
    private boolean onlyActualItem = true;          //признак поиска только актуальных объектов
    
    /* признак поиска с учётом вложенных групп, 
        false - поиск только в текущей папке, 
        true  - поиск также и во вложенных папках/группах
    */
    private boolean searcheInGroups = true;   //true-поиск во вложенных папках, false - поиск только в текущей папке      
    
    private String dateCreatePeriod;
    private Date dateCreateStart;
    private Date dateCreateEnd;
        
    private boolean fullTextSearche = true;
    
    private String dateChangePeriod;
    private Date dateChangeStart;
    private Date dateChangeEnd;
    
    private static final List<String> abcEnglSearche;
    private static final List<String> abcLocalSearche;
    
    private List<User> users = null; //список пользователей критериев поиска    
    
    static{
        abcEnglSearche = new ArrayList<>();
        abcEnglSearche.add("a");
        abcEnglSearche.add("b");
        abcEnglSearche.add("c");
        abcEnglSearche.add("d");
        abcEnglSearche.add("e");
        abcEnglSearche.add("f");
        abcEnglSearche.add("g");
        abcEnglSearche.add("h");
        abcEnglSearche.add("i");
        abcEnglSearche.add("j");
        abcEnglSearche.add("k");
        abcEnglSearche.add("l");
        abcEnglSearche.add("m");
        abcEnglSearche.add("n");
        abcEnglSearche.add("o");
        abcEnglSearche.add("p");
        abcEnglSearche.add("q");
        abcEnglSearche.add("r");
        abcEnglSearche.add("s");
        abcEnglSearche.add("t");
        abcEnglSearche.add("u");
        abcEnglSearche.add("v");
        abcEnglSearche.add("w");
        abcEnglSearche.add("x");
        abcEnglSearche.add("y");
        abcEnglSearche.add("z"); 
    }
    
    static{
        abcLocalSearche = new ArrayList<>();
        abcLocalSearche.add("а");
        abcLocalSearche.add("б");
        abcLocalSearche.add("в");
        abcLocalSearche.add("г");
        abcLocalSearche.add("д");
        abcLocalSearche.add("е");
        abcLocalSearche.add("ж");
        abcLocalSearche.add("з");
        abcLocalSearche.add("и");
        abcLocalSearche.add("к");
        abcLocalSearche.add("л");
        abcLocalSearche.add("м");
        abcLocalSearche.add("н");
        abcLocalSearche.add("о");
        abcLocalSearche.add("п");
        abcLocalSearche.add("р");
        abcLocalSearche.add("с");
        abcLocalSearche.add("т");
        abcLocalSearche.add("у");
        abcLocalSearche.add("ф");
        abcLocalSearche.add("х");
        abcLocalSearche.add("ц");
        abcLocalSearche.add("ч");
        abcLocalSearche.add("ш");
        abcLocalSearche.add("щ");
        abcLocalSearche.add("э");
        abcLocalSearche.add("ю");
        abcLocalSearche.add("я");
    }
     
    protected ExplorerBean explBean;    
    
    /* Добавление в параметры поискового запроса специфичных полей и условий */
    protected void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){};
    
    /**
     * Возвращает список идентификаторов состояний объекта, с учётом которых должен выполнятся поиск
     * @return 
     */
    public List<Integer> getStatesIds() {
        return stateSearche.stream().map(item -> item.getId()).collect(Collectors.toList());
    }
    
    public void changeDateChange(String period){
        dateChangePeriod = period;  
        if (dateChangePeriod != null){
            dateChangeStart = DateUtils.periodStartDate(dateChangePeriod, dateChangeStart);
            dateChangeEnd = DateUtils.periodEndDate(dateChangePeriod, dateChangeEnd);
        }
    }
     
    public void changeDateCreate(String period){
        dateCreatePeriod = period;  
        if (dateCreatePeriod != null){
            dateCreateStart = DateUtils.periodStartDate(dateCreatePeriod, dateCreateStart);
            dateCreateEnd = DateUtils.periodEndDate(dateCreatePeriod, dateCreateEnd);
        }
    }

    public void setExplBean(ExplorerBean explBean) {
        this.explBean = explBean;
    }
         
    public boolean isFullTextSearche() {
        return fullTextSearche;
    }
    public void setFullTextSearche(boolean fullTextSearche) {
        this.fullTextSearche = fullTextSearche;
    }
          
    public List<String> getAbcEnglSearche() {
        return abcEnglSearche;
    }

    public List<String> getAbcLocalSearche() {
        return abcLocalSearche;
    }

    public String getNameSearche() {
        return nameSearche;
    }
    public void setNameSearche(String nameSearche) {
        this.nameSearche = nameSearche;
    }

    public boolean isOnlyActualItem() {
        return onlyActualItem;
    }
    public void setOnlyActualItem(boolean onlyActualItem) {
        this.onlyActualItem = onlyActualItem;
    }

    public boolean isSearcheInGroups() {
        return searcheInGroups;
    }
    public void setSearcheInGroups(boolean searcheInGroups) {
        this.searcheInGroups = searcheInGroups;
    }
    
    public User getAuthorSearche() {
        return authorSearche;
    }
    public void setAuthorSearche(User authorSearche) {
        this.authorSearche = authorSearche;
    }
    
    public List<State> getStateSearche() {
        return stateSearche;
    }
    public void setStateSearche(List<State> stateSearche) {
        this.stateSearche = stateSearche;
    }

    public Date getDateCreateStart() {
        return dateCreateStart;
    }
    public void setDateCreateStart(Date dateCreateStart) {
        this.dateCreateStart = dateCreateStart;
    }

    public Date getDateCreateEnd() {
        return dateCreateEnd;
    }
    public void setDateCreateEnd(Date dateCreateEnd) {
        this.dateCreateEnd = dateCreateEnd;
    }

    public Date getDateChangeStart() {
        return dateChangeStart;
    }
    public void setDateChangeStart(Date dateChangeStart) {
        this.dateChangeStart = dateChangeStart;
    }

    public Date getDateChangeEnd() {
        return dateChangeEnd;
    }
    public void setDateChangeEnd(Date dateChangeEnd) {
        this.dateChangeEnd = dateChangeEnd;
    }

    public String getDateCreatePeriod() {
        return dateCreatePeriod;
    }
    public void setDateCreatePeriod(String dateCreatePeriod) {
        this.dateCreatePeriod = dateCreatePeriod;
    }

    public String getDateChangePeriod() {
        return dateChangePeriod;
    }
    public void setDateChangePeriod(String dateChangePeriod) {
        this.dateChangePeriod = dateChangePeriod;
    }
       
    public List<User> getUsers() {
        if (users == null){
            users = explBean.getUserBean().findAll();
        }
        return users;
    }
    
}

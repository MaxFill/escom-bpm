function clearSearche() {
    var elements = document.getElementsByClassName('btnClear');
    for (var i = 0; i < elements.length; i++) {
        //console.log("element " + i);
        var btn = elements[i];
        btn.click();
    }
        
    /*
    var dateChangeEndSearche = PF('dateChangeEndSearche');
    if (dateChangeEndSearche){
        PF('dateChangeEndSearche').setDate();
    }           
    */

    //PF('authorSearche').selectValue();
    //PF('cbmDocType').show();
    //PF('cbmDocType').uncheckAll();
    //PF('cbmDocType').hide();
    //PF('docNumberSearche').jq.val("");
    //PF('searchName').jq.val("");
    //PF('partnerSearche').selectValue();
    //PF('cbDateChange').selectValue();
    //PF('cbDateCreate').uncheck(); 
};
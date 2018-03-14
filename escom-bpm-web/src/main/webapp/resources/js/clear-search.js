function clearSearche() {
    var elements = document.getElementsByClassName('clear');
    for (var i = 0; i < elements.length; i++) {
        elements[i].click();
    }
    PF('authorSearche').selectValue();
    var dateCreateStartSearche = PF('dateCreateStartSearche');
    if (dateCreateStartSearche){
        PF('dateCreateStartSearche').setDate();
    }
    var dateCreateEndSearche = PF('dateCreateEndSearche');
    if (dateCreateEndSearche){
        PF('dateCreateEndSearche').setDate();
    }
    var dateChangeStartSearche = PF('dateChangeStartSearche');
    if (dateChangeStartSearche){
        PF('dateChangeStartSearche').setDate();
    }
    var dateChangeEndSearche = PF('dateChangeEndSearche');
    if (dateChangeEndSearche){
        PF('dateChangeEndSearche').setDate();
    }
    PF('cbDateCreate').uncheck();
    PF('cbDateChange').uncheck();
    var dateDoc = PF('cbDateDoc');
    if (dateDoc){
        var dateDocStartSearche = PF('dateDocStartSearche');
        if (dateDocStartSearche){
            PF('dateDocStartSearche').setDate();
        };
        var dateDocEndSearche = PF('dateDocEndSearche');
        if (dateDocEndSearche){
            PF('dateDocEndSearche').setDate();
        }
        PF('cbDateDoc').uncheck();
        PF('cbFullSearche').uncheck();
        PF('cbmDocType').show();
        PF('cbmDocType').uncheckAll();
        PF('cbmDocType').hide();
        PF('docNumberSearche').jq.val("");
        PF('partnerSearche').selectValue();
    }
};
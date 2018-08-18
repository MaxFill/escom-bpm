var isClose = 0;            

window.onbeforeunload = function (evt) {

    var message = "Confirm close the window! Data may be lost! Click 'Cancel' and use the button to exit the program.";
    if (typeof evt == "undefined") {
        evt = window.event;
    }

    if (isClose == 0) {
        if (evt) {
            evt.returnValue = message;

        }
        ;
        return message;
    }
};
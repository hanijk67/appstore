var currentDropDownDiv = null;

function debounce(fn, delay) {
    var timer = null;
    return function () {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    };
}

function throttle(callback, limit) {
    var wait = false;
    return function () {
        if (!wait) {
            callback.call();
            wait = true;
            setTimeout(function () {
                wait = false;
            }, limit);
        }
    }
}

function stopEventPropagation(event) {
    try {
        if (document.all) {
            event.cancelBubble = true;
        }
        else {
            event.stopPropagation();
        }
        try {
            $(event).stopPropagation();
        } catch (e) {
        }
    }
    catch (e) {
    }
}

function showHide(event, ctrl) {
    setHeaderId(ctrl + 'Head');
    var dropDownDiv = document.getElementById(ctrl);
    dropDownDiv.style.visibility = "visible";
    if (dropDownDiv.style.display == "inline") {
        dropDownDiv.style.display = "none";
        currentDropDownDiv = null;
    }
    else {
        dropDownDiv.style.display = "inline";
        //$(dropDownDiv).find("td").css("tex-align","right");
        if (currentDropDownDiv != null)
            currentDropDownDiv.style.display = "none";
        currentDropDownDiv = dropDownDiv;
    }

    try {
        var height = $(event.target).closest('.panel-body')[0].scrollHeight;
        $(event.target).closest('.panel-body').css('min-height', height + 'px');

    } catch (e) {
    }
    stopEventPropagation(event);
}

function selectAll(ctrl) {
    var chkList = document.getElementById(ctrl);
    var inputes = chkList.getElementsByTagName("input");
    for (var i = 0; i < inputes.length; i++) {
        var input = inputes[i];
        if (input.parentNode.style.display != "none")
            input.checked = true;
    }
}

function deselectAll(ctrl) {
    var chkList = document.getElementById(ctrl);
    var inputes = chkList.getElementsByTagName("input");
    for (var i = 0; i < inputes.length; i++) {
        var input = inputes[i];
        if (input.parentNode.style.display != "none")
            input.checked = false;
    }
}

function invertSelection(ctrl) {
    var chkList = document.getElementById(ctrl);
    var inputes = chkList.getElementsByTagName("input");
    for (var i = 0; i < inputes.length; i++) {
        var input = inputes[i];
        if (input.parentNode.style.display != "none")
            input.checked = !input.checked;
    }
}

function autoHideSelection() {
    if (currentDropDownDiv != null) {
        var header = document.getElementById(headerId);
        if (header != null) {
            var selectedText = getHeaderCaption(currentDropDownDiv);
            if (selectedText != null)
                header.innerHTML = selectedText;
            else
                header.innerHTML = "انتخاب";
        }
        currentDropDownDiv.style.display = "none";
        currentDropDownDiv = null;
    }
}

$(document).mouseup(function () {
    autoHideSelection()
});


var headerId;
function setHeaderId(ctrl) {
    headerId = ctrl;
}

function getHeaderCaption(chkList) {
    var labels = chkList.getElementsByTagName("label");
    var noOfChecked = 0;
    for (var i = 0; i < labels.length; i++) {
        var item = document.getElementById(labels[i].htmlFor);
        if (item.checked) {
            if (item.type == "checkbox")
                noOfChecked++;
            else
                return labels[i].innerHTML;
        }
    }
    if (noOfChecked == 0)
        return "انتخاب";
    else
        return noOfChecked + " انتخاب شده";
}

function tableGotoPage(url, currentPageCtrlId) {
    var currentPageCtrl = document.getElementById(currentPageCtrlId);
    Wicket.Ajax.post({'u': url + "&pageNo=" + currentPageCtrl.value});
}

function tableButtonClick(event, butCtrlId) {
    if (event.keyCode == 13) {
        var butCtrl = document.getElementById(butCtrlId);
        butCtrl.onclick.call();
        return false;
    }
    return true;
}

function treeSearch(url, currentPageCtrlId) {
    var currentPageCtrl = document.getElementById(currentPageCtrlId);
    Wicket.Ajax.post({'u': "searchLink=" + currentPageCtrl.value});

}

function treeButtonClick(event, butCtrlId) {
    if (event.keyCode == 13) {
        var butCtrl = document.getElementById(butCtrlId);
        butCtrl.onclick.call();
        return false;
    }
    return true;
}

function tableChangeRows(url, noOfRowsCtrlId) {
    var noOfRowsCtrl = document.getElementById(noOfRowsCtrlId);
    Wicket.Ajax.post({'u': url + "&noOfRows=" + noOfRowsCtrl.value});
}

function checkParent(pNode) {
    var disp = true;
    for (var i = 0; i < pNode.cells.length; i++) {
        var style = pNode.cells[i].style;
        disp = disp && style != null && pNode.cells[i].style.display == "none";
    }
    if (disp)
        pNode.style.display = "none";
    else
        pNode.style.display = null;
}

function doFilter(chkBoxId, ftxt) {
    try {

        var chkBoxCtrl = document.getElementById(chkBoxId);
        var choiceLabels = chkBoxCtrl.getElementsByTagName("label");
        for (var i = 0; i < choiceLabels.length; i++) {
            var lbl = choiceLabels[i];
            if (ftxt != "" && lbl.innerHTML.indexOf(ftxt) < 0) {
                lbl.parentNode.style.display = "none";
                checkParent(lbl.parentNode.parentNode);
            }
            else {
                lbl.parentNode.style.display = null;
                checkParent(lbl.parentNode.parentNode);
            }

        }
    }
    catch (e) {
    }
}

function filterChoices(event, txtField, chkBoxId) {
    if (event.keyCode == 13) {
        doFilter(chkBoxId, txtField.value);
        return false;
    }
    else {
        //	else if(event.keyCode==27){
        //		txtField.value = "";
        //		doFilter(chkBoxId, "");
        //		event.stopPropagation();
        //		return false;
        //	}
        return true
    }
}

function checkParent2(pNode) {
    var disp = true;
    var labels = pNode.getElementsByTagName("label");
    for (var i = 0; i < labels.length; i++) {
        var style = labels[i].style;
        disp = disp && style != null && labels[i].style.display == "none";
    }
    if (disp)
        pNode.display = "none";
    else
        pNode.display = null;
}

function doFilter2(chkBoxId, ftxt) {
    try {
        var chkBoxCtrl = document.getElementById(chkBoxId);
        var choiceLabels = chkBoxCtrl.getElementsByTagName("label");

        for (var i = 0; i < choiceLabels.length; i++) {
            var lbl = choiceLabels[i];
            if (ftxt != "" && lbl.innerHTML.indexOf(ftxt) < 0) {
                lbl.style.display = "none";
                $(lbl).prev("input[type='checkbox']")[0].style.display = "none";
                $(lbl).next("br").attr('style', 'display:none');
                checkParent2(lbl.parentNode);
            }
            else {
                lbl.style.display = null;
                $(lbl).prev("input[type='checkbox']")[0].style.display = null;
                $(lbl).next("br").removeAttr("style");
                checkParent2(lbl.parentNode);
            }

        }
    }
    catch (e) {
    }
}

function filterChoices2(event, txtField, chkBoxId) {
    if (event.keyCode == 13) {
        doFilter2(chkBoxId, txtField.value);
        return false;
    }
    else {
        return true
    }

}

function resetFilter(filterCtrlId, chkBoxId) {
    var filterCtrl = document.getElementById(filterCtrlId);
    filterCtrl.value = "";
    doFilter(chkBoxId, "");
}

function resetFilter2(filterCtrlId, chkBoxId) {
    var filterCtrl = document.getElementById(filterCtrlId);
    filterCtrl.value = "";
    doFilter2(chkBoxId, "");
}

function adjustWidth(ctrlId, doScroll) {
    var ctrl = document.getElementById(ctrlId);
    var choiceLabels = ctrl.getElementsByTagName("label");
    var maxLen = 0;
    var MIN_LEN = (doScroll != null && doScroll) ? 150 : 100;
    for (var i = 0; i < choiceLabels.length; i++) {
        var lbl = choiceLabels[i];
        var len = lbl.innerHTML.length;
        if (len > maxLen)
            maxLen = len;
    }
    var width = maxLen * 6 + 25;
    if (width < MIN_LEN)
        width = MIN_LEN;
    ctrl.style.width = width + "px";
    ctrl.style.visibility = "visible";
    ctrl.style.display = "none";
    document.getElementById(ctrlId + 'Head').innerHTML = getHeaderCaption(ctrl);
}

/*
 * IPPANEL, DATETIMEPANEL
 */
function handleFocse(prevTxtId, nextTxtId, event, length) {

    if (event.target.value.length == length || event.keyCode == 190 || event.keyCode == 191 || event.keyCode == 110) {
        if (nextTxtId != null) {
            var nextTxtIdField = document.getElementById(nextTxtId);
            nextTxtIdField.focus();
        }
    }
    else if (event.target.value.length == 0 && prevTxtId != null && event.keyCode == 8) {
        var prevTxtIdField = document.getElementById(prevTxtId);
        prevTxtIdField.focus();
    }
}

function showMessage2(msg) {
    var msgHolder = document.getElementById("msgHolder");
    msgHolder.innerHTML = msg;
    msgHolder.style.visibility = "visible";
}

function quickFind(url) {
    var id = window.prompt(":عبارت جستجو را وارد کنید");
    if (id != null && id.length > 0)
        Wicket.Ajax.post({'u': url + "&id=" + id});
}

function showTooltip(divId) {
    var div = document.getElementById(divId);
    div.style.visibility = "visible";
    div.style.display = "inline";
}

function hideTooltip(divId) {
    var div = document.getElementById(divId);
    div.style.display = "none";
}

function showMessage(message, title) {
    message = message.replace("\n", "<br>");

    var statesdemo = {
        state0: {
            title: title ? title : '',
            html: message,
            buttons: {OK: true},
            focus: 1,
            submit: function (e, v, m, f) {

            }
        }
    };

    $.prompt(statesdemo, {
        close: function () {
        }, zIndex: 10000
    });
}

function launchConfirmDialog(message, title, commandString) {
    var deferred = jQuery.Deferred();
    var statesdemo = {
        state0: {
            title: title,
            html: message,
            buttons: {Cancel: false, OK: true},
            focus: 1,
            submit: function (e, v, m, f) {
                deferred.resolve(v);
            }
        }
    };

    $.prompt(statesdemo, {
        close: function () {
            deferred.resolve(false);
        }, zIndex: 10000
    });

    deferred.then(function (value) {
        if (value) {
            console.log('-------->>>>', commandString);
            eval(commandString);
        }
    });
}

function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    return uuid;
}

var updateMessageTime = function (element) {
    var elems = $(element).find('.message-data-time');
    for (var i = 0; i < elems.length; i++) {
        var elemItem = elems[i];
        var textToAdd = jQuery.timeago(parseInt($(elemItem).text()));
        $(elemItem).text(textToAdd);
    }
};

var attachForwardMessageShow = function () {
    $('.message').on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        $('.message-after-detail', this).toggle();
    });
}

var updateMessageList = function (userListId, userId) {
    var el = $('#' + userListId).find('input[value="' + userId + '"]').siblings('div.about').find('[jid]');
    var newCount = parseInt($(el).text().replace('(', '').replace(')', '')) - 1;
    $(el).text(isNaN(newCount) || newCount <= 0 ? '' : '(' + newCount + ')');
}

var padZeroToDigit = function (digit) {
    digit = '' + digit;
    if(digit.length == 1)
        return "0" + digit;
    return digit;
}

function ajax_download(url, data, input_name) {
    var $iframe,
        iframe_doc,
        iframe_html;

    if (($iframe = $('#download_iframe')).length === 0) {
        $iframe = $("<iframe id='download_iframe'" +
            " style='display: none' src='about:blank'></iframe>"
        ).appendTo("body");
    }

    iframe_doc = $iframe[0].contentWindow || $iframe[0].contentDocument;
    if (iframe_doc.document) {
        iframe_doc = iframe_doc.document;
    }

    iframe_html = "<html><head></head><body><form method='POST' action='" +
        url +"'>" +
        (data ?
        "<input type=hidden name='" + input_name + "' value='" +
        JSON.stringify(data) +"'/></form>" +
        "</body></html>" : "");

    iframe_doc.open();
    iframe_doc.write(iframe_html);
    $(iframe_doc).find('form').submit();
}

function downloadURI(uri, name) {
    var link = document.createElement("a");
    link.download = name;
    link.href = uri;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    delete link;
}

function get_filesize(url) {
    var defer = {};

    var promise = new Promise(function(resolve, reject) {
        defer.resolve = function(data) {
            resolve(data);
        }
    });

    var xhr = new XMLHttpRequest();
    xhr.open("HEAD", url, true); // Notice "HEAD" instead of "GET",
                                 //  to get only the header
    xhr.onreadystatechange = function() {
        if (this.readyState == this.DONE) {
            defer.resolve(parseInt(xhr.getResponseHeader("Content-Length")));
        }
    };
    xhr.send();

    return promise;
}

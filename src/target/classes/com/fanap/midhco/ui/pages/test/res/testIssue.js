function showHistoryMessage(htmlHistoryMessage) {
    var myElement = $("div[jid='historyMessageId']").find('p').first();
    myElement.val = htmlHistoryMessage;
    myElement.append(htmlHistoryMessage);
}
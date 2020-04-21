function drawChart(canvasElem, labels, data) {
    var ctxL = $('#' + canvasElem)[0].getContext('2d');

    console.log('data.length is ', data.length);

    var dataSets = [];
    for(var i = 0; i < data.length; i++) {
        var dataSetObject = {};
        dataSetObject.label = data[i].label;
        dataSetObject.fillColor = "rgba(220,220,220,0.2)";
        dataSetObject.strokeColor = "rgba(220,220,220,1)";
        dataSetObject.pointColor = "rgba(220,220,220,1)";
        dataSetObject.pointStrokeColor = "#fff";
        dataSetObject.pointHighlightFill = "#fff";
        dataSetObject.pointHighlightStroke = "rgba(220,220,220,1)";
        dataSetObject.data = data[i].data;
        dataSets.push(dataSetObject);
    }

    console.log('labels is ' , labels, ' dataSets', dataSets);

    var myLineChart = new Chart(ctxL, {
        type: 'line',
        data: {
            labels: labels,
            datasets: dataSets
        },
        options: {
            responsive: true
        }
    });
}
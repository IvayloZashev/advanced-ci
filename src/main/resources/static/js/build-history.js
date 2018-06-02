(function() {
        "use strict";
        var page = 0;
        $.get("http://localhost:8090/history?page=" + page, function (zashevExecutionData) {
            buildHistoryTable(zashevExecutionData);
            buildHistoryPaging(zashevExecutionData.totalPages);
        });
    }
)();

function buildHistoryPage(element) {
    var page = element.dataset.page;
    $("#history-body").empty();
    $.get("http://localhost:8090/history?page=" + page, function (zashevExecutionData) {
            buildHistoryTable(zashevExecutionData);
    });
}

function buildHistoryTable(zashevExecutionData) {
    var tBodyElement = $("#history-body");
    var rowTeplate = "<tr>\n" +
        "     <td>%job%</td>\n" +
        "     <td>%date%</td>\n" +
        "     <td><button class=\"link file\" onclick=\"downloadLog(this)\" data-directory=\"%directory%\">%logFile%</button></td>\n" +
        "</tr>\n";
    var rowHtml;
    zashevExecutionData.content.forEach(function (executionRow) {
        $.get("http://localhost:8090/getJobName?id=" + executionRow.jobId, function (jobName) {
            var startDate = new Date(executionRow.startTime);
            var date = startDate.getDate()+"-"+getMonth(startDate.getUTCMonth())+"-"+startDate.getFullYear() + " " + startDate.getHours()+":"+startDate.getMinutes();
            rowHtml = rowTeplate.replace(/%job%/g, jobName)
                .replace(/%date%/g, date)
                .replace(/%directory%/g, executionRow.pathToLogFile)
                .replace(/%logFile%/g, executionRow.pathToLogFile.substring(executionRow.pathToLogFile.lastIndexOf("/") + 1));
            tBodyElement.append(rowHtml);
        });
    });
}

function buildHistoryPaging(totalPages) {
    var totalPagesIdx;
    var pagingContainer = $("#pages");
    var button = "<button class=\"link\" onclick=\"buildHistoryPage(this)\" data-page=\"%page%\">%totalPagesIdx%</button>";
    var buttonHtml;
    for (totalPagesIdx = 0; totalPagesIdx < totalPages; totalPagesIdx++) {
        buttonHtml = button.replace(/%totalPagesIdx%/g, totalPagesIdx + 1).replace(/%page%/g, totalPagesIdx);
        pagingContainer.append(buttonHtml);
    }
}

function downloadLog(element) {
    var directoryParam = element.dataset.directory;
    var fileName = element.innerHTML;
    $.ajax({
        url: 'http://localhost:8090/downloadFile?filePath=' + directoryParam,
        type: 'GET',

        contentType: 'application/json; charset=utf-8',
        success: function (data, textStatus, xhr) {
            download(data, fileName, "text/plain");
        },
        error: function (data, textStatus, xhr) {
            alert(data.responseText);
        }
    });
}

function getMonth(number) {
    var num = number + 1;
    if (number < 10) {
        return "0" + num;
    } else {
        return num;
    }
}
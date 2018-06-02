(function () {
    "use strict";
    var jobId = document.querySelector("#job-id").value;
    var jobName = document.querySelector("#job-name").value;
    var source = new EventSource("/run-job?jobId=" + jobId);
    var resultElement = $("#result");
    var summaryElement = $("#summary");
    var summaryHtml = "<div>Job %jobName% finished, check  <button class='log-file link' data-directory=\"%log-file%\" onclick='downloadLogFile(this)'>log file</button> for details</div>";
    source.addEventListener('message', function (response) {

        var line = response.data;
        if (line.indexOf("***stop sending") != -1) {
            var appendContent = summaryHtml.replace(/%jobName%/g, jobName)
                .replace(/%log-file%/g, line.substring(line.indexOf(": ") + 2));
            summaryElement.append(appendContent);
            source.close();
        } else {
            resultElement.append(response.data);
        }
    }, false);

})();

function downloadLogFile(element) {
    var logFileDirectory = element.dataset.directory;
    var fileName = logFileDirectory.substring(logFileDirectory.lastIndexOf("/") + 1);
    $.ajax({
        url: 'http://localhost:8090/downloadFile?filePath=' + logFileDirectory,
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
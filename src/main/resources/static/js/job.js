(function() {
        "use strict";
        var rootDirectoryValue = document.querySelector("#rootDirectory").value;
        var rootDirectory = rootDirectoryValue.replace(/\//g, "%2F").replace(/\\/g, "%2F");
        var fileTemplate = "<div><img src=\"%img%\" alt=\"image icon\"/><button class=\"link %class%\" onclick='getFiles(this)' data-directory=\"%directory%\">%file%</button><span style=\"display: %display%\">%fileSize%</span></div>";
        var filesContainer = $("#files-container");
        $.get("http://localhost:8090/listFiles?directory=" + rootDirectory, function (files) {
            buildWorkspace(files, fileTemplate, filesContainer);
        });
        $.get("http://localhost:8090/listRemoteBranches?projectRootDirectory=" + rootDirectory, function (branches) {
            buildBranchesSelect(branches);
            document.querySelector("#base-branch").addEventListener("change", baseBranchChanged);
        });


        // Adds pull event listener
        document.querySelector("#pull").addEventListener("click", pullFromRemoteRepo);
    }
)();

function getFiles(element) {
    var directory = element.dataset.directory;
    if (element.classList.contains("file")) {
        var fileName = directory.substring(directory.lastIndexOf("/")+1);
        var directoryParam = directory.replace(/\//g, "%2F").replace(/\\/g, "%2F");
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

    } else {
        var fileTemplate = "<div><img src=\"%img%\" alt=\"image icon\"/><button class=\"link %class%\" onclick='getFiles(this)'  data-directory=\"%directory%\">%file%</button><span style=\"display: %display%\">%fileSize%</span></div>";
        var filesContainer = $("#files-container");
        $.get("http://localhost:8090/listFiles?directory=" + directory, function (files) {
            filesContainer.empty();
            buildWorkspace(files, fileTemplate, filesContainer);
        });
        buildVisitedPath(directory);
    }
}

function buildVisitedPath(directory) {
    var searchedDirectory = directory.replace(/\//g, "%2F").replace(/\\/g, "%2F");
    var job = $("#name").val();
    $.get("http://localhost:8090//getParentDirectories?directory=" + searchedDirectory + "&job=" + job, function (parentDirectories) {
        $("#workspace-path").empty();
        $("#workspace-path").append("Path: ");
        if (parentDirectories.length > 0) {
            parentDirectories.reverse();
            parentDirectories.forEach(function (parentDirectory) {
                $("#workspace-path").append("<button class=\"link\" onclick='getFiles(this)' data-directory="+parentDirectory.path+">"+parentDirectory.name+"</button>");
            });
        }
    });
}

function buildWorkspace(files, fileTemplate, filesContainer) {
    if (files.length > 0) {
        var fileHtml;
        files.forEach(function (file) {
            if (file.file) {
                 fileHtml = fileTemplate.replace(/%file%/g, file.name)
                    .replace(/%directory%/g, file.path)
                    .replace(/%img%/g, "/img/text.png")
                    .replace(/%display%/g, "inline")
                    .replace(/%class%/g, "file")
                    .replace(/%fileSize%/g, file.sizeInBytes + " Bytes");
            } else {
                fileHtml = fileTemplate.replace(/%file%/g, file.name)
                    .replace(/%directory%/g, file.path)
                    .replace(/%img%/g, "/img/folder.png")
                    .replace(/%class%/g, "folder")
                    .replace(/%display%/g, "none");
            }
            filesContainer.append(fileHtml);
        });
    } else {
        filesContainer.append("No files in directory " + rootDirectory);
    }
}

function buildBranchesSelect(branches) {
    var optionTemplate = "<option value=\"%value%\">%text%</option>";
    var baseBranchSelectElement = $("#base-branch");
    var optionHtml;
    branches.forEach(function (branch) {
        optionHtml = optionTemplate
            .replace(/%value%/g, branch.branchId.name)
            .replace(/%text%/g, branch.name);
        baseBranchSelectElement.append(optionHtml);
    });
}

function baseBranchChanged() {
    var baseBranchId = this.value;
    var selectedIndex = this.selectedIndex;
    var selectedItemText;

    var url = window.location.href;
    var jobId = url.substring(url.lastIndexOf("/") + 1);
    console.log("jobId", jobId);
    if (selectedIndex != -1) {
        selectedItemText = this.options[selectedIndex].text
    }
    var baseBranchName = selectedItemText;
    var rootDirectory = document.querySelector("#rootDirectory").value;
    $.get("http://localhost:8090/checkoutBaseBranch?baseBranchId="+baseBranchId+"&baseBranchName="+baseBranchName+"&projectRootDirectory=" + rootDirectory+"&jobId=" + jobId, function (branch) {
        if (branch === baseBranchName) {
            var branchElement = document.querySelector("#base-branch");
            console.log("branch", branch);
        } else {
            alert("Could not checkout")
        }
        console.log("branch checkout", branch);
    });
}

function pullFromRemoteRepo() {
    var rootDirectory = document.querySelector("#rootDirectory").value;
    var rootDirectoryVal = rootDirectory.replace(/\//g, "%2F").replace(/\\/g, "%2F");
    var selectedIndex = document.querySelector("#base-branch").selectedIndex;
    var remoteBranch = document.querySelector("#base-branch")[selectedIndex].textContent;
    $.get("http://localhost:8090/pull?projectRootDirectory=" + rootDirectoryVal+"&remoteBranch="+remoteBranch, function (status) {
        $("#pull-result").empty();
        $("#pull-result").append("<p>"+status+"</p>");
        $("#pull-result").css("display", "block");
    });
}
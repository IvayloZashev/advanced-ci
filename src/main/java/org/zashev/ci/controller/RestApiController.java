package org.zashev.ci.controller;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.zashev.ci.model.Execution;
import org.zashev.ci.model.FileEntity;
import org.zashev.ci.model.GitBranch;
import org.zashev.ci.model.Job;
import org.zashev.ci.repository.ExecutionRepository;
import org.zashev.ci.repository.JobRepository;
import org.zashev.ci.util.FileEntityChainedComparator;
import org.zashev.ci.util.FileEntityIsFileComparator;
import org.zashev.ci.util.FileSizeComparator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
public class RestApiController {

    @Autowired
    ExecutionRepository executionRepository;

    @Autowired
    JobRepository jobRepository;

    @Value("${advanced.ci.root.workspace}")
    private String rootWorkspace;

    @GetMapping(value = "/listFiles")
    public List<FileEntity> listFiles(@RequestParam(value = "directory", required = true) String directory) {
        File rootFolder = new File(directory);
        List<FileEntity> files = new ArrayList<>();
        if (rootFolder.isDirectory()) {
            File[] rootFolderContent = rootFolder.listFiles();
            if (rootFolderContent != null) {
                for (File file : rootFolderContent) {
                    FileEntity fileEntity = new FileEntity();
                    String filePath = file.getAbsolutePath();
                    fileEntity.setPath(filePath);
                    fileEntity.setName(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
                    if (file.isFile()) {
                        fileEntity.setFile(true);
                        fileEntity.setSizeInBytes(file.length());
                    } else {
                        fileEntity.setSizeInBytes(measureFolderSize(file));
                    }
                    files.add(fileEntity);
                }
            }
        } else {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setPath(directory);
            fileEntity.setName(directory.substring(directory.lastIndexOf(File.separator) + 1));
            fileEntity.setFile(false);
            files.add(fileEntity);
        }
        Collections.sort(files, new FileEntityChainedComparator(new FileEntityIsFileComparator(), new FileSizeComparator()));
        return files;
    }

    @GetMapping(value = "/getParentDirectories")
    public List<FileEntity> getParentDirectories(@RequestParam(value = "directory", required = true) String directory, @RequestParam(value = "job", required = true) String job) {
        File searchedDirectory = new File(directory);
        List<FileEntity> parentDirectories = new ArrayList<>();

        if (!searchedDirectory.getAbsolutePath().equals(rootWorkspace + File.separator + job)) {
            FileEntity searchedEntity = new FileEntity();
            searchedEntity.setPath(directory);
            searchedEntity.setName(directory.substring(directory.lastIndexOf(File.separator)));
            parentDirectories.add(searchedEntity);
            while (!searchedDirectory.getParentFile().getAbsolutePath().equals(rootWorkspace + File.separator + job)) {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setPath(searchedDirectory.getParent());
                fileEntity.setName(searchedDirectory.getParent().substring(searchedDirectory.getParent().lastIndexOf(File.separator)));
                parentDirectories.add(fileEntity);
                searchedDirectory = new File(searchedDirectory.getParent());
            }
            FileEntity rootEntity = new FileEntity();
            rootEntity.setPath(rootWorkspace + File.separator + job);
            rootEntity.setName("~");
            parentDirectories.add(rootEntity);
        }
        return parentDirectories;
    }

    private static long measureFolderSize(File folder) {
        File[] folderContent = folder.listFiles();
        long size = 0;
        if (folderContent != null) {
            for (File file : folderContent) {
                if (file.isDirectory()) {
                    size += measureFolderSize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    @GetMapping("/history")
    public Page<Execution> list(@RequestParam(value = "page") Integer page) {
        int size = 10;
        Pageable pageable = new PageRequest(page, size);
        return executionRepository.findAll(pageable);
    }

    @GetMapping("/getJobName")
    public String getJobName(@RequestParam(value = "id") Integer id) {
        return jobRepository.getJobName(id);
    }

    @GetMapping(value = "/listRemoteBranches")
    public List<GitBranch> getAllRemoteBranches(@RequestParam(value = "projectRootDirectory") String projectRootDirectory) throws IOException, GitAPIException {
        Git git = Git.open(new File(projectRootDirectory + File.separator + ".git"));
        List<GitBranch> remoteBranches = new ArrayList<>();
        List<Ref> remoteBranchObjects = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        String remoteBranch, remoteBranchName;
        for (Ref remoteBranchObject : remoteBranchObjects) {
            remoteBranch = remoteBranchObject.getName();
            remoteBranchName = remoteBranch.substring(remoteBranch.lastIndexOf(File.separator) + 1);
            if (!remoteBranchName.equalsIgnoreCase("HEAD")) {
                GitBranch gitBranch = new GitBranch();
                gitBranch.setBranchId(remoteBranchObject.getObjectId());
                gitBranch.setName(remoteBranchName);
                remoteBranches.add(gitBranch);
            }
        }

        return remoteBranches;
    }

    @GetMapping(value = "/checkoutBaseBranch")
    public String checkoutBaseBranch(@RequestParam(value = "baseBranchId") String baseBranchId,
                                     @RequestParam(value = "baseBranchName") String baseBranchName,
                                     @RequestParam(value = "projectRootDirectory") String projectRootDirectory,
                                     @RequestParam(value = "jobId") Integer jobId) throws GitAPIException, IOException {
        Git git = Git.open(new File(projectRootDirectory + "/.git"));
        boolean createBranch = !ObjectId.isId(baseBranchId);
        Ref ref = git.checkout().
                setCreateBranch(createBranch).
                setName(baseBranchName).
                setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                setStartPoint("origin/" + baseBranchName).
                call();


        if (ref.getName().substring(ref.getName().lastIndexOf("/") + 1).equals(baseBranchName)) {
            Job job = jobRepository.findOne(jobId);
            job.setBaseBranch(ref.getName());
            jobRepository.updateJob(job.getBuildCommand(), job.getDescription(), job.getId(), ref.getName());
            return ref.getName().substring(ref.getName().lastIndexOf("/") + 1);
        } else {
            return "Could not cheackout to " + baseBranchName;
        }
    }

    @GetMapping(value = "/pull")
    public String pull(@RequestParam(value = "projectRootDirectory") String projectRootDirectory,
        @RequestParam(value = "remoteBranch") String remoteBranch) throws IOException, GitAPIException {
        Git git = Git.open(new File(projectRootDirectory + File.separator + ".git"));

        PullCommand pullCommand = git.pull();
        pullCommand.setRemote("origin");
        pullCommand.setRemoteBranchName(remoteBranch);

        pullCommand.setRebase(true);
        PullResult pullResult = pullCommand.call();
        RebaseResult.Status status = pullResult.getRebaseResult().getStatus();
        if (status == RebaseResult.Status.UP_TO_DATE) {
            return "Repository up-to date with remote";
        } else if (status == RebaseResult.Status.FAST_FORWARD) {
            return "Pull changes from remote repo.";
        } else {
            return "Error occurred while pulling.";
        }
    }
}

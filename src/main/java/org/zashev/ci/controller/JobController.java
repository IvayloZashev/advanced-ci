package org.zashev.ci.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.zashev.ci.model.Execution;
import org.zashev.ci.model.Job;
import org.zashev.ci.repository.ExecutionRepository;
import org.zashev.ci.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class JobController {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    ExecutionRepository executionRepository;

    @Value("${advanced.ci.root.workspace}")
    private String rootWorkspace;

    @RequestMapping("/")
    String home(ModelMap model) {
        List<Job> jobs = new ArrayList<>();
        for (Job job : jobRepository.findAll()) {
            long biggest = 0;
            for (Execution execution : job.getExecutionList()) {
                if (execution.getEndTime() > biggest) {
                    biggest = execution.getEndTime();
                }
            }
            job.setLastBuild(biggest);
            jobs.add(job);
        }
        model.addAttribute("jobs", jobs);
        model.addAttribute("job", new Job());
        model.addAttribute("name", "Ivaylo");
        return "index";
    }

    @RequestMapping("/create-job")
    String createJob(ModelMap model) {
        model.addAttribute("job", new Job());
        return "create-job";
    }

    @RequestMapping("/get-run-job/{jobId}")
    String getRunJOb(@PathVariable Integer jobId, Model model) {
        model.addAttribute("job", jobRepository.findOne(jobId));
        return "run-job";
    }

    @RequestMapping("/job/{jobId}")
    String getJob(@PathVariable Integer jobId, Model model) {
        Job job = jobRepository.findOne(jobId);
        model.addAttribute("job", job);
        return "job";
    }

    @RequestMapping(value = "/save-job", method = RequestMethod.POST)
    String saveJob(@ModelAttribute Job job) throws IOException {
        String projectRootDirectory = rootWorkspace + File.separator + job.getName();
        job.setProjectRootDirectory(projectRootDirectory);
        if (job.getId() == null) {
            Process p = Runtime.getRuntime().exec("git clone " + job.getGithubUrl() + " " + job.getProjectRootDirectory());
            String line;
            InputStream stderr = p.getErrorStream();
            InputStream stdout = p.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader error = new BufferedReader(new InputStreamReader(stderr));

            if (error.readLine() != null) {
                while ((line = error.readLine()) != null) {
                    System.out.println("error: " + line);
                }
            } else {
                while ((line = reader.readLine()) != null) {
                    System.out.println("Stdout: " + line);
                }
            }
        }
        job.setBaseBranch("master");
        jobRepository.save(job);
        return "redirect:/";
    }

    @PostMapping(value = "/update-job")
    public String updateJob(@ModelAttribute Job job, Model model) {
        jobRepository.updateJob(job.getBuildCommand(), job.getDescription(), job.getId(), job.getBaseBranch());
        model.addAttribute("job", jobRepository.findOne(job.getId()));
        return "job";
    }

    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET)
    public @ResponseBody
    HttpEntity<byte[]> download(@RequestParam(value = "filePath", required = true) String filePath) throws IOException {
        File file = new File(filePath);
        byte[] document = FileCopyUtils.copyToByteArray(file);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "pdf"));
        header.set("Content-Disposition", "inline; filename=" + file.getName());
        header.setContentLength(document.length);
        return new HttpEntity<byte[]>(document, header);
    }
}

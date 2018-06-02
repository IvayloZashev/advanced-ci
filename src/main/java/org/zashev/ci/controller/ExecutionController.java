package org.zashev.ci.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zashev.ci.model.Execution;
import org.zashev.ci.model.Job;
import org.zashev.ci.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.zashev.ci.repository.JobRepository;

import java.io.*;
import java.util.concurrent.*;

@Controller
public class ExecutionController {

    @Value("${advanced.ci.root.workspace}")
    private String rootWorkspace;

    @Autowired
    ExecutionRepository executionRepository;

    @Autowired
    JobRepository jobRepository;

    @RequestMapping(value = "/run-job", method = RequestMethod.GET)
    public SseEmitter runJob(@RequestParam Integer jobId) throws Exception {
        long startTime;

        Job job = jobRepository.findOne(jobId);
        final SseEmitter emitter = new SseEmitter();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String logFilesBaseDirectory = rootWorkspace + "/logs";
        File logFile = new File(logFilesBaseDirectory + "/" + job.getName() + System.currentTimeMillis() + "-log.txt");
        startTime = System.currentTimeMillis();
        Future<?> future = executorService.submit(() -> {
                    Process p = null;
                    try {
                        Thread.sleep(2000);
                        if (job.getBuildCommand().contains("mvn")) {
                            p = Runtime.getRuntime().exec(job.getBuildCommand() + " -f " + job.getProjectRootDirectory());
                        } else {
                            p = Runtime.getRuntime().exec(job.getBuildCommand() + " -p " + job.getProjectRootDirectory());
                        }
                        String line;
                        InputStream stdout = p.getInputStream();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                        FileOutputStream fos = new FileOutputStream(logFile);
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos));
                        while ((line = reader.readLine()) != null) {
                            emitter.send(line, MediaType.TEXT_PLAIN);
                            writer.write(line + "\n");
                            System.out.println("Stdout: " + line);
                        }
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        emitter.completeWithError(e);
                    }
                    Execution execution = new Execution();
                    execution.setStartTime(startTime);
                    execution.setEndTime(System.currentTimeMillis());
                    execution.setJobId(job.getId());
                    execution.setPathToLogFile(logFile.getAbsolutePath());
                    return execution;
                }
        );

        while (!future.isDone()) {
            System.out.println("Task is still not done...");
            Thread.sleep(200);
        }

        executionRepository.save((Execution) future.get());
        System.out.println("Task completed!");
        emitter.send("***stop sending: " + ((Execution) future.get()).getPathToLogFile());
        executorService.shutdown();
        return emitter;
    }

    @GetMapping("build-history")
    public String getBUildHistoryContent(ModelMap model) {
        model.addAttribute("history", executionRepository.findAll());
        return "build-history";
    }
}
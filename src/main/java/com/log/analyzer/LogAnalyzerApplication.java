package com.log.analyzer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.analyzer.dto.LogData;
import com.log.analyzer.exception.LogAnalyzerException;
import com.log.analyzer.modal.AnalyzedData;
import com.log.analyzer.repo.AnalyzedDataRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class LogAnalyzerApplication {

    public static final int LIMIT_MILLISECONDS = 4;

    public static void main(String[] args) {

        SpringApplication.run(LogAnalyzerApplication.class, args);
    }

    /**
     * Command runner method, Responsible for processing args pass from command line
     *
     * @param repository AnalyzedDataRepo
     * @return
     */
    @Bean
    public CommandLineRunner logAnalyzer(AnalyzedDataRepo repository) {
        log.info("Execution started for command runner");
        return (args) -> {
            if (args.length > 0) {
                if (args[0].contains("txt")) {
                    List<LogData> logDataList = getLogDataFromTextLogFile(args);

                    Map<String, List<LogData>> timeStampMap = new HashMap<>();
                    for (LogData data : logDataList) {
                        if (timeStampMap.containsKey(data.getId())) {
                            List<LogData> timeStampList = new ArrayList<>(timeStampMap.get(data.getId()));
                            timeStampList.add(data);
                            timeStampMap.put(data.getId(), timeStampList);
                        } else {
                            timeStampMap.put(data.getId(), Collections.singletonList(data));
                        }
                    }
                    Map<String, Integer> differenceTimeMap = getTimeDifferenceMap(timeStampMap);

                    List<AnalyzedData> analyzedDataList = getAnalyzedDataList(logDataList, differenceTimeMap);
                    repository.saveAll(analyzedDataList);
                    List<AnalyzedData> analyzedDataListFromDb = (List<AnalyzedData>) repository.findAll();
                    log.info("Analyzed log data from in-Memory database");
                    analyzedDataListFromDb.forEach(System.out::println);
                } else {
                    log.info("Enter text file name and try again...!");
                    throw new LogAnalyzerException("Enter file is not text formatted file");
                }
            } else {
                log.info("Enter file name and try again...!");
                throw new LogAnalyzerException("File name is not entered");
            }

        };
    }

    /**
     * Method to create logData list from text file
     *
     * @param args arg[0] text file name
     * @return logDataList
     * @throws IOException
     */
    private List<LogData> getLogDataFromTextLogFile(String[] args) throws IOException, LogAnalyzerException {
        log.info("Inside getLogDataFromTextLogFile");
        List<LogData> logDataList = new ArrayList<>();
        String json = "";
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        if (br != null) {
            for (String line; (line = br.readLine()) != null; ) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            json = sb.toString();
            String[] lineArray = json.split("\n");
            for (String line : lineArray) {
                ObjectMapper mapper = new ObjectMapper();
                LogData logData = mapper.readValue(line, LogData.class);
                logDataList.add(logData);
            }
        } else {
            log.info("File doesn't have any content");
            throw new LogAnalyzerException("File doesn't have any content");
        }
        log.info("logDataList size {}", logDataList.size());
        return logDataList;
    }

    /**
     * Method to get analyzed data list
     *
     * @param logDataList       Log data list, created from text file
     * @param differenceTimeMap map with key as a event id and value as time taken for execution
     * @return analyzedDataList
     */
    private List<AnalyzedData> getAnalyzedDataList(List<LogData> logDataList, Map<String, Integer> differenceTimeMap) {
        List<AnalyzedData> analyzedDataList = new ArrayList<>();
        for (LogData data : logDataList) {
            AnalyzedData analyzedData = new AnalyzedData();
            analyzedData.setId(data.getId());
            analyzedData.setType(data.getType());
            analyzedData.setAlert(differenceTimeMap.get(data.getId()) > LIMIT_MILLISECONDS ? true : false);
            analyzedData.setHost(data.getHost());
            analyzedData.setDuration(differenceTimeMap.get(data.getId()));
            analyzedDataList.add(analyzedData);
        }
        return analyzedDataList;
    }

    /**
     * Method to map with key as a event id and value as time taken for execution
     *
     * @param timeStampMap Map with the key as event id and value as list of log data size 2(start and finish event)
     * @return differenceTimeMap
     */
    private Map<String, Integer> getTimeDifferenceMap(Map<String, List<LogData>> timeStampMap) {
        log.info("Inside -> getTimeDifferenceMap");
        Map<String, Integer> differenceTimeMap = new HashMap<>();
        Iterator<Map.Entry<String, List<LogData>>> iterator = timeStampMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<LogData>> entry = iterator.next();
            List<LogData> logList = entry.getValue();
            int startDateTime, finishDateTime;
            if (logList.get(0).getState().equalsIgnoreCase("STARTED")) {
                startDateTime = new Timestamp(Long.parseLong(logList.get(0).getTimestamp())).getNanos();
                finishDateTime = new Timestamp(Long.parseLong(logList.get(1).getTimestamp())).getNanos();

            } else {
                startDateTime = new Timestamp(Long.parseLong(logList.get(1).getTimestamp())).getNanos();
                finishDateTime = new Timestamp(Long.parseLong(logList.get(0).getTimestamp())).getNanos();
            }
            differenceTimeMap.put(entry.getKey(),
                    (int) TimeUnit.MILLISECONDS.convert(finishDateTime - startDateTime, TimeUnit.NANOSECONDS));
        }
        log.debug("Execution finished -> getTimeDifferenceMap --> {}", differenceTimeMap);
        return differenceTimeMap;
    }
}

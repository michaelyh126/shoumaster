package org.jeecg.modules.timer;

import org.jeecg.modules.demo.student_records.service.IStudentRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExceptionTimer {
    @Autowired
    private IStudentRecordsService studentRecordsService;

    @Scheduled(fixedRate = 5*1000)
public  void time_out(){
       studentRecordsService.getException();
    }
}

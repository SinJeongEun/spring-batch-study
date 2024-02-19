package com.example.springbatchexample.part3;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

@Log4j2
public class SavePersonListener {
    /**
     * 방법1. 인터페이스 구현
      */
    public static class SaverPersonJobExecutionListener implements JobExecutionListener {

        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("before job");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions().stream()
                            .mapToInt(StepExecution::getWriteCount)
                            .sum();
            log.info("after job  {}", sum);
            log.info("after job  getExecutionContext {}", jobExecution.getExecutionContext());
            log.info("after job  getStepExecutions {}", jobExecution.getStepExecutions());

           /* jobExecution.getStepExecutions()  => [StepExecution: id=3, version=13, name=savePersonStep, status=COMPLETED, exitStatus=COMPLETED
                    , readCount=100, filterCount=0, writeCount=100 readSkipCount=0, writeSkipCount=0, processSkipCount=0
                , commitCount=11, rollbackCount=0, exitDescription=]*/
        }
    }

    /**
     * 방법2 annotation 구현
      */
    public static class SavePersonAnnotationJobExecutionListener {

        @BeforeJob
        public void beforeJob(JobExecution jobExecution) {
            log.info("annotation before job");
        }

        @AfterJob
        public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions().stream()
                    .mapToInt(StepExecution::getWriteCount)
                    .sum();
            log.info("annotation after job  {}", sum);
        }
    }

    public static class SavePersonStepExecutionListener{
        @BeforeStep
        public void beforeStep(StepExecution stepExecution){
            log.info("before step");
        }

        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution){
            log.info("after step  getWriteCount", stepExecution.getWriteCount());
            log.info("after step stepExecution {}", stepExecution);

            if(stepExecution.getWriteCount() == 0) return ExitStatus.FAILED;   // 상태 조작 가능
            // 스프링 배치에서는 내부적으로 step의 실패/종료 등 상태를 저장하고있음.
            return stepExecution.getExitStatus();

           /*StepExecution: id=4, version=12, name=savePersonStep, status=COMPLETED, exitStatus=COMPLETED, readCount=100
                   , filterCount=0, writeCount=100 readSkipCount=0, writeSkipCount=0, processSkipCount=0, commitCount=11
                , rollbackCount=0, exitDescription=*/

        }
    }
}

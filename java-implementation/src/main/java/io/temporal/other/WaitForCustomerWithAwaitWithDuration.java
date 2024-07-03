package io.temporal.other;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class WaitForCustomerWithAwaitWithDuration {


    static final String TASK_QUEUE = "WaitForCustomerWithAwaitWithDuration";
    static final String WORKFLOW_ID = "WaitForCustomerWithAwaitWithDuration";


    public static void main(String[] args) throws InterruptedException {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);


        worker.registerWorkflowImplementationTypes(WaitForCustomerWorkflowImpl.class);



        factory.start();

        WaitForCustomerWorkflow workflow =
                client.newWorkflowStub(
                        WaitForCustomerWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());




        WorkflowClient.start(workflow::run, "");

        Thread.sleep(1000);

        workflow.customerActionSignalName(CustomerAction.CustomerActionAmend);

        client.newUntypedWorkflowStub(WORKFLOW_ID).getResult(String.class);

        System.exit(0);
    }




    @WorkflowInterface
    public interface WaitForCustomerWorkflow {


        @WorkflowMethod
        String run(String name);

        @SignalMethod
        void customerActionSignalName(CustomerAction customerAction);
    }




    public static class WaitForCustomerWorkflowImpl implements WaitForCustomerWorkflow {

        private static final Logger log = LoggerFactory.getLogger(WaitForCustomerWorkflowImpl.class);

        private CustomerAction customerAction;


        @Override
        public String run(String name) {


            // The different with WaitForCustomerWithCancellableScope is that
            // here we don't cancell the timer.
            Workflow.await(Duration.ofSeconds(10), ()-> customerAction != null);

            if(customerAction != null){
                log.info("Received customer action " + this.customerAction);
            }else {
                log.info("Timed out waiting for customer action " + this.customerAction);
                this.customerAction = CustomerAction.CustomerActionTimedOut;
            }

            return customerAction.name();
        }

        @Override
        public void customerActionSignalName(CustomerAction customerAction) {
            this.customerAction = customerAction;
        }

    }
}

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

public class WaitForCustomerWithCancellableScope {

    static final String TASK_QUEUE = "WaitForCustomerWithCancellableScope";
    static final String WORKFLOW_ID = "WaitForCustomerWithCancellableScope";


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


    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod
        String processPayment(Fulfillment fulfillment);

        @ActivityMethod
        String processShipment(Fulfillment fulfillment);
    }

    public static class WaitForCustomerWorkflowImpl implements WaitForCustomerWorkflow {

        private static final Logger log = LoggerFactory.getLogger(WaitForCustomerWorkflowImpl.class);

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(10))
                                .build());
        private CustomerAction customerAction;


        @Override
        public String run(String name) {


            final CancellationScope scope = Workflow.newCancellationScope(()->{
                 Workflow.await(Duration.ofSeconds(10),()-> customerAction != null );
            });
            scope.run();

            if(customerAction != null){
                log.info("Received customer action " + this.customerAction);
                scope.cancel();
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

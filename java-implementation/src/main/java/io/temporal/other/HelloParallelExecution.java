package io.temporal.other;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TemporalFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class HelloParallelExecution {

    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWorkflow";
    private static final String Error_PaymentFulfillment = "Error_PaymentFulfillment";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);


        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


        worker.registerActivitiesImplementations(new GreetingWorkflowImpl.GreetingActivitiesImpl());

        factory.start();

        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());


        List<String> greeting = workflow.run("World");

        // Display workflow execution results
        System.out.println(greeting);
        System.exit(0);
    }

    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        List<String> run(String name);
    }


    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod
        String processPayment(Fulfillment fulfillment);

        @ActivityMethod
        String processShipment(Fulfillment fulfillment);
    }

    public static class GreetingWorkflowImpl implements GreetingWorkflow {
        private static final Logger log = LoggerFactory.getLogger(GreetingWorkflowImpl.class);


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(10))
                                .build());


        @Override
        public List<String> run(String name) {


            final List<Fulfillment> fulfillments = IntStream.rangeClosed(0, 10)
                    .mapToObj(Fulfillment::new).toList();


            final ArrayList<Promise<String>> promises = new ArrayList<>();
            for (Fulfillment fulfillment : fulfillments) {
                promises.add(Async.function(this::process, fulfillment));
            }


            List<String> processedItems = new ArrayList<>();

            try {
                //Wait for all to complete or one of them to fail
                Promise.allOf(promises).get();

            } catch (TemporalFailure e) {
                for (Promise<String> promise : promises) {
                    final RuntimeException failure = promise.getFailure();
                    if (failure != null) {
                        //parallel executions failed, do something...
                        log.error("Error >> : " + failure);
                        if (failure instanceof ActivityFailure) {

                            final ApplicationFailure applicationFailureCause = (ApplicationFailure) failure.getCause();
                            if (applicationFailureCause.getType().equals(Error_PaymentFulfillment)) {
                                //TODO Compensate payment...
                                Fulfillment failedFulfillment = applicationFailureCause.getDetails().get(0, Fulfillment.class);
                                log.error("Compensating payment for failed: " + failedFulfillment);

                            }
                        }


                    } else {
                        processedItems.add(promise.get());
                    }
                }

            }


            return processedItems;
        }


        private String process(Fulfillment fulfillment) {

            activities.processPayment(fulfillment);

            return activities.processShipment(fulfillment);
        }


        static class GreetingActivitiesImpl implements GreetingActivities {
            private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

            @Override
            public String processPayment(final Fulfillment fulfillment) {
                log.info("executing processPayment...");
                sleepRandomDuration();
                return fulfillment.toString();
            }

            private void sleepRandomDuration() {
                // Sleep random duration
                int randomSleep = new Random().nextInt(100) * 50;
                //Simulate some work
                log.info("About to sleep..." + randomSleep / 1000);

                try {
                    Thread.sleep(randomSleep);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String processShipment(final Fulfillment fulfillment) {

                if (fulfillment.equals(new Fulfillment(3))) {
                    throw ApplicationFailure
                            .newNonRetryableFailure("Error processing Fulfillment",
                                    Error_PaymentFulfillment, fulfillment);
                }

                log.info("executing processShipment...");
                sleepRandomDuration();
                return fulfillment.toString();
            }
        }
    }
}

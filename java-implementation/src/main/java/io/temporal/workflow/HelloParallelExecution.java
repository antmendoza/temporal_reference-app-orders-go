package io.temporal.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloParallelExecution {

    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        List<String> getGreeting(String name);
    }


    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod
        String processPayment(Fulfillment fulfillment);

        @ActivityMethod
        String processShipment(Fulfillment fulfillment);
    }

    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(10))
                                .build());


        public static void main(String[] args) {

            WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

            WorkflowClient client = WorkflowClient.newInstance(service);

            WorkerFactory factory = WorkerFactory.newInstance(client);

            Worker worker = factory.newWorker(TASK_QUEUE);


            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

            factory.start();

            GreetingWorkflow workflow =
                    client.newWorkflowStub(
                            GreetingWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId(WORKFLOW_ID)
                                    .setTaskQueue(TASK_QUEUE)
                                    .build());


            List<String> greeting = workflow.getGreeting("World");

            // Display workflow execution results
            System.out.println(greeting);
            System.exit(0);
        }

        @Override
        public List<String> getGreeting(String name) {


            final List<Fulfillment> fulfillments = IntStream.rangeClosed(0, 10)
                    .mapToObj(Fulfillment::new).toList();


            final ArrayList<Promise<String>> promises = new ArrayList<>();
            for (Fulfillment fulfillment : fulfillments) {
                promises.add(Async.function(this::process, fulfillment));
            }

            //Wait for all to complete
            Promise.allOf(promises).get();

            List<String> processedItems = promises.stream().map(p -> p.get()).collect(Collectors.toList());


            return processedItems;
        }

        private void activity2() {
            System.out.println("activity2");
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
                int randomSleep = new Random().nextInt(100)*50;
                //Simulate some work
                log.info("About to sleep..." +randomSleep/1000 );

                try {
                    Thread.sleep(randomSleep);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String processShipment(final Fulfillment fulfillment) {
                log.info("executing processShipment...");
                sleepRandomDuration();
                return fulfillment.toString();
            }
        }
    }
}

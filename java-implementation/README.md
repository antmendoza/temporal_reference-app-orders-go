**Rob Holland**

So this workflow would be great to have in
Java: https://github.com/temporalio/reference-app-orders-go/blob/main/app/shipment/workflows.go It's quite short, and
covers waiting for and sending signals. Good example of human involvement. (edited)

**Rob Holland**

Skip over/elide anything you think isn't relevant to showing how it works


----

The waitForCustomer logic
also: https://github.com/temporalio/reference-app-orders-go/blob/main/app/order/workflows.go#L223

So combining a signal with a timer

And then last bit: The Java equivalent of workflow.Go doing stuff in parallel with an Await to wait for completion.

----
# Backend Home Task in Java
## Implementation of a Payment Service

### Goals
- Allow the user to send funds to another user of the service
- Allow the user to send funds to an external address using the given API. The interface is defined in `PaymentService/src/main/java/WithdrawalService/WithdrawalService.java`
- Allow the user to listen to updates on the trancsaction, sent by the service.

### Why gRPC and not REST or Aeron
REST seemed the logical choice as it is widely used and easy to implement. But I thought that if we needed to stream updates from the server, just having a listener instead
of constantly sending GET request to the server was cleaner. Also, gRPC uses protobuf, which allows us to define the API and Data Structure and use that across a large
panel of languages. So we could have a Web client, C++ or Python client without any issues while implementing the various requests.

I actually used Aeron in a very low latency setting. While I find it very fast and powerful, I feel like this project is not the use-case for such tool. We wouldn't benefit
from the gained speed that a binary protocol offers and the implementation is actually quite finicky IMHO.

We could also consider ZeroMQ, which implements a Message Bus in a C-Style socket code, but same as with Aeron, I don't see the point.

## Server
The server is coded using Java 17, and I use gradle to handle the dependancies (gRPC and Protobuf in that case) as well as the building/cleaning process.
It implements 3 methods :
- sendMoneyInternally
- - This checks the funds of a User and sends an user-defined amount to another user, bypassing the given API.
- sendMoneyExternally
- - This checks the funds of a User and sends an user-defined amount to an external address, using the API. The API sets a random delay (to mimic real life use case)
- AddUser
- - This adds an user, with his ID, name and surname, as well as balance. This is only for testing purpose. Server side, each user is given an address, which is a unique random UUID.

It also runs a seperated thread, which role is to check periodically (every 10ms in this case, but can be easily adjusted to something higher) pending external transaction if their state changed to COMPLETED or FAILED. 
If that is the case, we notify the client through the stream, else we continue to loop over those pending transactions.

The server stores the data in memory, using two ConcurrentHashMap, one for the Users, one for the Transactions (Both internal and external). **Keep in mind that in a real-world scenario, this would cause a memory
issue and we would use a database to keep some sort of transaction and user history. It is only done this way for testing purpose**

## Client
I created a client in Java, that launches multiple threads and will blast the servers of transactions. This is a quick and dirty way to test the implementation, before doing any unit tests. Given more time,
the best way of testing such service would be to use Postman.

## How to run

- Clone the repository
- Open Test-Client and PaymentService in separated Intellij instances
- Gradle refresh on both project
- Build both projects.
- Launch the server
- Launch the client.
- 
This code has only been tested on Windows 11, I didn't have a Linux machine ready to test the code.

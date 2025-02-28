package centralserver;

import common.*;

/*
 * Updated on Feb 2025
 */
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

 /* You can add/change/delete class attributes if you think it would be
  * appropriate.
  *
  * You can also add helper methods and change the implementation of those
  * provided if you think it would be appropriate, as long as you DO NOT
  * CHANGE the provided interface.
  */

/* TODO extend appropriate classes and implement the appropriate interfaces */
public class CentralServer implements ICentralServer {
    private List<MessageInfo> receivedMessages;
    
    protected CentralServer () throws RemoteException {
        super();

        /* TODO: Initialise Array receivedMessages */
	receivedMessages = new ArrayList<>();
    }

    public static void main (String[] args) throws RemoteException {
        CentralServer cs = new CentralServer();

        /* TODO: Configure Security Manager (If JAVA version earlier than version 17) */
	if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        /* TODO: Create (or Locate) Registry */
	Registry registry = LocateRegistry.createRegistry(9999);
	
        /* TODO: Bind to Registry */
	registry.rebind("CentralServer", cs);

        System.out.println("Central Server is running...");


    }


    @Override
    public void receiveMsg (MessageInfo msg) {
        System.out.println("[Central Server] Received message " + (msg.getMessageNum()) + " out of " +
                msg.getTotalMessages() + ". Measure = " + msg.getMessage());


        /* TODO: If this is the first message, reset counter and initialise data structure. */
	if (msg.getMessageNum() == 1) {
            receivedMessages.clear();
        }

        /* TODO: Save current message */
	receivedMessages.add(msg);

        /* TODO: If done with receiveing prints stats. */
	if (receivedMessages.size() == msg.getTotalMessages()) {
            printStats();
        }


    }

    public void printStats() {
      /* TODO: Find out how many messages were missing */

      /* TODO: Print stats (i.e. how many message missing?
       * do we know their sequence number? etc.) */

      /* TODO: Now re-initialise data structures for next time */
	int totalMessages = receivedMessages.size();
        int expectedMessages = receivedMessages.isEmpty() ? 0 : receivedMessages.get(0).getTotalMessages();
        int missingMessages = expectedMessages - totalMessages;

        System.out.println("[Central Server] Total Missing Messages = " + missingMessages + " out of " + expectedMessages);
	receivedMessages.clear();


    }

}

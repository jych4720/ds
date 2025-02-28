package field;
/*
 * Updated on Feb 2025
 */
import centralserver.ICentralServer;
import common.MessageInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

 /* You can add/change/delete class attributes if you wish.
  *
  * You can also add other methods and change the implementation of the methods
  * provided, as long as you DO NOT CHANGE the interface.
  */


public class FieldUnit implements IFieldUnit {
    private ICentralServer central_server;

    /* Note: Could you discuss in one line of comment you think can be
     * an appropriate size for buffsize? (used to init DatagramPacket?)
     */

    private static final int buffsize = 2048;
    private int timeout = 50000;
    private List<Float> receivedMessages;
    private List<Float> movingAverages;


    public FieldUnit () {
        /* TODO: Initialise data structures */
	receivedMessages = new ArrayList<>();
	movingAverages = new ArrayList<>();
    }

    @Override
    public void addMessage (MessageInfo msg) {
      /* TODO: Save received message in receivedMessages */
	receivedMessages.add(msg.getMessage());
    }

    @Override
    public void sMovingAverage (int k) {
        /* TODO: Compute SMA and store values in a class attribute */
        movingAverages.clear(); 
	for (int i = 0; i < receivedMessages.size(); i++) {
            if (i < k) {
                movingAverages.add(receivedMessages.get(i)); 
            } else {
                float sum = 0;
                for (int j = i - k + 1; j <= i; j++) {
                    sum += receivedMessages.get(j);
                }
                movingAverages.add(sum / k);
            }
        }
    }



    @Override
    public void receiveMeasures(int port, int timeout) throws SocketException {

        this.timeout = timeout;

        /* TODO: Create UDP socket and bind to local port 'port' */
	DatagramSocket socket = new DatagramSocket(port);

        boolean listen = true;
	int totalMessagesReceived = 0;


        System.out.println("[Field Unit] Listening on port: " + port);

        while (listen) {
	    DatagramPacket packet = new DatagramPacket(new byte[buffsize], buffsize);


            /* TODO: Receive until all messages in the transmission (msgTot) have been received or
                until there is nothing more to be received */

            /* TODO: If this is the first message, initialise the receive data structure before storing it. */

            /* TODO: Store the message */

            /* TODO: Keep listening UNTIL done with receiving  */
	    try {
	        socket.receive(packet);
	        String receivedData = new String(packet.getData(), 0, packet.getLength());
	        MessageInfo msg = new MessageInfo(receivedData);
	        addMessage(msg);
	        totalMessagesReceived++;
	        if (totalMessagesReceived >= msg.getTotalMessages()) {
                        listen = false;
                }
	        System.out.println("[Field Unit] Message " + msg.getMessageNum() + " out of " + msg.getTotalMessages() + " received. Value = " + msg.getMessage());
	    }catch (SocketTimeoutException e) {
                System.out.println("[Field Unit] Timeout while waiting for messages.");
                listen = false;
	    }catch (IOException e) {
                System.out.println("[Field Unit] Error while receiving message: " + e.getMessage());
                listen = false;
            }
	}
        /* TODO: Close socket  */
	    socket.close();

    }

    public static void main (String[] args) throws SocketException {
        if (args.length < 2) {
            System.out.println("Usage: ./fieldunit.sh <UDP rcv port> <RMI server HostName/IPAddress>");
            return;
        }

        /* TODO: Parse arguments */
	int port = Integer.parseInt(args[0]);
        String rmiAddress = args[1];

        /* TODO: Construct Field Unit Object */
	FieldUnit fieldUnit = new FieldUnit();


        /* TODO: Call initRMI on the Field Unit Object */
	 fieldUnit.initRMI(rmiAddress);



            /* TODO: Wait for incoming transmission */
	 fieldUnit.receiveMeasures(port, fieldUnit.timeout);

            /* TODO: Compute Averages - call sMovingAverage()
                on Field Unit object */
	 fieldUnit.sMovingAverage(7);

            /* TODO: Compute and print stats */
	 fieldUnit.printStats();

            /* TODO: Send data to the Central Serve via RMI and
             *        wait for incoming transmission again
             */
	 fieldUnit.sendAverages();

    }


    @Override
    public void initRMI (String address) {

        /* TODO: Initialise Security Manager (If JAVA version earlier than version 17) */
	if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        /* TODO: Bind to RMIServer */
	try {
            Registry registry = LocateRegistry.getRegistry(address);
            central_server = (ICentralServer) registry.lookup("CentralServer");
            System.out.println("[Field Unit] Connected to Central Server via RMI.");
        } catch (RemoteException | NotBoundException e) {
            System.err.println("[Field Unit] Error connecting to RMI server: " + e.getMessage());
        }


    }

    @Override
    public void sendAverages () {
        /* TODO: Attempt to send messages the specified number of times */
	System.out.println("[Field Unit] Sending SMAs to RMI.");
        try {
            for (int i = 0; i < movingAverages.size(); i++) {
                MessageInfo msg = new MessageInfo(movingAverages.size(), i + 1, movingAverages.get(i));
                central_server.receiveMsg(msg);
            }
        } catch (RemoteException e) {
            System.out.println("[Field Unit] Error while sending averages via RMI: " + e.getMessage());
        }
    }

    @Override
    public void printStats () {
      /* TODO: Find out how many messages were missing */

      /* TODO: Print stats (i.e. how many message missing?
       * do we know their sequence number? etc.) */

      /* TODO: Now re-initialise data structures for next time */
	int totalMessages = receivedMessages.size();
        int expectedMessages = receivedMessages.isEmpty() ? 0 : (int) receivedMessages.get(0);  // Get the total expected from the first message

        int missingMessages = expectedMessages - totalMessages;

        System.out.println("[Field Unit] Total Missing Messages = " + missingMessages + "out of " + expectedMessages);
    }


}

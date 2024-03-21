package NetworkApplication;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;

import java.util.HashMap;
import java.util.Objects;

public class ApplicationLayer implements EDProtocol {

    // Identifier of the transport layer
    private int transportPid;
    // Transport layer object
    private TransportLayer transport;
    // Identifier of the current layer (the application layer)
    private int mypid;
    // Node number
    private int nodeId;
    // Prefix of the layer (protocol variable name from the configuration file)
    private String prefix;
    private int index;
    // HashMap<key, value> = HashMap<nodeID, nodeIndex>
    private HashMap<Integer, Integer> rightNeighbour = null;
    private HashMap<Integer, Integer> leftNeighbour = null;


    public void setRightNeighbour(HashMap<Integer, Integer> rightNeighbour) {
        this.rightNeighbour = rightNeighbour;
    }

    public void setLeftNeighbour(HashMap<Integer, Integer> leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTransportLayer(int index) {
        this.transport = (TransportLayer) Network.get(index).getProtocol(this.transportPid);
    }

    public TransportLayer getTransport() {
        return transport;
    }

    public int getIndex() {
        return index;
    }

    public int getNodeId() {
        return nodeId;
    }

    public ApplicationLayer(String prefix) {
        this.prefix = prefix;
        // Initialization of identifiers from the configuration file
        this.transportPid = Configuration.getPid(prefix + ".transport");
        this.mypid = Configuration.getPid(prefix + ".myself");
        this.transport = null;
    }

    public void processEvent( Node node, int pid, Object receivedMessage) {

        String reqIndex = "reqIndex";
        String reqID = "reqID";
        String srcID = "srcID";
        String srcIndex = "srcIndex";

        HashMap message = (HashMap<String, Integer>) receivedMessage;
        switch ((int) message.get("type")) {
            case 0:     // -> Request to find its place
                System.out.println("case 0");
                setNeighbours((int) message.get(reqIndex), (int) message.get(reqID));
                break;
            case 1:     // -> Node insertion request
                System.out.println("case 1");
                if (message.get(srcID) == leftNeighbour.keySet().iterator().next()) {
                    setLeftNeighbourFromInt((int) message.get(reqID), (int) message.get(reqIndex));
                } else {
                    setRightNeighbourFromInt((int) message.get(reqID), (int) message.get(reqIndex));
                }
                break;
            case 2:    // -> Once the node is placed, inform other nodes of its arrival
                System.out.println("case 2");
                if ((int) message.get(srcID) < getNodeId()) {
                    setLeftNeighbourFromInt((int) message.get(srcID), (int) message.get(srcIndex));
                    setRightNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                } else if ((int) message.get(srcID) > getNodeId()) {
                    setLeftNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                    setRightNeighbourFromInt((int) message.get(srcID), (int) message.get(srcIndex));
                } else if ((int) message.get("newConnectionID") < getNodeId()) {
                    setRightNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                } else {
                    setLeftNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                }
                break;
            case 3:     // -> Set both neighbours
                System.out.println("case 3");
                setRightNeighbourFromInt((int) message.get(srcID), (int) message.get(srcIndex));
                setLeftNeighbourFromInt((int) message.get(srcID), (int) message.get(srcIndex));
                break;
            case 4:     // -> Set right neighbour
                System.out.println("case 4");
                setRightNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                break;
            case 5:     // -> Set left neighbour
                System.out.println("case 5");
                setLeftNeighbourFromInt((int) message.get("newConnectionID"), (int) message.get("newConnectionIndex"));
                break;
            case 6:     // -> Leave
                System.out.println("case 6");
                System.out.println("index : " + index + " LEAVING" + nodeId);
                leave();
                break;
            default:
                System.err.println("message type undefined");
        }
        System.out.println(message);
    }

    public void setNeighbours(int reqIndex, int reqID) {
        HashMap<String, Integer> message = new HashMap<>();
        message.put("srcIndex", index);
        message.put("srcID", getNodeId());
        message.put("reqIndex", reqIndex);
        message.put("reqID", reqID);
        Node nodeSrc = Network.get(index);

        if (isFirstNode()) { // case where there is only one node
            setRightNeighbourFromInt(reqID, reqIndex);
            setLeftNeighbourFromInt(reqID, reqIndex);
            message.put("type", 3);
            transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0);
        } else if (reqID < getNodeId()) { // if we are not the first node, and the request needs to be sent left
            if (isDHTBeginning() || reqID > leftNeighbour.keySet().iterator().next()) { // case where we are at the beginning of the DHT -> insert the node directly to the left

                message.put("type", 1);
                transport.send(nodeSrc, Network.get(leftNeighbour.values().iterator().next()), new HashMap<>(message), 0);

                message.put("type", 2);
                message.put("newConnectionIndex", leftNeighbour.values().iterator().next());
                message.put("newConnectionID", leftNeighbour.keySet().iterator().next());
                transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0); // send the order to the node wishing to insert itself
                setLeftNeighbourFromInt(reqID, reqIndex);
            } else {
                message.put("type", 0);
                transport.send(nodeSrc, Network.get(leftNeighbour.values().iterator().next()), new HashMap<>(message), 0);
            }
        } else { // if we are not the first node, and the request needs to be sent right
            if (isDHTEnding() || reqID < rightNeighbour.keySet().iterator().next()) { // case where we are at the end of the DHT -> insert the node directly to the right

                message.put("type", 1);
                transport.send(nodeSrc, Network.get(rightNeighbour.values().iterator().next()), new HashMap<>(message), 0);

                message.put("type", 2);
                message.put("newConnectionIndex", rightNeighbour.values().iterator().next());
                message.put("newConnectionID", rightNeighbour.keySet().iterator().next());
                transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0); // send the order to the node wishing to insert itself
                setRightNeighbourFromInt(reqID, reqIndex);
            } else {
                message.put("type", 0);
                transport.send(nodeSrc, Network.get(rightNeighbour.values().iterator().next()), new HashMap<>(message), 0);
            }
        }
        message.put("type", 0);
    }

    public void leave() {
        if ((rightNeighbour != null && leftNeighbour != null) && !isFirstNode()) {
            Node nodeSrc = Network.get(index);
            HashMap<String, Integer> message = new HashMap<>();
            message.put("srcIndex", index);
            message.put("srcID", getNodeId());

            message.put("newConnectionIndex", rightNeighbour.values().iterator().next());
            message.put("newConnectionID", rightNeighbour.keySet().iterator().next());
            message.put("type", 4);
            transport.send(nodeSrc, Network.get(leftNeighbour.values().iterator().next()), new HashMap<>(message), 0); // set the right neighbor of our left neighbor

            message.put("newConnectionIndex", leftNeighbour.values().iterator().next());
            message.put("newConnectionID", leftNeighbour.keySet().iterator().next());
            message.put("type", 5);
            transport.send(nodeSrc, Network.get(rightNeighbour.values().iterator().next()), new HashMap<>(message), 0); // set the left neighbor of our right neighbor

            setLeftNeighbourFromInt(nodeId, index);
            setRightNeighbourFromInt(nodeId, index);
        }
    }

    public void setRightNeighbourFromInt(int nodeId, int nodeIndex) {
        HashMap<Integer, Integer> newHashMap = new HashMap<>();
        newHashMap.put(nodeId, nodeIndex);
        setRightNeighbour(newHashMap);
        DisplayDHTTask.displayDHT(); // Assuming DisplayDHTTask.displayDHT() is a method that displays the distributed hash table
    }

    public void setLeftNeighbourFromInt(int nodeId, int nodeIndex) {
        HashMap<Integer, Integer> newHashMap = new HashMap<>();
        newHashMap.put(nodeId, nodeIndex);
        setLeftNeighbour(newHashMap);
        DisplayDHTTask.displayDHT(); // Assuming DisplayDHTTask.displayDHT() is a method that displays the distributed hash table
    }

    // Check if this is the first node in the DHT
    private boolean isFirstNode() {
        return Objects.equals(leftNeighbour.keySet().iterator().next(), rightNeighbour.keySet().iterator().next()) && rightNeighbour.keySet().iterator().next() == getNodeId();
    }

    // Check if this node is at the beginning of the DHT
    private boolean isDHTBeginning() {
        return leftNeighbour.keySet().iterator().next() > nodeId;
    }

    // Check if this node is at the end of the DHT
    private boolean isDHTEnding() {
        return rightNeighbour.keySet().iterator().next() < nodeId;
    }

    // Necessary method for network creation (which is done by cloning a prototype)
    public Object clone() {
        ApplicationLayer dolly = new ApplicationLayer(this.prefix);
        return dolly;
    }

    // Method to print neighbours (for debugging purposes)
    public void printNeighbours() {
        System.out.println("Left neighbor: " + leftNeighbour + " | Node ID: " + nodeId + " | Right neighbor: " + rightNeighbour);
    }
}

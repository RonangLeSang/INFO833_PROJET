package NetworkApplication;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;

import java.util.HashMap;
import java.util.Objects;

public class ApplicationLayer implements EDProtocol {
    
    //identifiant de la couche transport
    private int transportPid;

    //objet couche transport
    private TransportLayer transport;

    //identifiant de la couche courante (la couche applicative)
    private int mypid;

    //le numero de noeud
    private int nodeId;

    //prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;
    private int index;

    //key : nodeID
    //value : nodeIndex
    private HashMap<Integer, Integer> rightNeighbour = null;

    private HashMap<Integer, Integer> leftNeighbour = null;

    public HashMap<Integer, Integer> getRightNeighbour() {
        return rightNeighbour;
    }

    public HashMap<Integer, Integer> getLeftNeighbour() {
        return leftNeighbour;
    }

    public ApplicationLayer(String prefix) {
        this.prefix = prefix;
        //initialisation des identifiants a partir du fichier de configuration
        this.transportPid = Configuration.getPid(prefix + ".transport");
        this.mypid = Configuration.getPid(prefix + ".myself");
        this.transport = null;
    }

    public TransportLayer getTransport() {
        return transport;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    //methode appelee lorsqu'un message est recu par le protocole ApplicationLayer du noeud

    // type du message :
    // 0 -> requête pour trouver sa place
    // 1 -> requête d'insertion de noeuds
    // 2 -> une fois le noeud placé, oon informe les autres noeuds de son arrivé
    // 3 -> setBothNeighbours
    public void processEvent( Node node, int pid, Object receivedMessage) {

        String reqIndex = "reqIndex";
        String reqID = "reqID";
        String srcID = "srcID";
        String srcIndex = "srcIndex";

        HashMap message = (HashMap)receivedMessage;
//        System.out.println("recu : " + message);
//        System.out.println("message type : " + (int)message.get("type"));
        switch ((int)message.get("type")){
            case 0:
                System.out.println("case 0");
                setNeighbours((int)message.get(reqIndex), (int)message.get(reqID));
                break;
            case 1:
                System.out.println("case 1");
                if (message.get(srcID)==leftNeighbour.keySet().iterator().next()) {
                    setLeftNeighbourFromInt((int)message.get(reqID), (int)message.get(reqIndex));
                } else {
                    setRightNeighbourFromInt((int)message.get(reqID), (int)message.get(reqIndex));
                }
                break;
            case 2:
                System.out.println("case 2");
                if ((int)message.get(srcID) < getNodeId()) {
                    setLeftNeighbourFromInt((int)message.get(srcID), (int)message.get(srcIndex));
                    setRightNeighbourFromInt((int)message.get("newConnectionID"), (int)message.get("newConnectionIndex"));
                } else {
                    setLeftNeighbourFromInt((int)message.get("newConnectionID"), (int)message.get("newConnectionIndex"));
                    setRightNeighbourFromInt((int)message.get(srcID), (int)message.get(srcIndex));
                }
                break;
            case 3:
                System.out.println("case 3");
                setRightNeighbourFromInt((int)message.get(srcID), (int)message.get(srcIndex));
                setLeftNeighbourFromInt((int)message.get(srcID), (int)message.get(srcIndex));
                printNeighbours();
            default:
                System.err.println("message type undefined");
        }

        DisplayDHTTask displayDHTTask = new DisplayDHTTask();
        displayDHTTask.execute();

    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setRightNeighbourFromInt( int nodeId, int nodeIndex){
        System.out.println("right neighbour set");
        printNeighbours();
        HashMap<Integer, Integer> newHashMap = new HashMap<>();
        newHashMap.put(nodeId, nodeIndex);
        setRightNeighbour(newHashMap);
    }

    public void setLeftNeighbourFromInt( int nodeId, int nodeIndex){
        System.out.println("left neighbour set");
        printNeighbours();
        HashMap<Integer, Integer> newHashMap = new HashMap<>();
        newHashMap.put(nodeId, nodeIndex);
        setLeftNeighbour(newHashMap);
    }

    public void setRightNeighbour(HashMap<Integer, Integer> rightNeighbour) {
        this.rightNeighbour = rightNeighbour;
    }

    public void setLeftNeighbour(HashMap<Integer, Integer> leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    private boolean isFirstNode(){
//        System.out.println(" | " + leftNeighbour.keySet().iterator().next() + " | " + rightNeighbour.keySet().iterator().next() + " | " + getNodeId());
        return Objects.equals(leftNeighbour.keySet().iterator().next(), rightNeighbour.keySet().iterator().next()) && rightNeighbour.keySet().iterator().next() == getNodeId();
    }

    private boolean isDHTBeginning(){
        return leftNeighbour.keySet().iterator().next() > nodeId;
    }

    private boolean isDHTEnding(){
        return rightNeighbour.keySet().iterator().next() < nodeId;
    }

    public void setNeighbours(int reqIndex, int reqID) {
        HashMap<String, Integer> message = new HashMap<>();
        message.put("srcIndex", index);
        message.put("srcID", getNodeId());
        message.put("reqIndex", reqIndex);
        message.put("reqID", reqID);
        Node nodeSrc = Network.get(index);

        // fait
        if (isFirstNode()){ // cas ou on a une seule node
            setRightNeighbourFromInt(reqID, reqIndex);
            setLeftNeighbourFromInt(reqID, reqIndex);
            message.put("type", 3);
            transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0);

        } else if (reqID < getNodeId()) { // si on est pas le premier noeud, et que il faut envoyer la requête à gauche
            if (isDHTBeginning() || reqID > leftNeighbour.keySet().iterator().next()) { // cas ou on est au début de la DHT -> on insère le noeud directement à gauche

                message.put("type", 1);
                transport.send(nodeSrc, Network.get(leftNeighbour.values().iterator().next()), new HashMap<>(message), 0);

                message.put("type", 2);
                message.put("newConnectionIndex", leftNeighbour.values().iterator().next());
                message.put("newConnectionID", leftNeighbour.keySet().iterator().next());
                transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0); // on envoi au noeud souhaitant s'insérer l'ordre de le faire
                setLeftNeighbourFromInt(reqID, reqIndex);
            } else {
                message.put("type", 0);
                transport.send(nodeSrc, Network.get(leftNeighbour.values().iterator().next()), new HashMap<>(message), 0);
            }
        } else { // si on est pas le premier noeud, et que il faut envoyer la requête à droite
            System.out.println("droite");
            if (isDHTEnding() || reqID < rightNeighbour.keySet().iterator().next()) { // cas ou on est au début de la DHT -> on insère le noeud directement à gauche

                message.put("type", 1);
                transport.send(nodeSrc, Network.get(rightNeighbour.values().iterator().next()), new HashMap<>(message), 0);

                message.put("type", 2);
                message.put("newConnectionIndex", rightNeighbour.values().iterator().next());
                message.put("newConnectionID", rightNeighbour.keySet().iterator().next());
                transport.send(nodeSrc, Network.get(reqIndex), new HashMap<>(message), 0); // on envoi au noeud souhaitant s'insérer l'ordre de le faire
                setRightNeighbourFromInt(reqID, reqIndex);
            } else {
                message.put("type", 0);
                transport.send(nodeSrc, Network.get(rightNeighbour.values().iterator().next()), new HashMap<>(message), 0);
            }
        }
        message.put("type", 0);
    }

    //methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
    public Object clone() {
        ApplicationLayer dolly = new ApplicationLayer(this.prefix);
        return dolly;
    }

    //liaison entre un objet de la couche applicative et un 
    //objet de la couche transport situes sur le meme noeud
    public void setTransportLayer(int index) {
        this.transport = (TransportLayer) Network.get(index).getProtocol(this.transportPid);
    }

    //envoi d'un message (l'envoi se fait via la couche transport)
//    public void send(Message msg, Node dest) {
//	this.transport.send(getMyNode(), dest, msg, this.mypid);
//    }

    //affichage a la reception
    private void receive(Object msg) {
        System.out.println(this + ": Received " + msg);
    }

    //retourne le noeud courant
    private Node getMyNode() {
        return Network.get(this.nodeId);
    }

    public int getMypid() {
        return mypid;
    }

    public void printNeighbours(){
        System.out.println("voisin de gauche : " + leftNeighbour + " | node ID : " + nodeId + " | voisin de droite : " + rightNeighbour);
    }



}
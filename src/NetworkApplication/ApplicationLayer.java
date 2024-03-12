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

    private HashMap<Integer, Integer> rightNeighbour;

    private HashMap<Integer, Integer> leftNeighbour;


    public ApplicationLayer(String prefix) {
	this.prefix = prefix;
	//initialisation des identifiants a partir du fichier de configuration
	this.transportPid = Configuration.getPid(prefix + ".transport");
	this.mypid = Configuration.getPid(prefix + ".myself");
	this.transport = null;
    }

    //methode appelee lorsqu'un message est recu par le protocole ApplicationLayer du noeud
    public void processEvent( Node node, int pid, Object event ) {
//        this.receive((Object)event);

        HashMap nodeTmp = (HashMap)event;
        System.out.println(nodeTmp);
//        ApplicationLayer appTmp = (ApplicationLayer)Network.get(nodeTmp.get("reqIndex")).getProtocol(0);
//        System.out.println("message reçu de : " + appTmp.getNodeId() + " sur : " + nodeId + " contenu : " + event);
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setRightNeighbourFromInt( int nodeId, int nodeIndex){
        HashMap<Integer, Integer> newHashMap = new HashMap<>();
        newHashMap.put(nodeId, nodeIndex);
        setRightNeighbour(newHashMap);
    }

    public void setLeftNeighbourFromInt( int nodeId, int nodeIndex){
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
        return Objects.equals(leftNeighbour.keySet().iterator().next(), rightNeighbour.keySet().iterator().next()) && rightNeighbour.keySet().iterator().next() == getNodeId();
    }

    public void setNeighbours(int srcIndex, int reqIndex, int reqID) {
        HashMap<String, Integer> message = new HashMap<>();
        message.put("srcIndex", srcIndex);
        message.put("srcID", getNodeId());
        message.put("reqIndex", reqIndex);
        message.put("reqID", reqID);
        int destIndex = srcIndex;

        if (isFirstNode()){
            HashMap
            leftNeighbour;
        } else if (reqID < getNodeId()) {
                destIndex = leftNeighbour.values().iterator().next();
                if (reqID > leftNeighbour.keySet().iterator().next()) {
                    ;
                }
            } else {
                destIndex = rightNeighbour.values().iterator().next();
            }
        System.out.println("sout : " + leftNeighbour.values().iterator().next());
        transport.send(Network.get(srcIndex), Network.get(destIndex), message, 0);
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




}
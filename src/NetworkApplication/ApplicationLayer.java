package NetworkApplication;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;

import java.util.HashMap;

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

        Node nodeTmp = (Node)event;
        ApplicationLayer appTmp = (ApplicationLayer)nodeTmp.getProtocol(0);
        System.out.println("message reçu de : " + appTmp.getNodeId() + " sur : " + nodeId + " contenu : " + event);
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setRightNeighbour(HashMap<Integer, Integer> rightNeighbour) {
        this.rightNeighbour = rightNeighbour;
    }

    public void setLeftNeighbour(HashMap<Integer, Integer> leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    public void setNeighbours(Node node, int SrcIndex) {
        HashMap<String, Integer> message = new HashMap<>();
        message.put("srcIndex", SrcIndex);
        message.put("srcID", getNodeId());
        System.out.println( leftNeighbour.keySet().iterator().next());
        if (SrcIndex < nodeId) {
            int reqIndex = leftNeighbour.keySet().iterator().next();
            message.put("reqIndex", reqIndex);
            message.put("reqID", leftNeighbour.get(reqIndex));
            //A changer
            transport.send(Network.get(0), Network.get(5), Network.get(0), 0);
        } else {
            transport.send(getMyNode(), Network.get(rightNeighbour.get("index")), new Message(Message.HELLOWORLD, "Hello!!"), this.mypid);
        }
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
    public void send(Message msg, Node dest) {
	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

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
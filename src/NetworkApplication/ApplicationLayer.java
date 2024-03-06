package NetworkApplication;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;

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

    private int rightNeighbour;

    private int leftNeighbour;


    public ApplicationLayer(String prefix) {
	this.prefix = prefix;
	//initialisation des identifiants a partir du fichier de configuration
	this.transportPid = Configuration.getPid(prefix + ".transport");
	this.mypid = Configuration.getPid(prefix + ".myself");
	this.transport = null;
    }

    //methode appelee lorsqu'un message est recu par le protocole ApplicationLayer du noeud
    public void processEvent( Node node, int pid, Object event ) {
        this.receive((Message)event);
//        System.out.println(Network.get(0).getIndex());
//        System.out.println("pid : " + mypid);
//        System.out.println("id : " + nodeId);
        this.send((Message)event, Network.get(rightNeighbour));
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setRightNeighbour(int index) {
        try{
            this.rightNeighbour = Network.get(index + 1).getIndex();
        }catch(ArrayIndexOutOfBoundsException e){
            this.rightNeighbour = Network.get(0).getIndex();
        }
    }


    public void setLeftNeighbour(int index) {
        try{
            this.leftNeighbour = Network.get(index - 1).getIndex();
        }catch(ArrayIndexOutOfBoundsException e){
            this.leftNeighbour = Network.get(Network.size() - 1).getIndex();
        }
    }

    //methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
    public Object clone() {

	ApplicationLayer dolly = new ApplicationLayer(this.prefix);

	return dolly;
    }

    //liaison entre un objet de la couche applicative et un 
    //objet de la couche transport situes sur le meme noeud
    public void setTransportLayer(int nodeId) {
	this.nodeId = nodeId;
	this.transport = (TransportLayer) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    //envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg, Node dest) {
	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    //affichage a la reception
    private void receive(Message msg) {
	System.out.println(this + ": Received " + msg.getContent());
    }

    //retourne le noeud courant
    private Node getMyNode() {
	return Network.get(this.nodeId);
    }

    public String toString() {
	return "Node "+ this.nodeId;
    }

    
}
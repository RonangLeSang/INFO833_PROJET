package NetworkApplication;

import peersim.core.*;
import peersim.config.*;

import java.util.Random;

/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
 */
public class Initializer implements peersim.core.Control {
    
    private int helloWorldPid;

    public Initializer(String prefix) {
	//recuperation du pid de la couche applicative
	this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
    }

	private void setNeighbours(ApplicationLayer node){
		ApplicationLayer current;
		Node dest;
		dest = Network.get(0);
		current = (ApplicationLayer)dest.getProtocol(this.helloWorldPid);
		if(node.getNodeId() < current.getNodeId()){
			;
		}else{
			;
		}
	}

    public boolean execute() {
	int nodeNb;
	ApplicationLayer emitter, current;
	Node dest;
	Message helloMsg;

	//recuperation de la taille du reseau
	nodeNb = Network.size();
	//creation du message
	helloMsg = new Message(Message.HELLOWORLD,"Hello!!");
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}

	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	emitter = (ApplicationLayer)Network.get(0).getProtocol(this.helloWorldPid);
	emitter.setTransportLayer(0);

	Random random = new Random();
	for (int i = 0; i < nodeNb; i++) {
		int randomNumber = random.nextInt(1000, 9999);
		System.out.println(randomNumber);
		dest = Network.get(i);
		current = (ApplicationLayer)dest.getProtocol(this.helloWorldPid);
		current.setNodeId(randomNumber);
	}

	ApplicationLayer firstNode = (ApplicationLayer) Network.get(1);
	firstNode.setRightNeighbour(0);
	firstNode.setLeftNeighbour(0);

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
	//puis on fait envoyer au noeud 0 un message "Hello"
	for (int i = 1; i < nodeNb; i++) {
	    dest = Network.get(i);
	    current = (ApplicationLayer)dest.getProtocol(this.helloWorldPid);
	    current.setTransportLayer(i);

		setNeighbours(current);
	}

	emitter.send(helloMsg, Network.get(0));

	System.out.println("Initialization completed");
	return false;
    }
}
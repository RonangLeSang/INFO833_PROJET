package NetworkApplication;

import peersim.core.*;
import peersim.config.*;

import java.util.HashMap;
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

    public boolean execute() {
	int nodeNb;
	ApplicationLayer ApNodeInit, currentApp;
	Node currentNode;
	Message helloMsg;

	nodeNb = Network.size();
	//creation du message
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}

	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	ApNodeInit = (ApplicationLayer)Network.get(0).getProtocol(this.helloWorldPid);
	ApNodeInit.setTransportLayer(0);

	//creation des ID de nodes
	Random random = new Random();
	for (int i = 0; i < nodeNb; i++) {
		int randomNumber = random.nextInt(1000, 9999);
		currentNode = Network.get(i);
		currentApp = (ApplicationLayer)currentNode.getProtocol(this.helloWorldPid);
		currentApp.setNodeId(randomNumber);
	}

	HashMap<Integer, Integer> firstNeighbour = new HashMap<>();
	ApplicationLayer app = (ApplicationLayer)Network.get(0).getProtocol(0);
	firstNeighbour.put(app.getNodeId(), 0);

	ApNodeInit.setRightNeighbour(firstNeighbour);
	ApNodeInit.setLeftNeighbour(firstNeighbour);

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
	//puis on fait envoyer au noeud 0 un message "Hello"
	for (int nodeIndex = 1; nodeIndex < nodeNb; nodeIndex++) {
	    currentNode = Network.get(nodeIndex);
	    currentApp = (ApplicationLayer)currentNode.getProtocol(0);
	    currentApp.setTransportLayer(nodeIndex);

		HashMap<Integer, Integer> hashmapInit;
		currentApp.setRightNeighbour(firstNeighbour);
		currentApp.setLeftNeighbour(firstNeighbour);
		ApNodeInit.setNeighbours(0, nodeIndex, currentApp.getNodeId());
	}
//	TransportLayer tmp = (TransportLayer)Network.get(1).getProtocol(1);


	System.out.println("Initialization completed");
	return false;
    }
}
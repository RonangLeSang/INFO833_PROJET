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

	public void initFirstNeighbours(ApplicationLayer apNodeInit){
		HashMap<Integer, Integer> firstNeighbour = new HashMap<>();
		ApplicationLayer app = (ApplicationLayer)Network.get(0).getProtocol(0);
		firstNeighbour.put(app.getNodeId(), 0);

		apNodeInit.setRightNeighbour(firstNeighbour);
		apNodeInit.setLeftNeighbour(firstNeighbour);
	}

	public void setNodeID(){
		int nodeNb = Network.size();
		Node currentNode;
		ApplicationLayer currentApp;
		Random random = new Random();
		for (int i = 0; i < nodeNb; i++) {
			int randomNumber = random.nextInt(1000, 9999);
			currentNode = Network.get(i);
			currentApp = (ApplicationLayer)currentNode.getProtocol(this.helloWorldPid);
			currentApp.setNodeId(randomNumber);
		}
	}

	public void setTransportAndNeighbours(ApplicationLayer apNodeInit){
		int nodeNb = Network.size();
		Node currentNode;
		ApplicationLayer currentApp;
		for (int nodeIndex = 1; nodeIndex < nodeNb; nodeIndex++) {
			currentNode = Network.get(nodeIndex);
			currentApp = (ApplicationLayer)currentNode.getProtocol(0);
			currentApp.setTransportLayer(nodeIndex);

			HashMap<Integer, Integer> hashmapInit;
			apNodeInit.setNeighbours(0, nodeIndex, currentApp.getNodeId());

			displayDHT();
		}
	}

	public void displayDHT(){
		int nodeNb = Network.size();
		ApplicationLayer currentApp;
		Node currentNode;
		for(int nodeIndex = 0; nodeIndex < nodeNb; nodeIndex++){
			System.out.print("index : " + nodeIndex + " | ");
			currentNode = Network.get(nodeIndex);
			currentApp = (ApplicationLayer)currentNode.getProtocol(0);
			currentApp.printNeighbours();
		}
		System.out.println("-------------------------");
	}

    public boolean execute() {
	int nodeNb;
	ApplicationLayer apNodeInit, currentApp;
	Node currentNode;
	Message helloMsg;

	nodeNb = Network.size();
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}

	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	apNodeInit = (ApplicationLayer)Network.get(0).getProtocol(this.helloWorldPid);
	apNodeInit.setTransportLayer(0);

	setNodeID();
	initFirstNeighbours(apNodeInit);

	setTransportAndNeighbours(apNodeInit);
//	TransportLayer tmp = (TransportLayer)Network.get(1).getProtocol(1);


	System.out.println("Initialization completed");
	return false;
    }
}
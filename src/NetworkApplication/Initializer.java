package NetworkApplication;

import peersim.core.*;
import peersim.config.*;

import java.util.HashMap;
import java.util.Random;
import peersim.edsim.EDSimulator;

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
		HashMap<String, Integer> msg = new HashMap<>();
		msg.put("type", 0);
		EDSimulator.add(0, msg, Network.get(0), 0);
		int nodeNb = Network.size();
		Node currentNode;
		ApplicationLayer currentApp;

		for (int nodeIndex = 1; nodeIndex < nodeNb; nodeIndex++) {
			currentNode = Network.get(nodeIndex);
			currentApp = (ApplicationLayer)currentNode.getProtocol(0);
			currentApp.setTransportLayer(nodeIndex);
			currentApp.setLeftNeighbourFromInt(currentApp.getNodeId(), nodeIndex);
			currentApp.setRightNeighbourFromInt(currentApp.getNodeId(), nodeIndex);

			msg.put("srcIndex", nodeIndex);
			msg.put("srcID", currentApp.getNodeId());
			msg.put("reqIndex", nodeIndex);
			msg.put("reqID", currentApp.getNodeId());
			System.out.println("base msg : " + msg);

//			apNodeInit.setNeighbours(0, nodeIndex, currentApp.getNodeId());
			apNodeInit.getTransport().send(currentNode, Network.get(0), new HashMap<>(msg), 0);

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
	ApplicationLayer apNodeInit;

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
	System.out.println("Initialization completed");
	displayDHT();
	return false;
    }
}


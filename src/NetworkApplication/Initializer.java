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
	ApplicationLayer emitter, current;
	Node dest;
	Message helloMsg;

	nodeNb = Network.size();
	//creation du message
	helloMsg = new Message(Message.HELLOWORLD,"");
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}

	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	emitter = (ApplicationLayer)Network.get(0).getProtocol(this.helloWorldPid);
	emitter.setTransportLayer(0);

	//creation des ID de nodes
	Random random = new Random();
	for (int i = 0; i < nodeNb; i++) {
		int randomNumber = random.nextInt(1000, 9999);
		System.out.println(randomNumber);
		dest = Network.get(i);
		current = (ApplicationLayer)dest.getProtocol(this.helloWorldPid);
		current.setNodeId(randomNumber);
	}

	HashMap<Integer, Integer> rightNeighbour = new HashMap<>();
	ApplicationLayer app = (ApplicationLayer)Network.get(0).getProtocol(0);
	rightNeighbour.put(app.getNodeId(), 0);

	emitter.setRightNeighbour(rightNeighbour);
	emitter.setLeftNeighbour(rightNeighbour);

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
	//puis on fait envoyer au noeud 0 un message "Hello"
	for (int i = 1; i < nodeNb; i++) {
	    dest = Network.get(i);
	    current = (ApplicationLayer)dest.getProtocol(0);
	    current.setTransportLayer(i);
	}
//	dest.setNeighbours(Network.get(1));
	TransportLayer tmp = (TransportLayer)Network.get(1).getProtocol(1);
	tmp.send(Network.get(0), Network.get(5), Network.get(0), 0);


	System.out.println("Initialization completed");
	return false;
    }
}
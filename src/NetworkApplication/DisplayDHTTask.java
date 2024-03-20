package NetworkApplication;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DisplayDHTTask implements Control {

    public boolean execute() {
        displayDHT();
        return false; // Assuming you don't want this task to be repeatedly executed
    }

    public static void displayDHT(){
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
}
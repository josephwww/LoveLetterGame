package agents;
import java.util.*;

import loveletter.*;

public class Node {
	Action move;
	Node parent;
	ArrayList<Node> child;
	int wins;
	int visits;
	int avails;
	int playerJustMoved;
	
	/**
	   * Constructs a node of a ISMCTS tree.
	   * @param parent the parent node of the new node.
	   * @param move the move that got us to this node
	   * **/
	Node(Node parent, Action move){
		this.move=move; //the move that got us to this node - "None" for the root node
		this.parent = parent;//"None" for the root node
		child = new ArrayList<Node>();
		wins=0;
		visits=0;
		avails=0;
		playerJustMoved=-1;
	}
	
	
	/**
	   * Plays a game of LoveLetter
	   * @param allPossibleMove the number of action can be done by the current state
	   * @return an arraylist included the action which didn't try
	   * **/
	public ArrayList<Action> GetUntriedMoves(ArrayList<Action> allPossibleMove) {
		
		for(Node a: this.child) {
			allPossibleMove.remove(a.move);
		}
		return allPossibleMove;
	}
	
	/**
	   * use the UCB1 formula to find a most interest node to simulate
	   * @param allMoves the number of action can be done by the current state
	   * @return a most interesting node
	   * **/
	public Node UCBSelectChild(ArrayList<Action> allMoves) {
		double exploration =0.7;
		List<Node> legalChildren=new LinkedList<Node>();
		
		//Filter the list of children by the list of legal moves
		for(Node a: this.child) {
			if(allMoves.contains(a.move)) {
				legalChildren.add(a);
			}
		}
		
		//Get the child with the highest UCB score
		Node max=null;
		double asd = 0;
		for(Node c: legalChildren) {
			double a=(float)(c.wins)/(float)(c.visits) + exploration * Math.sqrt(Math.log(c.avails)/(float)(c.visits));
			if(a>asd) {
				asd=a;
				max=c;
			}
		}
		
		for(Node c: legalChildren) {
			if(this.child.contains(c)) {
				this.avails++;
			}
		}
		
		return max;
	}
	
	/**
	   * add a new node to the ISMCTS tree
	   * @param act the move that got us to this node
	   * @param lastplay the parent index
	   * @return the new child node
	   * **/
	public Node AddChild(Action act, int lastplay) {
		Node node = new Node(this,act);
		node.playerJustMoved=lastplay;
		this.child.add(node);
		return node;
	}
	
	/**
	   * update the information in the node
	   * @param state the current state
	   * **/
	public void Update(MyState state) {
		this.visits++;
		if(this.parent!=null) {
			if(state.roundWinner()==this.playerJustMoved) {
				this.wins++;
			}
		}
	}
}

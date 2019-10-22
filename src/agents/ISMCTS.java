package agents;
import java.util.*;

import loveletter.*;

public class ISMCTS {
	State state;
	private int itermax;
	Card draw;
	
	/**
	   * Constructs a ISMCTS algorithm.
	   * @param state the current state
	   * @param itermax the time we want to iterate
	   * @param draw the card of observer drawn from the deck
	   * **/
	ISMCTS(State state, int itermax, Card draw){
		this.state=state;
		this.itermax=itermax;
		this.draw=draw;
	}
	
	/**
	 * the function exists in class ISMCTS. 
	 * It is used to do the five steps in ISMCTS
	 * (determinization, selection, expansion, simulation, backpropagation)
	 * @return the action object
	 * @throws IllegalActionException if an illegal action.
	 * **/
	public Action processing() throws IllegalActionException {
		Node rootnode = new Node(null,null);
		
		for(int i=0;i<itermax;i++) {
			Node node = rootnode;
			
			//Determinize
			MyState st = new MyState(state,draw);
					
			//select
			while(st.GetMoves()!=null && node.GetUntriedMoves(st.GetMoves())==null) {
				node = node.UCBSelectChild(st.GetMoves());
				st.update(node.move);
			}
			
			//Expand
			ArrayList<Action> untriedMoves = node.GetUntriedMoves(st.GetMoves());
			if(untriedMoves.size()>0) {
				Action m=null;
				Random random = new Random();
				m=untriedMoves.get(random.nextInt(untriedMoves.size()));
				st.update(m);
				node = node.AddChild(m, st.getIndex());
			}
			
			//simulate
			while(!st.roundOver()) {
				Action m=null;
				Random random = new Random();
				m=st.GetMoves().get(random.nextInt(st.GetMoves().size()));
				st.update(m);
			}
			
			//Backpropogation
			while(node != null) {
				node.Update(st);
				node = node.parent;
			}
			
		}
		Node best=null;
		int i=0;
		for(Node a:rootnode.child) {
			if(a.visits>i) {
				best=a;
			}
		}
		
		return best.move;
	}
	
}

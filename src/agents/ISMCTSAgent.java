package agents;
import java.util.Random;

import loveletter.Action;
import loveletter.Agent;
import loveletter.Card;
import loveletter.IllegalActionException;
import loveletter.State;

/**
 * Author:Hongfeng Wang 22289267
 *        Haoran Zhang  22289211
 * The ISMCTS agent
 */
public class ISMCTSAgent implements Agent{
	
	private Random rand;
	private State current;
	private int myIndex;
	
	
	//0 place default constructor
	public ISMCTSAgent(){
	    rand  = new Random();
	}
	
	/**
	   * Reports the agent's name
	   * */
	public String toString(){
		return "ricky";
	}
	
	/**
	   * Method called at the start of a round
	   * @param start the initial state of the round
	   **/
	public void newRound(State start) {
		// TODO Auto-generated method stub
		current= start;
		myIndex = current.getPlayerIndex();
	}

	/**
	   * Method called when any agent performs an action. 
	   * @param act the action an agent performs
	   * @param results the state of play the agent is able to observe.
	   * **/
	public void see(Action act, State results) {
		// TODO Auto-generated method stub
		current=results;
	}
	
	
	/**
	   * Perform an action after drawing a card from the deck
	   * @param c the card drawn from the deck
	   * @return the action the agent chooses to perform
	   * @throws IllegalActionException when the Action produced is not legal.
	   * */
	public Action playCard(Card c) {	
		ISMCTS ism = new ISMCTS(current, 1000, c);
		Action act=null;
		try {
			act= ism.processing();
		}  catch (IllegalActionException e) {
			e.printStackTrace();
		}
		return act;
	}

}

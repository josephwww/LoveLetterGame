package agents;

import java.util.*;

import loveletter.*;

public class MyState implements Cloneable{
	private int player;//the player who observes this outcome, or -1 for the game engine
	private int num; //The number of players in the game
	private Card[] hand=new Card[4]; //the cards players currently hold, or null if the player has been eliminated 
	private List<Card> deck=new ArrayList<Card>(); //the deck of remaining cards
	private int nextPlayer; //the index of the next player to draw a card (using Object reference so value is shared).
	private Card[] myHand = new Card[2];//the observer player have 2 cards in his hand
	private boolean handmaid[]= {false,false,false,false};//to indicate which player is protected by the maid
	
	/**
	   * Constructs a clone state of the current state
	   * @param state the current state
	   * @param card the card drawn by the observer
	   * **/
  	MyState(State state, Card card){
  		this.player=state.getPlayerIndex();
  		this.num=state.numPlayers();
  		this.myHand[0]=state.getCard(this.player);
  		this.myHand[1]=card;

  		this.nextPlayer = (player+1)%num;
        while(state.eliminated(nextPlayer)) nextPlayer = (nextPlayer+1)%num; //find the alive player
  		Card[] initial=state.unseenCards();
  		deck.addAll(Arrays.asList(initial)); 
  		deck.remove(myHand[0]);//delete the card initially in my hand
  		
  		Collections.shuffle(deck);//shuffle the card
  		hand[player]=myHand[0];
  		for(int i=0;i<this.num;i++) {
  			if(state.eliminated(i)) {hand[i]=null;continue;} //if the player die
			if(i==player) continue;
  			hand[i]=deck.remove(0);
  		}
  		
  		// find out whether the player was protected by handmaid
  		for(int i=0;i<4;i++) {
  			if(state.handmaid(i))
  				this.handmaid[i]=true;
  		}
  	}
  	
  	/**
	   * get the player index of the observer
	   * @return the player index of the observer
	   * **/
  	public int getIndex() {return this.player;}

  	/**
	   * judge the player if eliminated
	   * @param play the player index
	   * @return true for eliminated false for not
	   * **/
  	public boolean eliminated(int play){
  	    return hand[play]==null;
  	  }
   
  	/**
     * checks to see if agent a targetting agent t, with card c, whilst holding card d is a legal action.
     * That is 
     * a) the player a must hold card c, 
     * b) it must be player a's turn
     * c) if the player holds the Countess, they cannot play the Prince or the King
     * d) if the action has a target, they cannot be eliminated
     * e) if the target is protected by the Handmaid and their is some player other than the target and a not protected, 
     *    then that player must be targetted instead. 
     * f) if all players are protected by the Handmaid and the player a plays a Prince, they must target themselves
     * @param a the index of the playing agent
     * @param t the index of the targeted player or -1, of no such target exists
     * @param c the card played 
     * @param drawn the card drawn
     * @throws IllegalActionException if any of these conditions hold.
     * **/      
    private void legalAction(int a, int t, Card c, Card drawn) throws IllegalActionException{
      if(hand[a]!=c && drawn!=c)
        throw new IllegalActionException("Player does not hold the played card");
      if((hand[a]==Card.COUNTESS || drawn==Card.COUNTESS) && (c==Card.KING || c==Card.PRINCE))//if one of the cards is the countess, a king or prince may not be played.
        throw new IllegalActionException("Player must play the countess");
      if(t!=-1){//if this action has a target (1,2,3,5,6 cards)
        if(eliminated(t)) //you cannot target an eliminated player
          throw new IllegalActionException("The action's target is already eliminated");
        if(c==Card.PRINCE && a==t) return;//a player can always target themselves with the Prince.
        if(handmaid(t) && (!allHandmaid(a) || c==Card.PRINCE))//you cannot target a player with the handmaid
          throw new IllegalActionException("The action's target is protected by the handmaid");
      } 
    }

    /**
     * Checks to see if an action is legal given the current state of the game, for an agent who has just drawn a card.
     * That is 
     * a) the player a must hold card c, 
     * b) it must be player a's turn
     * c) if the player holds the Countess, they cannot play the Prince or the King
     * d) if the action has a target, they cannot be eliminated
     * e) if the target is protected by the Handmaid and their is some player other than the target and a not protected, 
     *    then that player must be targetted instead. 
     * f) if all players are protected by the Handmaid and the player a plays a Prince, they must target themselves
     * There are other rules (such as a player not targetting themselves) that is enforced in the Action class.
     * @param act the action to be performed
     * @param drawn the card drawn by the playing agent.
     * @throws IllegalActionException if any of these conditions hold.
     * **/      
    public boolean legalAction(Action act, Card drawn){
      if(act ==null) return false;
      try{
        legalAction(act.player(), act.target(), act.card(), drawn);
      }
      catch(IllegalActionException e){return false;}
      return true;
    }
    
    /**
     * helper method to determine if the nominated player is protected by the handmaid
     * @return true if and only if the index corresponds to a player who is protected by the handmaid
     * **/
    public boolean handmaid(int player){
      return this.handmaid[player];
    }

    /**
     * helper method to check if every other player other than the specified player is either eliminated or protected by the handmaid
     * @param player the player who would be playing a card
     * @return true if and only if every player other than the nominated player is eliminated or prtoected by the handmaid
     * @throws ArrayIndexoutOfBoundsException if the playerIndex is out of range.
     * **/
    private boolean allHandmaid(int player){
      boolean noAction = true;
      for(int i = 0; i<num; i++)
        noAction = noAction && (eliminated(i) || handmaid(i) || i==player); 
      return noAction;
    }
  	
    /**
     * get all possible action from the current state
     * @return an arraylist store all possible action
     * @throws IllegalActionException if the action is not legal.
     * **/
  	public ArrayList<Action> GetMoves() throws IllegalActionException {

  		
  		Set<Action> moves=new HashSet<Action>();
  		
  		int target;
  		Action act =null;
  		Card choose=null;
  		
  		for(int i=0;i<this.myHand.length;i++) {
			switch(this.myHand[i]){
            case GUARD:
            	for(target=0;target<this.num;target++) {
            		for(int j=2;j<=8;j++) {
            			try {
            			if(j==2) {
            				choose=Card.PRIEST;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==3) {
            				choose=Card.BARON;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==4) {
            				choose=Card.HANDMAID;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==5) {
            				choose=Card.PRINCE;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==6) {
            				choose=Card.KING;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==7) {
            				choose=Card.COUNTESS;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			if(j==8) {
            				choose=Card.PRINCESS;
            				act = Action.playGuard(this.player, target, choose);
            			}
            			}catch(IllegalActionException e) {/*do nothing*/}
            			if(this.legalAction(act,this.myHand[1])/*&&target!=player&&!this.allHandmaid(player)*/) {
            				moves.add(act);//没有考虑所有人被maid保护
            			}
            		}
            	}
                break;
            case PRIEST:
            	for(target=0;target<this.num;target++) {
            		try {
						act = Action.playPriest(this.player, target);
					}catch(IllegalActionException e) {}
            		if(this.legalAction(act,this.myHand[1])) {
        				moves.add(act);
        			}
            	}
                break;
            case BARON:
            	for(target=0;target<this.num;target++) {
            		try {
						act = Action.playBaron(this.player, target);
					}catch (IllegalActionException e) {}
            		if(this.legalAction(act,this.myHand[1])) {
        				moves.add(act);
            		}
            	}
                break;
            case HANDMAID:
            	try {
					act = Action.playHandmaid(this.player);
				}catch(IllegalActionException e){}
                moves.add(act);
                break;
            case PRINCE:
            	for(target=0;target<this.num;target++) {
            		try {
						act = Action.playPrince(this.player, target);
					}catch (IllegalActionException e){}
	            	if(this.legalAction(act,this.myHand[1])) {
	    				moves.add(act);
	            	}
            	}
            	
                break;
            case KING:
            	for(target=0;target<this.num;target++) {
            		try {
						act = Action.playKing(this.player, target);
					}catch (IllegalActionException e){}
	            	if(this.legalAction(act,this.myHand[1])) {
	    				moves.add(act);
	            	}
            	}
                break;
            case COUNTESS:
            	try {
					act = Action.playCountess(this.player);
				}catch(IllegalActionException e){}
                moves.add(act);
                break;
            default:
                act = null;//never play princess
  		}
  	}
		return new ArrayList<Action>(moves);
  	}
  	
  	/**
	   * update the current state (the observer become the next player)
	   * @param act the action do by the observer
	   * @throws IllegalActionException if the action is not legal.
	   * **/
  	public void update(Action act) throws IllegalActionException{
  		
  		this.handmaid[player]=false;

  	    int a = act.player();//actor
  	    int t = act.target();//target
  	    Card c = act.card();
  	   
  	    try{
  	       legalAction(a,t,c,myHand[1]);
  	    }catch(IllegalActionException e){
  	      throw e;//reset discard top
  	    }
  	    if(c==hand[a]) {//if the player played the card in their hand, insert the new card into their hand.
			hand[a] = myHand[1];
		}
  	    switch(c){
  	      case GUARD://actor plays the guard
  	    	if(allHandmaid(a)) ;
  	        else if(act.guess()==hand[t]){//correct guess, target eliminated
  	          hand[t]=null;
  	        } 
  	        break;
  	      case PRIEST: 
  	        break;
  	      case BARON:
  	    	if(allHandmaid(a)) break; 
  	        int elim = -1;
  	        if(hand[a].value()>hand[t].value()) elim = t;
  	        else if(hand[a].value()<hand[t].value()) elim = a;
  	        if(elim!=-1) hand[elim]=null;
  	        break;
  	      case HANDMAID:
  	        //no update required
  	    	handmaid[player]=true;
  	        break;
  	      case PRINCE:
  	    	if(hand[t]==Card.PRINCESS) hand[t]=null; 	
  	    	else hand[t]=deck.remove(0);
  	        break;
  	      case KING:
  	    	if(allHandmaid(a)) break;
  	        Card tmp = hand[a];
  	        hand[a] = hand[t];
  	        hand[t] = tmp;
  	        break;
  	      case COUNTESS:  
  	        //no update required
  	        break;
  	      case PRINCESS:
  	    	hand[a]=null;
  	        break;
  	      default: 
  	        throw new IllegalActionException("Illegal Action? Something's gone very wrong");
  	    }//end of switch
  	    
  	    
        if(roundOver()){//check for round over
  	      int winner = roundWinner();
  	    }
  	    else{//set nextPlayer to next noneliminated player
			while(eliminated(nextPlayer)) nextPlayer = (nextPlayer+1)%num;
  	  	    player=nextPlayer;
  	    	nextPlayer = (nextPlayer+1)%num;    	
  	        while(eliminated(nextPlayer)) nextPlayer = (nextPlayer+1)%num;
  	        this.myHand[0]=hand[player];
  	        this.myHand[1]=deck.remove(0);
  	        
  	    }

  	  }
  	
  	/**
     * Tests to see if the round is over, either by all but one player being eliminated
     * or by all but one card being drawn from the deck.
     * @return true if and only if the round is over
     * **/
  	public boolean roundOver(){
  	    int remaining = 0;
  	    for(int i=0; i<num; i++) 
  	      if(!eliminated(i)) remaining++;
  	    return remaining==1 || deck.size()<2;
  	  }
  	
  	/**helper method to determine the winner of the round.
     * In the unlikely event of a total draw, 
     * the player with the smallest index is the winner.
     * @return the index of the winner, or -1 if the round is not yet over.
     * **/ 
  	public int roundWinner(){
  	    if(!roundOver()) return -1;
  	    int winner=-1;
  	    int topCard=-1;
  	    for(int p=0; p<num; p++){
  	      if(!eliminated(p)){
  	        if(hand[p].value()>topCard){
  	          winner = p;
  	          topCard = hand[p].value();
  	        }
  	      }
  	    }
  	    return winner;
  	  }
}

package agents;
import java.util.*;

import loveletter.*;

public class JosephAgent implements Agent{

    private Random rand;
    private State current;
    private int myIndex;
    private Map<Integer,Card> known = new HashMap<Integer,Card>();

    public JosephAgent(){
        rand  = new Random();
    }

    public String toString(){return "Joseph";}

    public void newRound(State start) {
        current = start;
        myIndex = current.getPlayerIndex();
        known.clear();
    }

    public void see(Action act, State results) {
        current = results;
    }

    public Action playCard(Card c) {
        Action act = null;

        boolean allProtected=true;

        for(int i=0;i<current.numPlayers();i++) {
            if(i==myIndex) continue;
            if(!current.eliminated(i)&&!current.handmaid(i)) allProtected=false;
        }
        Card[] handCards={c,current.getCard(myIndex)};
        int cardChosen=0;//the index we choose the card

        for(int i=0;i<4;i++) {
            if(i==myIndex) continue;
            if(current.getCard(i)!=null&&!current.eliminated(i))
                known.put(i,current.getCard(i));
            if(current.eliminated(i)||current.handmaid(i)) known.remove(i);
        }
        //select the lower value card
        cardChosen = handCards[0].value()<handCards[1].value()?0:1;

        if(handCards[0].value()==7&&(handCards[1].value()==5||handCards[1].value()==6)) cardChosen=0;
        if(handCards[1].value()==7&&(handCards[0].value()==6||handCards[0].value()==5)) cardChosen=1;
        //when all protected, we avoid playing Prince
        if(allProtected&&handCards[cardChosen].value()==5&&handCards[1-cardChosen].value()<8)
            cardChosen=1-cardChosen;
        //find the greatest scores player except myself
        int[] scores = new int[4];

        //setting a target list, ranking by their score DESCENDING
        List<Integer> targetList = new LinkedList<Integer>();
        for(int i=0;i<4;i++) {
            if(i==myIndex||current.eliminated(i)) continue;
            targetList.add(i);
        }
        switch(targetList.size()) {
            case 1:
                break;
            case 2:
                if (current.score(targetList.get(0)) < current.score(targetList.get(1)))
                    Collections.swap(targetList, 0, 1);
                if (current.handmaid(targetList.get(0)))
                    Collections.swap(targetList, 0, 1);
                break;
            case 3:
                if (current.score(targetList.get(0)) < current.score(targetList.get(1)))
                    Collections.swap(targetList, 0, 1);
                if (current.score(targetList.get(0)) < current.score(targetList.get(2)))
                    Collections.swap(targetList, 0, 2);
                if (current.score(targetList.get(1)) < current.score(targetList.get(2)))
                    Collections.swap(targetList, 1, 2);
                if (current.handmaid(targetList.get(1)))
                    Collections.swap(targetList, 1, 2);
                if (current.handmaid(targetList.get(0))) {
                    Collections.swap(targetList, 0, 2);
                    Collections.swap(targetList, 0, 1);
                }
                break;
        }

        Card[] unseen = current.unseenCards();
        List<Card> ch = new ArrayList<Card>();
        ch.addAll(Arrays.asList(unseen));
        ch.remove(handCards[1]);
        while(ch.remove(Card.GUARD));
        if(ch.isEmpty()&&handCards[1-cardChosen].value()<8) cardChosen=1-cardChosen;
        int target = targetList.get(0);
        try{
            switch(handCards[cardChosen]){
                case GUARD:
                    Card choose=null;
                    for(int i=0;i<4;i++)
                    {//choose the one we can see the card
                        if(i==myIndex) continue;//skip myself
                        if(known.containsKey(i)&&known.get(i).value()!=1)
                        {
                            choose=known.get(i);
                            target=i;
                        }
                    }
                    if(choose==null&&ch.size()>0) choose = ch.get(rand.nextInt(ch.size()));
                    if(choose==null) choose = Card.PRINCE;//only guard left to guess

                    act = Action.playGuard(myIndex, target, choose);
                    break;
                case PRIEST:
                    act = Action.playPriest(myIndex, target);
                    break;
                case BARON:
                    for(int i=0;i<4;i++)
                    {//choose the one we can see the card
                        if(i==myIndex) continue;//skip myself
                        if(known.containsKey(i))
                        {
                            if(known.get(i).value()<handCards[1-cardChosen].value())
                                target=i;
                        }
                    }

                    act = Action.playBaron(myIndex, target);
                    break;
                case HANDMAID:
                    act = Action.playHandmaid(myIndex);
                    break;
                case PRINCE:
                    if(allProtected) target=myIndex;
                    act = Action.playPrince(myIndex, target);
                    break;
                case KING:
                    act = Action.playKing(myIndex, target);
                    known.put(target,handCards[1-cardChosen]);//store the card we swapped
                    break;
                case COUNTESS:
                    act = Action.playCountess(myIndex);
                    break;
                default:
                    act = null;//never play princess
            }
        }catch(IllegalActionException e){/*do nothing, just try again*/}
        return act;
    }
}

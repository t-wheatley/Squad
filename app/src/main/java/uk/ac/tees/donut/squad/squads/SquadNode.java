package uk.ac.tees.donut.squad.squads;

/**
 * Created by q5273202 on 14/03/2017.
 */

public class SquadNode
{
    Squad squad;
    Interest interest;
    SquadNode next;

    public SquadNode(Squad s, Interest i)
    {
        squad = s;
        interest = i;
    }
}

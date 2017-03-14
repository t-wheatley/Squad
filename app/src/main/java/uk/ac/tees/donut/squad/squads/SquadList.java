package uk.ac.tees.donut.squad.squads;

/**
 * Created by q5273202 on 14/03/2017.
 */

public class SquadList
{
    SquadNode first;
    SquadNode last;

    public SquadList()
    {

    }
    public SquadList(SquadNode s)
    {
        first = s;  last = s;
    }

    public void insertNode(SquadNode s)
    {
        if(first == null)
        {
            first = s;
            last = s;
        }
        else
        {
            last.next = s;
            last = s;
        }
    }

    public int getLength()
    {
        if(first == null)
            return 0;

        SquadNode pointer = first;

        int count = 1;
        while(pointer.next != null)
        {
            pointer = pointer.next;
            count++;
        }

        return count;
    }
    
    //returns a squad when entering the Interest ENUM for that squad
    public Squad getSquad(Interest in)
    {
        SquadNode pointer = first;
        while(pointer.interest != in)
        {
            if(pointer.next == null)
                return null;

            pointer = pointer.next;
        }

        return pointer.squad;
    }
}

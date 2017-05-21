package uk.ac.tees.donut.squad.posts;

/**
 * Class used to represent a User's Post in a Squad.
 */
public class Post
{
    private String id, post, user, squad;
    private long dateTime;

    /**
     * Empty constructor for Firebase.
     */
    public Post()
    {
    }

    /**
     * Constructor for a default Post.
     *
     * @param user     The User who made the Post.
     * @param squad    The Squad the Post belongs to.
     * @param post     The text of the Post.
     * @param id       The unique id of the Post.
     * @param dateTime The DateTime the Post was made at.
     */
    public Post(String user, String squad, String post, String id, long dateTime)
    {
        this.user = user;
        this.squad = squad;
        this.post = post;
        this.id = id;
        this.dateTime = dateTime;
    }

    // GETTERS
    public String getId()
    {
        return id;
    }

    public String getPost()
    {
        return post;
    }

    public String getUser()
    {
        return user;
    }

    public String getSquad()
    {
        return squad;
    }

    public long getDateTime()
    {
        return dateTime;
    }

    // SETTERS
    public void setId(String id)
    {
        this.id = id;
    }

    public void setPost(String post)
    {
        this.post = post;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setSquad(String squad)
    {
        this.squad = squad;
    }

    public void setDateTime(long dateTime)
    {
        this.dateTime = dateTime;
    }
}



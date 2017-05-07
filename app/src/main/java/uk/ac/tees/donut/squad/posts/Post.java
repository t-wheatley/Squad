package uk.ac.tees.donut.squad.posts;

/**
 * Created by q5071134 on 07/05/2017.
 */

public class Post {
    String id, post, user, squad;

    public Post() {
    }

    public Post(String user, String squad, String post, String id) {
        this.user = user;
        this.squad = squad;
        this.post = post;
        this.id = id;
    }
}



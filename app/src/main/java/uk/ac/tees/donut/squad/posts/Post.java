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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSquad() {
        return squad;
    }

    public void setSquad(String squad) {
        this.squad = squad;
    }
}



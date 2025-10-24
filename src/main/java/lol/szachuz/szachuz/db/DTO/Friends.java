package lol.szachuz.szachuz.db.DTO;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "friends")
public class Friends {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int friendshipId;

    @ManyToOne
    @JoinColumn(name = "userid1", referencedColumnName = "userid")
    private Users user1;

    @ManyToOne
    @JoinColumn(name = "userid2", referencedColumnName = "userid")
    private Users user2;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "date")
    private LocalDate friendSince;


    //    <<  <<  GETTERS/SETTERS  >>  >>

    public int getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(int friendshipId) {
        this.friendshipId = friendshipId;
    }

    public Users getUser1() {
        return user1;
    }

    public void setUser1(Users user1) {
        this.user1 = user1;
    }

    public Users getUser2() {
        return user2;
    }

    public void setUser2(Users user2) {
        this.user2 = user2;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getFriendSince() {
        return friendSince;
    }

    public void setFriendSince(LocalDate friendSince) {
        this.friendSince = friendSince;
    }

    public enum Status {
        not_friends,
        pending,
        friends
    }
}


package kickstart.profil.konto;

import jakarta.persistence.*;
import kickstart.user.User;

/**
 * class to store Messages from the System for the user
 */
@Entity
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "CONTENT", length = 1000)
	private String content;


	private User.UserIdentifier userId;

	private String date;

	public Message(String content, User.UserIdentifier userId) {
		this.content = content;
		this.userId = userId;
	}

	public Message() {

	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public long getId() {
		return id;
	}


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User.UserIdentifier getUserId() {
		return userId;
	}

}


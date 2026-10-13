package kickstart.community;

import kickstart.user.User;
import kickstart.user.User.UserIdentifier;


import java.util.*;

import jakarta.persistence.*;

@Entity
/*
 * stores Information about a Community bet
 */
public class CommunityBettingInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	private long bettingSlipId;
	private String gameName;
	private String submittedDate;
	private String drawDate;

	@Column(name = "INFO", length = 1000)
	private String info;
	private Double basePrice;

	@ElementCollection
	private Map<String, Double> userBets;
	private int participants;
	private Double betAmount;
	@ManyToOne
	private Community community;

	public CommunityBettingInfo() {
	}


	public CommunityBettingInfo(Double basePrice,
								List<User> members,
								long bettingSlipId,
								String gameName,
								String submittedDate,
								User betCreator,
								Double betAmount,
								String drawDate) {
		this.basePrice = basePrice;
		this.bettingSlipId = bettingSlipId;
		this.gameName = gameName;
		this.submittedDate = submittedDate;
		this.userBets = new HashMap<>();
		this.betAmount = betAmount;
		this.participants = 0;
		this.drawDate = drawDate;
		for (User member: members) {
			userBets.put(member.getUserAccount().getUsername(), 0.0);
		}

	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}
	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public void addNewCommunityMember(User user) {
		userBets.put(user.getUserAccount().getUsername(), 0.0);
	}
	public void setBettingSlipId(long bettingSlipId) {
		this.bettingSlipId = bettingSlipId;
	}
	public long getBettingSlipId() {
		return bettingSlipId;
	}
	public String getGameName() {
		return gameName;
	}
	public String getSubmittedDate() {
		return submittedDate;
	}
	public Map<String, Double> getUserBets() {
		return userBets;
	}

	/**
	 *
	 * @return participants - all members of the bet that have more than 0 dollars placed on the bet
	 */
	public int getParticipants() {
		participants = 0;
		for (double bets: userBets.values()) {
			if (bets > 0.0) {
				participants++;
			}
		}
		return participants;
	}
	public Double getBetAmount() {
		return betAmount;
	}
	public void setBetAmount(Double betAmount) {
		this.betAmount = betAmount;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	public void setUserBets(Map<String, Double> userBets) {
		this.userBets = userBets;
	}
	public void setDrawDate(String drawDate) {
		this.drawDate = drawDate;
	}
	public String getDrawDate() {
		return drawDate;
	}
	public Double getBasePrice() {
		return basePrice;
	}

	/**
	 * adds the username and his bet to the userBets, also increases the groups overall bet amount
	 * @param userName - name of the user
	 * @param betAmount - the amount the user bets
	 */
	public void addUserBet(String userName, Double betAmount) {
		Double currentBet = userBets.get(userName);
		userBets.put(userName, currentBet + betAmount);
		this.betAmount += betAmount;
	}
	public void setBasePrice(Double basePrice) {
		this.basePrice = basePrice;
	}

	/**
	 * removes a user from the Community bet and removes all his bets
	 * @param user
	 */
	public void removeUser(User user) {
		String userName = user.getUserAccount().getUsername();
		double userBet = userBets.get(userName);
		userBets.remove(userName);
		betAmount -= userBet;
	}


}

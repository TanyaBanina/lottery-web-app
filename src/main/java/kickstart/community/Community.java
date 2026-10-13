package kickstart.community;

import jakarta.persistence.*;
import kickstart.user.User;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/*
 * stores information about a community
 */
@Entity
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private int communitySize = 0;


    private String name, password;

	/**
	 * one-to-many relation with CommunityBettingInfos
	 **/
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CommunityBettingInfo> communityBettingInfos = new ArrayList<>();

    @ManyToOne (cascade = CascadeType.PERSIST)
    private User admin;

    @ManyToMany(mappedBy = "communities", cascade = CascadeType.PERSIST)
    private List<User> communityMembers = new ArrayList<User>();

    public Community() {
    }

    public Community(User admin, String name, String password) {
        this.admin = admin;
        communityMembers.add(admin);
        this.name = name;
        this.password = password;
		communitySize++;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public User getAdmin() {
        return admin;
    }
    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public List<String> getCommunityMemberNames() {
        List<String> memberNames = new ArrayList<>();
        for (User member : communityMembers) {
            memberNames.add(member.getUserAccount().getUsername());
        }
        return memberNames;
    }

    public List<User> getCommunityMembers() {
        return communityMembers;
    }

	/**
	 * adds a
	 * @param user
	 * to the community and also to each CommunityBettingInfo of the Community, increases communitySize by 1
	 */
    public void addCommunityMember(User user) {
		if (communityMembers.size() == 30) {
			return;
		}
        communityMembers.add(user);
		communitySize += 1;
		for (CommunityBettingInfo bettingInfo : communityBettingInfos) {
			bettingInfo.addNewCommunityMember(user);
		}
    }

	public String showRole(User user) {
		if (admin == user) {
			return "Admin";
		}
		if (communityMembers.contains(user)) {
			return "Mitglied";
		}
		return null;
	}

	/**
	 * removes a user from the Community and all the CommunityBettingInfos, decreases communitySize by 1
	 * @param user
	 */
    public void removeCommunityMember(User user) {
        communityMembers.remove(user);
		for (CommunityBettingInfo bettingInfo : communityBettingInfos) {
			bettingInfo.removeUser(user);
			communitySize -=1;
		}
    }

	public long getId() {
		return id;
	}

    public List<CommunityBettingInfo> getCommunityBettingInfos() {
        return communityBettingInfos;
    }

	public CommunityBettingInfo getCommunityBettingInfoByBettingSlipId(Long bettingSlipId) {
		for (CommunityBettingInfo bettingInfo : communityBettingInfos) {
			if (bettingInfo.getBettingSlipId() == bettingSlipId) {
				return bettingInfo;
			}
		}
		return null;
	}
    public void addCommunityBettingInfo(CommunityBettingInfo bettingInfo) {
        communityBettingInfos.add(bettingInfo);
        bettingInfo.setCommunity(this);
    }

	/**
	 * checks all community bets of the Community and returns the bets that are still to be drawn
	 * @return localBettingInfos - a list of ongoing community bets
	 */
	public List<CommunityBettingInfo> getOngoingBets() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		List<CommunityBettingInfo> localBettingInfos = new ArrayList<>(communityBettingInfos); //deepcopy

		for (CommunityBettingInfo bettingInfo : communityBettingInfos) {
			try {
				LocalDate parsedDate = LocalDate.parse(bettingInfo.getDrawDate(), formatter);
				LocalDateTime drawDate = parsedDate.atStartOfDay();
				LocalDateTime now = LocalDateTime.now();

				if (drawDate.isBefore(now)) {
					localBettingInfos.remove(bettingInfo);
				}
			} catch (DateTimeParseException e) {
				System.out.println("Invalid date format: " + e.getMessage());
			}
		}

		return localBettingInfos;
	}

	public int getCommunitySize() {
		return communitySize;
	}

	public int getOngoingBetsCount() {
		return getOngoingBets().size();
	}


}

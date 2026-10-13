package kickstart.lotteries.toto.slips;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import kickstart.lotteries.BettingSlip;
import kickstart.user.User.UserIdentifier;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
public class TotoBettingSlip extends BettingSlip {

    @ElementCollection
    private Map<String, String> predictions;
    private Long communityId;
    private LocalDateTime drawDate;
    private String league; // "bundesliga1" or "bundesliga2"
    private TotoStatus status;

    public TotoBettingSlip() {
        super();
    }

    /**
     * Initializes slip with individual data (predictions, bet amount, user ID, community ID, date of submittal).
     * Sets draw date to next midnight and status to PENDING.
     */
    public TotoBettingSlip(LocalDateTime dateSubmitted, double betAmount, Map<String, String> predictions,
                           UserIdentifier userId, Long communityId) {
        super(null, dateSubmitted, userId, betAmount);

        this.predictions = predictions;
        this.communityId = communityId;
        this.drawDate = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        this.status = TotoStatus.PENDING;
        this.league = "bundesliga1"; // default, will be set in controller
    }

    public Map<String, String> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<String, String> predictions) {
        this.predictions = predictions;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public LocalDateTime getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(LocalDateTime drawDate) {
        this.drawDate = drawDate;
    }

    public TotoStatus getStatus() {
        return status;
    }

    public void setStatus(TotoStatus status) {
        this.status = status;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }
}


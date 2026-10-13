package kickstart.community;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CommunityRepository extends JpaRepository<Community, Long> {
    Community findByName(String name);
	Community findById(long id);
    Community findTop1By(); //to be removed when lotto works
}

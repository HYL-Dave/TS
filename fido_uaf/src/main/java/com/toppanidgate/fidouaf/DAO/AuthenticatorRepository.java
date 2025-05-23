package com.toppanidgate.fidouaf.DAO;

import java.util.List;

import com.toppanidgate.fidouaf.model.Authenticator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {

	@Query(value = "select devkey from {h-schema}Authenticator a where a.available = 'Y' and  userid = :userid ORDER BY id ASC", nativeQuery = true)
	public List<String> getDevkeysByName(@Param("userid") String name);

	@Modifying
	@Query(value = "update {h-schema}Authenticator set available = 'N' where available = 'Y' and userid = :userid", nativeQuery = true)
	public int updateByUserID(@Param("userid") String userid);

	@Query(value = "select value from {h-schema}Authenticator a where a.available = 'Y' and userid = :userid and devKey = :devKey", nativeQuery = true)
	public String getValueByDevKeyAndUsername(@Param("devKey") String devKey, @Param("userid") String username);

	@Query(value = "select value from {h-schema}Authenticator a where a.available = 'Y' and devKey = :devKey", nativeQuery = true)
	public String getValueByDevKey(@Param("devKey") String devKey);
	
	@Query(value = "select top(1) * from {h-schema}Authenticator", nativeQuery = true)
	public Authenticator healthCheck();

	@Query(value = "select value from {h-schema}Authenticator a where a.available = 'Y' and userid = :userid ORDER BY id ASC", nativeQuery = true)
	public List<String> getValuesByUsername(@Param("userid") String username);

}

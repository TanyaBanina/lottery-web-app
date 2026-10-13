package kickstart.community;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CommunityHelper {
	/**
	 * hashes the password and returns it hashed
	 * @param password
	 * @return hexString - the hashed password
	 */
	public static String hashPassword(String password) {  //should not throw exception as both algorithms are hardcoded
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}

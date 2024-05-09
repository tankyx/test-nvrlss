package WithdrawalService.Utils;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class SaltGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 32; // Adjust the length as needed
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> generatedSalts = new HashSet<>();

    public static String generateUniqueSalt() {
        String salt;
        do {
            salt = generateSalt();
        } while (!generatedSalts.add(salt)); // Keep generating until we find a new, unique salt
        return salt;
    }

    /**
     * Helper method to generate a random salt string.
     * @return a random string salt
     */
    private static String generateSalt() {
        StringBuilder salt = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            salt.append(CHARACTERS.charAt(index));
        }
        return salt.toString();
    }
}

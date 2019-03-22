/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.revelc.code.totp4j;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Totp {

  public static final long TOTP_INTERVAL = 30L;
  public static final long PRINT_INTERVAL = 2L;
  public static final int NUM_DIGITS = 6; // 6, 7, or 8

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Must specify KEY on command-line");
      System.exit(1);
    }
    var key = args[0];
    generate(key);
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> generate(key), PRINT_INTERVAL,
        PRINT_INTERVAL, TimeUnit.SECONDS);
  }

  public static void generate(String key) {
    var movingFactor = getMovingFactor();
    var hmac = hmacSha1(decodeBase32(key), movingFactor);
    int offset = hmac[hmac.length - 1] & 0x0F;
    var result = Arrays.copyOfRange(hmac, offset, offset + 4);
    result[0] &= 0x7f;
    var otp = new BigInteger(result).longValueExact();
    printOtp(otp, NUM_DIGITS);
  }

  private static void printOtp(long otp, int digits) {
    long truncated = otp % (long) Math.pow(10, digits);
    System.out.printf("\r%0" + digits + "d <---", truncated);
  }

  private static byte[] getMovingFactor() {
    var intervalsSinceEpoch = Instant.now().toEpochMilli() / 1000L / TOTP_INTERVAL;
    var time = BigInteger.valueOf(intervalsSinceEpoch).toByteArray();
    var movingFactor = new byte[8];
    System.arraycopy(time, 0, movingFactor, movingFactor.length - time.length, time.length);
    return movingFactor;
  }

  private static final String HMAC_SHA1 = "HmacSHA1";

  private static byte[] hmacSha1(byte[] key, byte[] text) {
    try {
      var mac = Mac.getInstance(HMAC_SHA1);
      mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
      return mac.doFinal(text);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new AssertionError("Completely unexpected", e);
    }
  }

  private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
  private static final Map<Character, Long> bitValues = new HashMap<>();

  static {
    for (int i = 0; i < base32Chars.length(); i++) {
      bitValues.put(base32Chars.charAt(i), i & 0x7FFFFFFFL);
    }
  }

  private static byte[] decodeBase32(String encoded) {
    var upperCaseEncoded = encoded.toUpperCase();
    var value = BigInteger.ZERO;
    int numEncodedChars = 0;
    for (int i = 0; i < upperCaseEncoded.length(); i++) {
      var nextBits = bitValues.get(upperCaseEncoded.charAt(i));
      if (nextBits != null) {
        numEncodedChars++;
        value = value.shiftLeft(5).add(BigInteger.valueOf(nextBits));
      }
    }
    var numBitsInLastChar = 8 - 5 * (numEncodedChars - 1) % 8;
    var numBitsOvershot = 5 - numBitsInLastChar;
    value = value.shiftRight(numBitsOvershot);
    var numBytes = (numEncodedChars * 5 - numBitsOvershot) / 8;
    byte[] result = value.toByteArray();
    if (result.length < numBytes) {
      byte[] padded = new byte[numBytes];
      System.arraycopy(result, 0, padded, numBytes - result.length, result.length);
      return padded;
    }
    return result;
  }

}

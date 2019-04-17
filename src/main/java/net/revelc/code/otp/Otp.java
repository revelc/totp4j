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

package net.revelc.code.otp;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Otp {

  protected Options options;

  public Otp(Options options) {
    this.options = options;
  }

  public static void main(String[] args) {
    new Otp(Options.parse(args)).generate();
  }


  public void generate() {
    var counter = getCounterBytes(getCounter());
    for (String key : options.getKeys()) {
      var hmac = hmac(Base32.decode(key), counter);
      int offset = hmac[hmac.length - 1] & 0x0F;
      var result = Arrays.copyOfRange(hmac, offset, offset + 4);
      result[0] &= 0x7f;
      var otp = new BigInteger(result).longValueExact();

      int digits = options.getDigits();
      System.out.printf("%0" + digits + "d%n", otp % (long) Math.pow(10, digits));
    }
  }

  protected String generateOtp(String key, BigInteger counter) {

    var cBytes = getCounterBytes(counter);
    var hmac = hmac(Base32.decode(key), cBytes);
    int offset = hmac[hmac.length - 1] & 0x0F;
    var result = Arrays.copyOfRange(hmac, offset, offset + 4);
    result[0] &= 0x7f;
    var otp = new BigInteger(result).longValueExact();

    int digits = options.getDigits();
    return formatOtp(otp, digits);
  }

  private String formatOtp(long otp, int digits) {
    return String.format("%0" + digits + "d", otp % (long) Math.pow(10, digits));
  }


  public BigInteger getCounter() {
    // counter is number of intervals since the epoch in TOTP; in HOTP the counter is specified
    return BigInteger
        .valueOf(options.isTotp() ? Instant.now().getEpochSecond() / options.getTimestep()
            : options.getCounter());
  }

  private byte[] getCounterBytes(BigInteger counter) {
    var cBytes = counter.toByteArray();
    var padded = new byte[8];
    System.arraycopy(cBytes, 0, padded, padded.length - cBytes.length, cBytes.length);
    return padded;
  }

  private byte[] hmac(byte[] key, byte[] text) {
    try {
      var mac = Mac.getInstance(options.getAlgorithm());
      mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
      return mac.doFinal(text);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new AssertionError("Completely unexpected", e);
    }
  }

}

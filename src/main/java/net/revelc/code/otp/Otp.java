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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Otp {

  private Options options;

  public Otp(Options options) {
    this.options = options;
  }

  public static void main(String[] args) {
    new Otp(Options.parse(args)).generate();
  }

  public void generate() {
    var counter = getCounter();
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

  private byte[] getCounter() {
    // counter is number of intervals since the epoch in TOTP; in HOTP the counter is specified
    var counter =
        BigInteger.valueOf(options.isTotp() ? Instant.now().getEpochSecond() / options.getTimestep()
            : options.getCounter()).toByteArray();
    var padded = new byte[8];
    System.arraycopy(counter, 0, padded, padded.length - counter.length, counter.length);
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

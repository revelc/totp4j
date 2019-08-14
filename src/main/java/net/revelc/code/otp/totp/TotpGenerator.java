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

package net.revelc.code.otp.totp;

import java.math.BigInteger;
import java.time.Instant;

import net.revelc.code.otp.Options;
import net.revelc.code.otp.Otp;

/**
 * Generator for TOTP
 */
public class TotpGenerator extends Otp {

  private String key;

  public TotpGenerator(String key) {
    super(totpOptions());
    this.key = key;
  }

  private static Options totpOptions() {
    return Options.parse(new String[] {"--totp", "-d=6"});
  }

  public Totp generateTotp() {

    BigInteger bigCounter = getCounter();
    int counter = bigCounter.intValue();
    String otp = generateOtp(key, bigCounter);
    long nextSeconds = (counter + 1) * 30;
    long currentSeconds = Instant.now().getEpochSecond();
    int diff = (int) (nextSeconds - currentSeconds);
    return new Totp(otp, diff);
  }

  /**
   * Simple class that combines a password string along with its remaining life, in seconds
   */
  public static class Totp {
    private String otp;
    private int secondsRemaining;

    public String getOtp() {
      return otp;
    }

    public int getSecondsRemaining() {
      return secondsRemaining;
    }

    private Totp(String otp, int secondsRemaining) {
      this.otp = otp;
      this.secondsRemaining = secondsRemaining;
    }
  }
}

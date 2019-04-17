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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Options {

  private enum OptionEnum {
    // adopt some similar naming of command-line options from oathtool man page
    HELP("help", "h"), VERBOSE("verbose", "v"),

    HEXKEY("hex", "x"), BASE32KEY("base32", "b"),

    HOTP("hotp"), COUNTER("counter", "c"),

    TOTP("totp"), TIMESTART("start-time", "S"), TIMENOW("now", "N"), TIMESTEP("time-step-size",
        "s"),

    HMAC_SHA1("sha1"), HMAC_SHA256("sha256"), HMAC_SHA512("sha512"),

    DIGITS("digits", "d");

    private final String longOpt;
    private final String shortOpt;

    private OptionEnum(String longOpt) {
      this(longOpt, null);
    }

    private OptionEnum(String longOpt, String shortOpt) {
      this.longOpt = longOpt;
      this.shortOpt = shortOpt;
    }

    /**
     * @return the longOpt
     */
    public String getLongOpt() {
      return longOpt;
    }

    /**
     * @return the shortOpt
     */
    public String getShortOpt() {
      return shortOpt;
    }

  }

  private static final Map<String, OptionEnum> opts = new HashMap<>();
  static {
    for (OptionEnum e : OptionEnum.values()) {
      if (e.getLongOpt() != null) {
        opts.compute("--" + e.getLongOpt(), (key, oldValue) -> {
          if (oldValue != null) {
            throw new IllegalStateException(
                "Two option enums have same longOpt: " + oldValue + "," + e);
          }
          return e;
        });
      }
      if (e.getShortOpt() != null) {
        opts.compute("-" + e.getShortOpt(), (key, oldValue) -> {
          if (oldValue != null) {
            throw new IllegalStateException(
                "Two option enums have same shortOpt: " + oldValue + "," + e);
          }
          return e;
        });
      }
    }
  }

  private static final String HMAC_SHA1 = "HmacSHA1";
  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String HMAC_SHA512 = "HmacSHA512";

  private List<String> keys = new ArrayList<>();
  private int counter = 0;
  private boolean base32 = true;
  private int digits = 6;
  private String algorithm = HMAC_SHA1;
  private boolean totp = true;
  private boolean help = false;
  private int timestep = 30;

  private Options() {
    // copy constructor
  }

  private Options(String[] args) {
    Objects.requireNonNull(args, "Arguments list must not be null");
    this.help = args.length == 0;
    for (var i = new AtomicInteger(0); i.get() < args.length; i.incrementAndGet()) {
      var argvalue = args[i.get()].split("=", 2);
      var arg = argvalue[0];
      var equalsArg = argvalue.length > 1 ? argvalue[1] : null;
      OptionEnum e = opts.get(arg);
      if (e == null) {
        keys.add(arg);
      } else {
        switch (e) {
          case BASE32KEY:
            this.base32 = true;
            break;
          case COUNTER:
            this.counter = getIntValue(equalsArg, args, i, x -> x >= 0,
                "A non-negative integer must follow the counter option");
            break;
          case DIGITS:
            this.digits = getIntValue(equalsArg, args, i, x -> x > 5 && x < 9,
                "A value of 6, 7, or 8 must follow the digits option");
            break;
          case HELP:
            this.help = true;
            break;
          case HEXKEY:
            this.base32 = false;
            break;
          case HMAC_SHA1:
            this.algorithm = HMAC_SHA1;
            break;
          case HMAC_SHA256:
            this.algorithm = HMAC_SHA256;
            break;
          case HMAC_SHA512:
            this.algorithm = HMAC_SHA512;
            break;
          case HOTP:
            this.totp = false;
            break;
          case TIMENOW:
            throw new UnsupportedOperationException("Not yet implemented"); // TODO
          case TIMESTART:
            throw new UnsupportedOperationException("Not yet implemented"); // TODO
          case TIMESTEP:
            this.timestep = getIntValue(equalsArg, args, i, x -> x > 0,
                "A strictly positive integer must follow the timestep option");
            break;
          case TOTP:
            this.totp = true;
            break;
          case VERBOSE:
            throw new UnsupportedOperationException("Not yet implemented"); // TODO
          default:
        }
      }
    }
    if (this.help) {
      doHelp();
    }
  }

  private static int getIntValue(String equalsArg, String[] argArray, AtomicInteger nextArgIndex,
      Predicate<Integer> validator, String errorMessage) {
    String arg;
    if (equalsArg != null) {
      arg = equalsArg;
    } else if (nextArgIndex.incrementAndGet() < argArray.length) {
      arg = argArray[nextArgIndex.get()];
    } else {
      throw new IllegalArgumentException(errorMessage);
    }
    int number = Integer.parseInt(arg);
    if (validator.test(number)) {
      return number;
    }
    throw new IllegalArgumentException(errorMessage);
  }

  private void doHelp() {
    // TODO Auto-generated method stub
    System.out.println("print help here");
  }

  public static Options parse(String[] args) {
    return new Options(args);
  }

  /**
   * Returns a new copy of the given options, using the given key
   * 
   * @param newkey the new key
   * @return the new options
   */
  public Options withKeys(String newkey) {
    Options copy = new Options();
    copy.counter = counter;
    copy.base32 = base32;
    copy.digits = digits;
    copy.algorithm = algorithm;
    copy.totp = totp;
    copy.timestep = timestep;
    copy.keys = List.of(newkey);
    return copy;
  }

  /**
   * @return the keys
   */
  public List<String> getKeys() {
    return keys;
  }

  /**
   * @return the algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * @return the digits
   */
  public int getDigits() {
    return digits;
  }

  /**
   * @return the timestep
   */
  public int getTimestep() {
    return timestep;
  }

  /**
   * @return the counter
   */
  public int getCounter() {
    return counter;
  }

  /**
   * @return the totp
   */
  public boolean isTotp() {
    return totp;
  }

  /**
   * @return the base32
   */
  public boolean isBase32() {
    return base32;
  }
}

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
import java.util.HashMap;
import java.util.Map;

public class Base32 {
  private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
  private static final Map<Character, Byte> bitValues = new HashMap<>();

  static {
    // create mapping for char numeric values, 0 to 31, or 00000000 to 00011111 (5-bits each, max)
    for (byte i = 0; i < base32Chars.length(); i++) {
      bitValues.put(base32Chars.charAt(i), i);
    }
  }

  public static byte[] decode(String encoded) {
    encoded = encoded.toUpperCase();
    var decodedAsNumber = BigInteger.ZERO;
    int numEncodedChars = 0;
    // for each recognized char, add its corresponding 5-bit value
    for (int i = 0; i < encoded.length(); i++) {
      var nextBits = bitValues.get(encoded.charAt(i));
      if (nextBits != null) {
        numEncodedChars++;
        decodedAsNumber = decodedAsNumber.shiftLeft(5).or(BigInteger.valueOf(nextBits));
      }
    }
    // all the chars, except maybe the last one, since the last 8 byte boundary encode 5-bits each
    // the last char encodes however many more bits it takes to get to the next full byte (8-bits)
    var numBitsInLastChar = 8 - 5 * (numEncodedChars - 1) % 8;

    // the last 5-bit shift in the loop may have shifted too far, if the last char didn't encode
    // a full 5 bits, so shift it back if necessary
    var numBitsOvershot = 5 - numBitsInLastChar;
    decodedAsNumber = decodedAsNumber.shiftRight(numBitsOvershot);

    byte[] result = decodedAsNumber.toByteArray();

    // total number of 8-bit bytes decoded (5 bits for each encode char, except maybe the last one)
    var numBytes = (numEncodedChars * 5 - numBitsOvershot) / 8;
    // pad or remove padding, as needed
    if (result.length < numBytes) {
      byte[] zeroPadded = new byte[numBytes];
      System.arraycopy(result, 0, zeroPadded, numBytes - result.length, result.length);
      return zeroPadded;
    }
    if (result.length > numBytes) {
      byte[] truncateZeroPadding = new byte[numBytes];
      System.arraycopy(result, result.length - numBytes, truncateZeroPadding, 0, numBytes);
      return truncateZeroPadding;
    }
    return result;
  }

}

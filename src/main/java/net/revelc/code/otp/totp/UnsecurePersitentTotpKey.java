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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Key object that can save itself to disk in the users home directory
 */
public class UnsecurePersitentTotpKey implements TotpKey {

  private File keyFile;

  public UnsecurePersitentTotpKey() {
    String userHome = System.getProperty("user.home");
    String packageName = getClass().getPackageName();
    keyFile = new File(userHome, "." + packageName + ".info.txt");
  }

  @Override
  public String getKey() {
    if (keyFile.exists()) {
      try {
        return Files.readString(keyFile.toPath());
      } catch (IOException e) {
        System.err.println("Unable to read key file: " + keyFile);
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public void setKey(String key) {
    try {
      Files.writeString(keyFile.toPath(), key);
    } catch (IOException e) {
      System.err.println("Unable to write key to file: " + keyFile);
      e.printStackTrace();
    }
  }

}

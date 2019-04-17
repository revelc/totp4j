package net.revelc.code.otp.totp;

/**
 * Simple interface for getting and setting a key
 */
public interface TotpKey {

  public String getKey();

  public void setKey(String key);
}

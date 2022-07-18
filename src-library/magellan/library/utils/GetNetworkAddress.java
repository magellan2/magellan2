// class magellan.client.swing.GetNetworkAddress
// created on Apr 1, 2022
//
// Copyright 2003-2022 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Determines the MAC or IP address of the current machine.
 *
 */
public class GetNetworkAddress {

  /**
   * An exception showing that the address could not be determined with a cause.
   */
  public static class AddressException extends Exception {
    AddressException(Exception cause) {
      super("Could not determine address", cause);
    }
  }

  /**
   * What type of address should be returned.
   */
  public enum Type {
    /**
     * MAC address
     */
    MAC,
    /**
     * IP address
     */
    IP
  }

  /**
   * Returns the address of the current machine
   * 
   * @param addressType {@link Type#MAC} requests the MAC address, {@link Type#IP} an IP address
   * @return A MAC address is returned in the form 12-34-56-78-9A; an IP address is returned as 192.168.1.42.
   * @throws GetNetworkAddress.AddressException If there was some type of network error while querying the system.
   */
  public static String getAddress(GetNetworkAddress.Type addressType) throws GetNetworkAddress.AddressException {
    String address;
    try {

      InetAddress lanIp = InetAddress.getLocalHost();
      NetworkInterface iface = NetworkInterface.getByInetAddress(lanIp);
      if (iface != null) {
        byte[] hardwareAddress = iface.getHardwareAddress();
        return new String(hardwareAddress);
      }

      String ipAddress = null;
      Enumeration<NetworkInterface> net = null;
      net = NetworkInterface.getNetworkInterfaces();

      while (net.hasMoreElements()) {
        NetworkInterface element = net.nextElement();
        Enumeration<InetAddress> addresses = element.getInetAddresses();

        while (addresses.hasMoreElements() &&
            element.getHardwareAddress() != null &&
            element.getHardwareAddress().length > 0 &&
            !isVMMac(element.getHardwareAddress())) {
          InetAddress ip = addresses.nextElement();
          if (ip instanceof Inet4Address) {

            if (ip.isSiteLocalAddress()) {
              ipAddress = ip.getHostAddress();
              lanIp = InetAddress.getByName(ipAddress);
            }

          }

        }
      }

      if (lanIp == null)
        return null;

      switch (addressType) {
      case IP:
        address = lanIp.toString().replaceAll("^/+", "");
        break;
      case MAC:
        address = getMacAddress(lanIp);
        break;
      default:
        address = "";
      }
    } catch (Exception e) {
      throw new AddressException(e);
    }
    return address;
  }

  private static String getMacAddress(InetAddress ip) throws SocketException {
    String address = null;
    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
    byte[] mac = network.getHardwareAddress();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    address = sb.toString();

    return address;
  }

  private static boolean isVMMac(byte[] mac) {
    if (null == mac)
      return false;
    byte invalidMacs[][] = {
        { 0x00, 0x05, 0x69 }, // VMWare
        { 0x00, 0x1C, 0x14 }, // VMWare
        { 0x00, 0x0C, 0x29 }, // VMWare
        { 0x00, 0x50, 0x56 }, // VMWare
        { 0x08, 0x00, 0x27 }, // Virtualbox
        { 0x0A, 0x00, 0x27 }, // Virtualbox
        { 0x00, 0x03, (byte) 0xFF }, // Virtual-PC
        { 0x00, 0x15, 0x5D } // Hyper-V
    };

    for (byte[] invalid : invalidMacs) {
      if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2])
        return true;
    }

    return false;
  }

}
package tv.ismar.account;

import android.os.Handler;
import android.os.Message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by huibin on 02/11/2017.
 */

public class IsmartvDns implements Dns {
    private static boolean occurError = false;
    private static DnsHandler mDnsHandler = new DnsHandler();

    @Override
    public List<InetAddress> lookup(String hostName) throws UnknownHostException {
//        private static final String SKY_HOST = "http://sky.tvxio.com";
//        private static final String SKY_HOST_TEST = "http://skypeach.test.tvxio.com/";
        if (occurError && (hostName.equals("sky.tvxio.com") || hostName.equals("skypeach.test.tvxio.com"))){
            throw new UnknownHostException("occurError throw UnknownHostException");
        }

        String ipAddress = IsmartvActivator.getHostByName(hostName);
        if (ipAddress.endsWith("0.0.0.0")) {
            occurError = true;
            mDnsHandler.sendEmptyMessageDelayed(0 ,2000);
            throw new UnknownHostException("can't connect to internet");
        }
        return Dns.SYSTEM.lookup(ipAddress);
    }

     static class DnsHandler extends Handler{
         @Override
         public void handleMessage(Message msg) {
             occurError = false;
         }
     }
}

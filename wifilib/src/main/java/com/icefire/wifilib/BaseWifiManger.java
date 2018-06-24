package com.icefire.wifilib;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * @author yangchj
 * email: yangchj@icefire.me
 */
public abstract class BaseWifiManger {

    public final static int WIFI_CLIENT_MODE=0;//客户端模式
    public final static int WIFI_AP_MODE=1;//热点模式

    public final static String WIFI_MANGER="wifi-manager";

    //验证模式常量
    public static final int WIFICIPHER_NOPASS = 0; //无密码
    public static final int WIFICIPHER_WEP = 1;//WEP
    public static final int WIFICIPHER_WPA = 2;//WPA

    protected List<WifiConfiguration> mWifiConfigurations;//无线网络配置信息类集合（网络连接列表）
    protected WifiManager mWifiManager;
    protected WifiInfo mWifiInfo;//描述Wi-Fi的连接状态
    protected WifiManager.WifiLock mWifiLock;//能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    protected Context mContext;

    public BaseWifiManger(Context context) {
        this.mContext = context;
        mWifiManager= (WifiManager) this.mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiInfo=mWifiManager.getConnectionInfo();
    }

    /**
     * 获取WI-FI状态
     * @return 是否打开
     */
    public boolean isOpen(){
        return this.mWifiManager.isWifiEnabled();
    }

    /**
     * 打开WI-FI
     */
    public void openWifi(){
        if (!isOpen()){//当前网络不可用
            this.mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WIFI
     */
    public void closeWifi(){
        if (isOpen()){
            this.mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 配置WI-FI信息
     * @param ssid  Wifi热点名称
     * @param password  密码
     * @param authAlogrithm KEY_NONE是无密码，KEY_WEP是共享密钥WEP模式，KEY_WPA是WPA_PSK加密，暂不支持EAP
     * @param type  AP模式或Client模式,WIFI_CLIENT_MODE,WIFI_AP_MODE
     * @return 配置信息
     */
    public WifiConfiguration makeConfiguration(String ssid,String password,int authAlogrithm,int type){
        // 配置网络信息类
        WifiConfiguration wifiConfig = new WifiConfiguration();
        // 清空配置网络属性
        wifiConfig.allowedAuthAlgorithms.clear();
        wifiConfig.allowedGroupCiphers.clear();
        wifiConfig.allowedKeyManagement.clear();
        wifiConfig.allowedPairwiseCiphers.clear();
        wifiConfig.allowedProtocols.clear();

        if (type==WIFI_CLIENT_MODE){//Wi-Fi连接
            wifiConfig.SSID="\"" + ssid + "\"";
            //检测热点是否已存在
            WifiConfiguration tmpConfig=isExsits(ssid);
            if (tmpConfig!=null){//存在
                Log.d(WIFI_MANGER,"已存在该网络的配置网络");
                mWifiManager.removeNetwork(tmpConfig.networkId);//从列表中删除指定的网络配置网络
            }
            if (authAlogrithm==WIFICIPHER_NOPASS){//没有密码
                wifiConfig.wepKeys[0]="";
                wifiConfig.wepTxKeyIndex=0;
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }else  if (authAlogrithm==WIFICIPHER_WEP){//WEP密码
                wifiConfig.hiddenSSID=true;
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.wepKeys[0]="\""+password+"\"";
                wifiConfig.wepTxKeyIndex = 0;
            }else if(authAlogrithm==WIFICIPHER_WPA){
                wifiConfig.preSharedKey="\""+password+"\"";
                wifiConfig.hiddenSSID=true;
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
            }
        }else{ // "ap" wifi热点
            wifiConfig.SSID=ssid;
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (authAlogrithm==WIFICIPHER_NOPASS){//没有密码
                wifiConfig.wepKeys[0]="";
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.wepTxKeyIndex=0;
            }else if(authAlogrithm==WIFICIPHER_WEP){//WEP密码
                wifiConfig.hiddenSSID=true;//网络上不广播
                wifiConfig.wepKeys[0]=password;
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            }else if(authAlogrithm==WIFICIPHER_WPA){// WPA加密
                wifiConfig.hiddenSSID = true;// 网络上不广播ssid
                wifiConfig.preSharedKey = password;
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            }
        }
        return wifiConfig;
    }

    /**
     * 已配置的无线热点中，是否存在网络信息
     * @param ssid ssid
     * @return 配置信息
     */
    protected WifiConfiguration isExsits(String ssid){
        if (this.mWifiManager.getConfiguredNetworks()==null){
            return null;
        }
        Iterator<WifiConfiguration> localIterator=this.mWifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfig;
        do {
            if (!localIterator.hasNext()){
                return null;
            }
            localWifiConfig=localIterator.next();
        }while (!localWifiConfig.SSID.equals("\"" + ssid + "\""));
        return localWifiConfig;
    }

    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfigurations;
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     */
    public void acquireWifiLock() {
        this.mWifiLock.acquire();
    }

    /**
     * 创建一个WifiLock
     * @param lock 锁
     */
    public void createWifiLock(String lock) {
        this.mWifiLock = this.mWifiManager.createWifiLock(lock);
    }

    /**
     * 解锁WifiLock
     */
    public void releaseWifilock() {
        if (mWifiLock.isHeld()) { // 判断时候锁定
            mWifiLock.acquire();
        }
    }

    /**
     * 获取wifi SSID
     * @return ssid
     */
    public String getSSID() {
        if (this.mWifiInfo == null) {
            return null;
        }
        return this.mWifiInfo.getSSID();
    }

    /**
     * 获取wifi BSSID
     * @return bssid
     */
    public String getBSSID() {
        if (this.mWifiInfo == null) {
            return null;
        }
        return this.mWifiInfo.getBSSID();
    }

    /**
     * 获取ip地址
     * @return ip
     */
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * 获取网关IP
     * @return 网关ip
     */
    public int getGatewayIP() {
        return (this.mWifiManager == null) ? 0 : this.mWifiManager.getDhcpInfo().gateway;
    }

    /**
     * 获取物理地址(Mac)
     * @return mac地址
     */
    public String getMacAddress() {
        return (mWifiInfo == null) ? null : mWifiInfo.getMacAddress();
    }

    /**
     * 获取网络id
     * @return 网络ID
     */
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * 获取wifi连接信息
     * @return Wifi信息
     */
    public WifiInfo getWifiInfo() {
        return this.mWifiManager.getConnectionInfo();
    }

    /**
     * 获取WI-FI Manager
     * @return WifiManager
     */
    public WifiManager getWifiManager() {
        return this.mWifiManager;
    }
}

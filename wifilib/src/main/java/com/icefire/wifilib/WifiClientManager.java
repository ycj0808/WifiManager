package com.icefire.wifilib;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;

import java.util.List;

/**
 * @author yangchj
 * email: yangchj@icefire.me
 */
public class WifiClientManager extends BaseWifiManger{


    public WifiClientManager(Context context) {
        super(context);
    }

    @Override
    public WifiConfiguration makeConfiguration(String ssid, String password, int authAlogrithm, int type) {
        return super.makeConfiguration(ssid, password, authAlogrithm, WIFI_CLIENT_MODE);
    }

    /**
     * 断开指定id的WI-FI
     * @param networkId netId
     */
    public void disconnectWifi(int networkId){
        this.mWifiManager.disableNetwork(networkId);
    }

    /**
     * 添加并连接指定网络
     * @param wifiConfig 网络配置
     */
    public void addNetwork(WifiConfiguration wifiConfig){
        openWifi();
        WifiConfiguration tmpConfig=isExsits(wifiConfig.SSID);
        if (tmpConfig!=null){
            mWifiManager.removeNetwork(tmpConfig.networkId);//从列表中删除指定的网络配置网络
        }
        int netId=mWifiManager.addNetwork(wifiConfig);
        mWifiManager.enableNetwork(netId,true);
    }

    /**
     * 连接指定配置好的网络
     * @param netId netId
     */
    public void connectWifi(int netId){
        super.openWifi();
        mWifiManager.enableNetwork(netId,true);
    }

    /**
     * 获取当前网络状态
     * WIFI_STATE_DISABLING = 0;
     * WIFI_STATE_DISABLED = 1
     * WIFI_STATE_ENABLING = 2;
     * WIFI_STATE_ENABLED = 3;
     * WIFI_STATE_UNKNOWN = 4;
     * @return 网络状态
     */
    public int getWifiState(){
        return this.mWifiManager.getWifiState();
    }

    /**
     * 指定的ssid是否已连接
     * @param ssid ssid
     * @return 连接 true，未连接 false
     */
    public boolean isConnected(String ssid){
        WifiInfo wifiInfo=getWifiInfo();
        if (wifiInfo==null) return false;
        return TextUtils.equals(wifiInfo.getSSID(),ssid);
    }

    /**
     * 获取已连接过的热点
     * @return 已连接过的WI-FI
     */
    public List<WifiConfiguration> getHasWifiConfiguration(){
        return this.mWifiManager.getConfiguredNetworks();
    }
}

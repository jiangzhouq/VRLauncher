package qjizho.vrlauncher.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import qjizho.vrlauncher.R;
import qjizho.vrlauncher.WifiActivity;

/**
 * Created by qjizho on 15/10/28.
 */
public class WTAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ScanResult> mList;  //扫描到的网络结果列表
    private WifiActivity mContext;
    private int cur_selected_pos;
    public WTAdapter(WifiActivity context, List<ScanResult> list) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    //新加的一个函数，用来更新数据
    public void setData(List<ScanResult> list) {
        this.mList = list;
        notifyDataSetChanged();
    }
    public void setCurPosition(int pos){
        cur_selected_pos = pos;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //获取点击向的扫描结果
        final ScanResult localScanResult = mList.get(position);
        Log.d("qiqi", "capabilities: " +localScanResult.capabilities);
        //获取wifi类
//		final WifiAdmin wifiAdmin = WifiAdmin.getInstance(mContext);
        final WifiAdmin wifiAdmin = mContext.m_wiFiAdmin;
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.wtitem, parent,false);
            //加载布局模板控件
            viewHolder.textVName = ((TextView) convertView.findViewById(R.id.name_text_wtitem));
            viewHolder.progressBConnecting = ((ProgressBar) convertView.findViewById(R.id.connecting_progressBar_wtitem));
            viewHolder.wifiIcon = (ImageView ) convertView.findViewById(R.id.wifi_icon);
            viewHolder.connectOK = (TextView) convertView.findViewById(R.id.connected);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //For the view of wifi locked or unlocked.
        if(localScanResult.capabilities.contains("WPA")
                || localScanResult.capabilities.contains("wpa")
                || localScanResult.capabilities.contains("WEP")
                || localScanResult.capabilities.contains("wep")){
            viewHolder.wifiIcon.setImageResource(R.mipmap.wifi_lock);
        }else{
            viewHolder.wifiIcon.setImageResource(R.mipmap.wifi);
        }
//        //点击连接处理事件
//        viewHolder.textConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //创建wifi网络
//                WifiConfiguration localWifiConfiguration = wifiAdmin.createWifiInfo(localScanResult.SSID, WifiActivity.WIFI_AP_PASSWORD, 3,"wt");
//                //添加到网络
//                wifiAdmin.addNetwork(localWifiConfiguration);
//                //"点击链接"消失，显示进度条，
//                viewHolder.textConnect.setVisibility(View.GONE);
//                viewHolder.progressBConnecting.setVisibility(View.VISIBLE);
//                viewHolder.linearLConnectOk.setVisibility(View.GONE);
//                //点击后3.5s发送消息
//                mContext.mHandler.sendEmptyMessageDelayed(mContext.m_nWTConnected, 3500L);
//            }
//        });
//        // 点击断开处理事件
//        viewHolder.linearLConnectOk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //断开指定wifi热点
//                wifiAdmin.disconnectWifi(wifiAdmin.getWifiInfo().getNetworkId());
//                //"断开连接"消失，进度条显示
//                viewHolder.progressBConnecting.setVisibility(View.VISIBLE);
//                //点击后3.5s发送消息
//                mContext.mHandler.sendEmptyMessageDelayed(mContext.m_nWTConnected, 3500L);
//            }
//        });

        //初始化布局
        viewHolder.progressBConnecting.setVisibility(View.GONE);
        viewHolder.textVName.setText(localScanResult.SSID); //显示热点名称
        if(position == cur_selected_pos){
            viewHolder.textVName.setTextColor(mContext.getResources().getColor(R.color.drap_bar_word_color));
        }else{
            viewHolder.textVName.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        // 正连接的wifi信息
        WifiInfo localWifiInfo = wifiAdmin.getWifiInfo();
        if (localWifiInfo != null) {
            try {//正在连接
                if ((localWifiInfo.getSSID() != null)&& (localWifiInfo.getSSID().replace("\"", "").equals(localScanResult.SSID.toString()))) {
                    Log.d("qiqi","SSID connected:" + localWifiInfo.getSSID() + " local SSID:" + localScanResult.SSID);
//                    viewHolder.linearLConnectOk.setVisibility(View.VISIBLE);
                    viewHolder.connectOK.setVisibility(View.VISIBLE);
                    return convertView;
                }else{

                    viewHolder.connectOK.setVisibility(View.GONE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return convertView;
            }
//            viewHolder.textConnect.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public final class ViewHolder {
        public ProgressBar progressBConnecting;
        public TextView textVName;
        public TextView connectOK;
        public ImageView wifiIcon;
        public ViewHolder() {
        }
    }
}
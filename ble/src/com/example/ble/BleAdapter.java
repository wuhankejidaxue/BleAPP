package com.example.ble;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;//加载浏览出现的蓝牙设备的布局需要用到
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;//蓝牙适配器继承自基本适配器
import android.widget.TextView;	//展示浏览到的蓝牙设备的具体信息

public class BleAdapter extends BaseAdapter
{
	private ArrayList<BluetoothDevice> leDevices;	//layoutinflater相当于是findViewById，但获得的是布局文件对象
	private static LayoutInflater inflater;	
	
	/*
	 * 构造函数方便初始化实例对象
	 */
	public BleAdapter(Context context)
	{
		leDevices = new ArrayList<BluetoothDevice>();//泛型可以存储长度不限，元素类型不限的内容，这里是蓝牙设备和对应的信息
		//用inflater来加载一个布局 
		//convertView = inflater.inflate(R.layout.listdevice, null);
		//加载listdevice.xml文件
		inflater = LayoutInflater.from(context);
	}
	
	/*
	 * 添加蓝牙设备,先把获取到的蓝牙在泛型中检查是否在其中，信息压（包含）入泛型ArrayList<BluetoothDevice>堆栈
	 */
	public void BleaddAdvice(BluetoothDevice device)
	{
		if(!leDevices.contains(device))
			leDevices.add(device);
	}
	
	/*
	 * 在泛型中查找对应位置的元素（蓝牙设备）并返回,其类型是BluetoothDevice
	 */
	public BluetoothDevice getBleDevice(int position)
	{
		return leDevices.get(position);
	}
	
	/*
	 *清除泛型里面的元素（蓝牙信息）并置空泛型 
	 */
	public void Bleclear()
	{
		leDevices.clear();
	}
	
	/*
	 * （非 Javadoc）获取泛型的长度，即搜索到的蓝牙设备的个数
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return leDevices.size();
	}
	
	/*
	 * （非 Javadoc）查找泛型中对应位置的元素，并返回为原始object对象类型
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return leDevices.get(position);
	}
	
	/*
	 * （非 Javadoc）返回要查找的位置
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/*
	 * （非 Javadoc）用viewHolder展现搜索到的设备信息（包括设备名和设备地址）
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder viewHolder;//viewholder相当于一个
		if(convertView ==null)
		{
			convertView = inflater.inflate(R.layout.listdevice, null);//使用
			viewHolder = new ViewHolder();
			viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
			viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		BluetoothDevice device = leDevices.get(position);
		String nameString = device.getName();
		if(nameString!=null && nameString.length()>-1)
			viewHolder.deviceName.setText(nameString);
		else 
			viewHolder.deviceName.setText("未知设备");
		viewHolder.deviceAddress.setText(device.getAddress());
		return convertView;
	}
	
    private class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}

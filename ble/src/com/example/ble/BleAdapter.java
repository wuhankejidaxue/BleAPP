package com.example.ble;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;//����������ֵ������豸�Ĳ�����Ҫ�õ�
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;//�����������̳��Ի���������
import android.widget.TextView;	//չʾ������������豸�ľ�����Ϣ

public class BleAdapter extends BaseAdapter
{
	private ArrayList<BluetoothDevice> leDevices;	//layoutinflater�൱����findViewById������õ��ǲ����ļ�����
	private static LayoutInflater inflater;	
	
	/*
	 * ���캯�������ʼ��ʵ������
	 */
	public BleAdapter(Context context)
	{
		leDevices = new ArrayList<BluetoothDevice>();//���Ϳ��Դ洢���Ȳ��ޣ�Ԫ�����Ͳ��޵����ݣ������������豸�Ͷ�Ӧ����Ϣ
		//��inflater������һ������ 
		//convertView = inflater.inflate(R.layout.listdevice, null);
		//����listdevice.xml�ļ�
		inflater = LayoutInflater.from(context);
	}
	
	/*
	 * ��������豸,�Ȱѻ�ȡ���������ڷ����м���Ƿ������У���Ϣѹ���������뷺��ArrayList<BluetoothDevice>��ջ
	 */
	public void BleaddAdvice(BluetoothDevice device)
	{
		if(!leDevices.contains(device))
			leDevices.add(device);
	}
	
	/*
	 * �ڷ����в��Ҷ�Ӧλ�õ�Ԫ�أ������豸��������,��������BluetoothDevice
	 */
	public BluetoothDevice getBleDevice(int position)
	{
		return leDevices.get(position);
	}
	
	/*
	 *������������Ԫ�أ�������Ϣ�����ÿշ��� 
	 */
	public void Bleclear()
	{
		leDevices.clear();
	}
	
	/*
	 * ���� Javadoc����ȡ���͵ĳ��ȣ����������������豸�ĸ���
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return leDevices.size();
	}
	
	/*
	 * ���� Javadoc�����ҷ����ж�Ӧλ�õ�Ԫ�أ�������Ϊԭʼobject��������
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return leDevices.get(position);
	}
	
	/*
	 * ���� Javadoc������Ҫ���ҵ�λ��
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/*
	 * ���� Javadoc����viewHolderչ�����������豸��Ϣ�������豸�����豸��ַ��
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder viewHolder;//viewholder�൱��һ��
		if(convertView ==null)
		{
			convertView = inflater.inflate(R.layout.listdevice, null);//ʹ��
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
			viewHolder.deviceName.setText("δ֪�豸");
		viewHolder.deviceAddress.setText(device.getAddress());
		return convertView;
	}
	
    private class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}

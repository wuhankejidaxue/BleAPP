package com.example.ble;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.TextView;

import com.example.ble.MainActivity;

public class BleControl  
{
	public static float x,y,z;
	public float a=(float) 9.8;//定义实时合加速度
	public  static float p1=0;
	public  static float p2=0;
	public  static float p3=0;
	public  static float k1=0;//定义p1与p2之间的斜率
	public  static float k2=0;//定义p2与p3之间的斜率
	public Context context;
	public BluetoothAdapter bluetoothAdapter;
	public BleAdapter devicelistadapter;//设备列表（安卓listview）一般需要和对应的两个适配器关联起来
	public BluetoothDevice BLEdevice;
	public BluetoothGatt bluetoothGatt;
	public BluetoothGattService service;
	public BluetoothGattCharacteristic mCharacteristic,readCharacteristic,writeCharacteristic;
	public Handler handler;
	public String clientUuid;
	//read and write service , characteristic UUID 
	public static UUID RW_Service = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
	public static UUID R_Char = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
	public static UUID W_Char = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	public boolean scaning;	
	
	
    public BleControl(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;    
    }
	

    
    
    
    public static Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			
			//显示接收到的内容	
			Log.i("sensor data=", msg.obj.toString());
			Log.i("（字符串）长度为:",Integer.toString(msg.obj.toString().length()));
			//过滤因蓝牙传输数据出错而发送的某次或几次连续的不完整数据 数据切割，强制类型转换
			if(msg.obj.toString().length() >= 14){
				String[] strs = msg.obj.toString().split(" ");
				Log.i("sensor xdata=", strs[0]);
				MainActivity.xdata.setText(strs[0]);
				Log.i("sensor ydata=", strs[1]);
				MainActivity.ydata.setText(strs[1]);
				Log.i("sensor zdata=", strs[2]);
				MainActivity.zdata.setText(strs[2]);
				x = Float.parseFloat(strs[0]);
				y = Float.parseFloat(strs[1]);
				z = Float.parseFloat(strs[2]);
				p1=p2;
				p2=p3;
				p3=Math.abs(y);
				k1=p2-p1;
				k2=p3-p2;
				if(p3!=0){
				if(Math.abs(z)>12){
					MainActivity.movemode.setText("检测到竖直摆动");
				}
				else 
					MainActivity.movemode.setText("       ");
				}
				if(z<12){
					if(k1>0.1 && k2>0.1){
						MainActivity.movemode.setText("检测到水平运动");
					}
					else
						MainActivity.movemode.setText("       ");
				}
				
			}
			
//			MainActivity.information.clearComposingText();
//			MainActivity.information.append("rece:"+(msg.obj).toString()+"\n");
		}
	}; 
	
	
    public void initialize() {
    	BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
    	bluetoothAdapter = manager.getAdapter();
    	devicelistadapter = new BleAdapter(context);
    }
	



	
    //check blue-tooth enable,检查
    public void checkBluetoothEnabled() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    //checking the scanning state检查浏览状态
    public void checkDeviceScanning() {
        if (scaning) {
            scanBleDevice(false);
        }
    }

    //check the state of GATT connect检查低功耗蓝牙与本地手机蓝牙的的连接状态
    public void checkGattConnected() {
        if (bluetoothGatt != null) {
            if (bluetoothGatt.connect()) {
            	bluetoothGatt.disconnect();
            	bluetoothGatt.close();
            }
        }
    }
    

    //
    public void scanBleDevice(boolean enable) {
    	if (enable) 
    	{
    		handler.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				scaning = false;
    				bluetoothAdapter.stopLeScan(mLeScanCallback);
    				Message message = new Message();
    				message.what = 1;
    				handler.sendMessage(message);
    			}
    		}, 5000);
    		scaning = true;     
    		bluetoothAdapter.startLeScan(mLeScanCallback);
    	} else {
    		scaning = false;
    		bluetoothAdapter.stopLeScan(mLeScanCallback);
    	}
    }
    
    /*
     * 创建浏览到的低功耗蓝牙设备列表（可能有多个设备，在我们的项目测试中一般只会找到flora蓝牙设备的信息）
     */
    public void creatDeviceListDialog() {
        if (devicelistadapter.getCount() > 0) {
            new AlertDialog.Builder(context).setCancelable(true)
                .setAdapter(devicelistadapter, new DialogInterface.OnClickListener() {
                    @Override
                    /*
                     * （非 Javadoc）选择和点击我们想要 连接的蓝牙设备，那么
                     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                     */
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothDevice device = devicelistadapter.getBleDevice(which);
                        bluetoothGatt = device.connectGatt(context, false, callback);
                    }
                }).show();
        }
    } 
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() 
    {
    	@Override
    	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
    		{
    			devicelistadapter.BleaddAdvice(device); 
    			devicelistadapter.notifyDataSetChanged();
    		}
    	}
    };
    
    
	public BluetoothGattCallback callback = new BluetoothGattCallback()
	{
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) 
		{
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				if(newState == BluetoothProfile.STATE_CONNECTED)
					bluetoothGatt.discoverServices();
			}
		};

		/*
		 * （非 Javadoc）数据从这里上来
		 * @see android.bluetooth.BluetoothGattCallback#onCharacteristicChanged(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic)
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) 
		{
				try{
					byte[] data = characteristic.getValue();
					String dataString = new String(data,"GB2312").trim();	
					Message msg = new Message();
					msg.obj = dataString;
					mHandler.sendMessage(msg);
				} catch (Exception e) {   }
		};

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) 
		{
			super.onCharacteristicWrite(gatt, characteristic, status);
		};

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) 
		{
			super.onCharacteristicRead(gatt, characteristic, status);
		};
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) 
		{
			super.onServicesDiscovered(gatt, status);
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				//use read and write UUID to create service and find characteristic
				service = bluetoothGatt.getService(RW_Service);
				readCharacteristic = service.getCharacteristic(R_Char);
				writeCharacteristic = service.getCharacteristic(W_Char);
			}
			setCharacteristicNotification(readCharacteristic);
		};
	};

	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic)
	{
		String clientUuid = "00002902-0000-1000-8000-00805f9b34fb";
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(clientUuid));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
	}
	
	public void send(View view)
	{
		if (bluetoothGatt != null) {
			if (bluetoothGatt.connect() && writeCharacteristic != null)
			{
				String dataString = MainActivity.input.getText().toString();
				writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				writeCharacteristic.setValue(dataString);
				bluetoothGatt.writeCharacteristic(writeCharacteristic);
				MainActivity.input.setText(null);
//				MainActivity.information.append("send: "+dataString+"\n");
			}
		}
	}
}

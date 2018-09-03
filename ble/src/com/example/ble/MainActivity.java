package com.example.ble;


import com.example.ble.MainActivity;
import com.example.ble.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;







import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity
{
	
	public static BluetoothDevice device;
	public BleControl bleControl;
	public boolean scaning;
	public static EditText input;
	public static TextView xdata,ydata,zdata,movemode;
//	private static final int SERIES_NR = 1;
	public Button send,clear;
	OnClickListener listener1 = null;
	OnClickListener listener2 = null;	
		
	private Timer timer = new Timer();
	private TimerTask task;
	private Handler handler;
	private XYSeries series1,series2,series3;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private Context context;
	private int addX = -1;

	private float addY,addY1,addY2,addY3;
	private String[] titles = new String[] { "Acclerate_x", "Acclerate_y", "Acclerate_z" };
	int[] xv1 = new int[20];
	float[] yv1= new float[20];
	int[] xv2 = new int[20];
	float[] yv2= new float[20];
	int[] xv3 = new int[20];
	float[] yv3= new float[20];

	

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {//���浱ǰ��״̬�����������ķ�ʽ�˳����򣬱����ڴ治������û������˳������´��ٴν���ʱ�Ӵ������������activity�н��룬һ�㳣���Ķ�������ҳ����д�����������ʾ����״̬
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		input = (EditText) findViewById(R.id.editText);
		send = (Button) findViewById(R.id.send);
		clear = (Button) findViewById(R.id.clear);
		xdata = (TextView) findViewById(R.id.xdata);
		ydata = (TextView) findViewById(R.id.ydata);
		zdata = (TextView) findViewById(R.id.zdata);
		movemode = (TextView) findViewById(R.id.movemode);
		setTitle("���ݼ��ƽ̨");		
		bleControl = new BleControl(this, mHandler);//���mhandle����ʾ�������������豸��
		bleControl.initialize();
		
		MainActivity.movemode.setText("���˶�");
		//bleControl.a=(float) Math.sqrt(bleControl.x*bleControl.x+bleControl.y*bleControl.y+bleControl.z*bleControl.z);
			
		 context = getApplicationContext();    
		    //������main�����ϵĲ��֣�������ͼ��ؼ����������������
		    LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout);    
		    //������������������ϵ����е㣬��һ����ļ��ϣ�������Щ�㻭������
		    series1 = buildOneSeries(titles[0]);
		    series2 = buildOneSeries(titles[1]);
		    series3 = buildOneSeries(titles[2]);
		    
		    //����һ�����ݼ���ʵ����������ݼ�������������ͼ��
		    mDataset = new XYMultipleSeriesDataset();
		    
		    //���㼯��ӵ�������ݼ���
		    mDataset.addSeries(series1);
		    mDataset.addSeries(series2);
		    mDataset.addSeries(series3);
		    
		    //���¶������ߵ���ʽ�����Եȵȵ����ã�renderer�൱��һ��������ͼ������Ⱦ�ľ��
		    int[] colors = new int[] { Color.BLUE, Color.RED, Color.BLACK};
		    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
		        PointStyle.TRIANGLE};
		    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
		    int length = renderer.getSeriesRendererCount();
		    setChartSettings(renderer, "SensorWave", "Time", "Data", 0.5, 12.5, -30, 30,
		        Color.LTGRAY, Color.LTGRAY);
		    //����ͼ��
		    chart = ChartFactory.getLineChartView(context, mDataset, renderer);//context?  
		    //��ͼ����ӵ�������ȥ
		    layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));		    
		    //�����Handlerʵ������������Timerʵ������ɶ�ʱ����ͼ��Ĺ���
		    handler = new Handler() {
		    @Override
		    public void handleMessage(Message msg) 
		    {
		     //ˢ��ͼ��
		     updateChart(series1,series2,series3);
		     super.handleMessage(msg);
		    }
		    };
		    
		    task = new TimerTask() {
		    @Override
		    public void run() {
		    Message message = new Message();
		        message.what = 1;
		        handler.sendMessage(message);
		    }
		    };
		    
		    timer.schedule(task, 500, 500);
		    
	}

	public void onDestroy1() {
	 //����������ʱ�ص�Timer
	 timer.cancel();
	 super.onDestroy();
	}
	
	
	
	
	
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) 
	{ 
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(); 
	    int length = colors.length; 
	    for (int i = 0; i < length; i++) 
	    { 
	        XYSeriesRenderer r = new XYSeriesRenderer(); 
	        r.setColor(colors[i]); 
	        r.setPointStyle(styles[i]); 
	        r.setFillPoints(fill); 
	        renderer.addSeriesRenderer(r); 
	    } 
	    return renderer; 
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String chartTitle,String xTitle, String yTitle,
	double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
	 //�йض�ͼ�����Ⱦ�ɲο�api�ĵ�
	 renderer.setChartTitle(chartTitle);//���ñ�Ĵ����
	 renderer.setXTitle(xTitle);//����x��ı�ʾ
	 renderer.setYTitle(yTitle);//����y��ı�ʾ
	 renderer.setXAxisMin(xMin);//����x����ĳ�ʼ�̶���Сֵ
	 renderer.setXAxisMax(xMax);//����X����ĳ�ʼ�̶����ֵ
	 renderer.setYAxisMin(yMin);//����y����ĳ�ʼ�̶�������Сֵ
	 renderer.setYAxisMax(yMax);//����y����ĳ�ʼ�̶����ֵ
	 renderer.setAxesColor(axesColor);//������������ɫ
	 renderer.setLabelsColor(labelsColor);//���ÿ̶���ɫ
	 renderer.setShowGrid(true);//��������ɼ�
	 renderer.setGridColor(Color.GREEN);//������������ɫ
	 renderer.setXLabels(20);//���ú�����Ŀ̶���������ô���=��XAxisMax-XAxisMin��/20-1+1
	 renderer.setYLabels(10);//����������Ŀ̶���������ô���=��YAxisMax-YAxisMin��/10-1
	 renderer.setYLabelsAlign(Align.RIGHT);//����y�����������ߵĶ����ϵ�ǿ̶ȱ�ע�������ߵ��Ҳ࣬ʵ���ϵ���������������̶��������ߵġ��Ϸ���
	 renderer.setPointSize((float) 2);//���õĲ�������������Ī��Ĺ�ϵ��������ļ��
	 renderer.setShowLegend(true);//����ͼ����������Խ�һ���Ż�
	 renderer.setDisplayChartValues(true);
	}
	
	protected XYSeries buildOneSeries(String title) 
	{ 
	XYSeries series = new XYSeries(title);    //����ÿ���ߵ����ƴ�����ÿ�����м�����	
	for (int k = 0; k < 1000; k++)        //ÿ�����еĵ�����������������ֵ�Ե���ʽ��������
	{ 
		addX = k;
		addY = 0;
		series.add(addX, addY); //ÿ���߼��뵽���ݼ���
	}
	
	return series; 
	}

	
	
	private void updateChart(XYSeries series1,XYSeries series2,XYSeries series3) {
	
	//���ú���һ����Ҫ���ӵĽڵ�
	addX = 0;
	addY1 = (float)bleControl.x;
	addY2 = (float)bleControl.y;
	addY3 = (float)bleControl.z;
	//�Ƴ����ݼ��оɵĵ㼯
	mDataset.removeSeries(series1);
	mDataset.removeSeries(series2);
	mDataset.removeSeries(series3);
	//�жϵ�ǰ�㼯�е����ж��ٵ㣬��Ϊ��Ļ�ܹ�ֻ������100�������Ե���������100ʱ��������Զ��100
	int length = series1.getItemCount();
	if (length > 20) {
	 length = 20;
	}
	//���ɵĵ㼯��x��y����ֵȡ��������backup�У����ҽ�x��ֵ��1�������������ƽ�Ƶ�Ч��
	for (int i = 0; i < length; i++) {
	xv1[i] = (int) series1.getX(i) + 1;//ÿ����һ����ôƽ�Ƶĳ߶�Ϊ1������ӵĳ���1Ҫ���������̶����Ե����ö�����
	yv1[i] = (float) series1.getY(i);//Ҫʵ�ֶ�̬Ч���������Ȱѵ㼯�����ݼ��Ƴ���ƥ�䣬Ȼ���ƽ��ˢ��֮ǰ���ض�һ�����е㼯��洢����ֵ�Ի�ȡ����ʱ�洢�����鵥Ԫ��
	xv2[i] = (int) series2.getX(i) + 1;
	yv2[i] = (float) series2.getY(i);
	xv3[i] = (int) series3.getX(i) + 1;
	yv3[i] = (float) series3.getY(i);	
	}
	//�㼯����գ�Ϊ�������µĵ㼯��׼��
	series1.clear();
	series2.clear();
	series3.clear();
	//���²����ĵ����ȼ��뵽�㼯�У�Ȼ����ѭ�����н�����任���һϵ�е㶼���¼��뵽�㼯��
	//�����������һ�°�˳��ߵ�������ʲôЧ������������ѭ���壬������²����ĵ�
	series1.add(addX, addY1);//�ڲ����ߵ��������µ�1�������
	series2.add(addX, addY2);
	series3.add(addX, addY3);
	for (int k = 0; k < length; k++) {
	 series1.add(xv1[k], yv1[k]);
	 series2.add(xv2[k], yv2[k]);
	 series3.add(xv3[k], yv3[k]);//����ʱ���ݴ�ռ���һ��ȡ���ɵ�����㣬��ôÿһ��ˢ��˲��ͻ����������ƶ���21�����������ʾ��ͼ����
	 }
	//�����ݼ�������µĵ㼯
	mDataset.addSeries(series1);//���°��ض�һ���ߵĵ㼯���뵽���ݼ���ʵ���������ݼ�����Ⱦ��render�İ�
	mDataset.addSeries(series2);
	mDataset.addSeries(series3);
	//��ͼ���£�û����һ�������߲�����ֶ�̬
	//����ڷ�UI���߳��У���Ҫ����postInvalidate()������ο�api
	chart.invalidate();//ԭ��������±����ɣ�ʵ��ϵͳ��ε�ˢ��
	 }
	
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
			finish();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//setting the menu and offer two button: start and stop
		getMenuInflater().inflate(R.menu.menu, menu);
		if(!scaning)
		{
			menu.findItem(R.id.scan_start).setVisible(true);
			menu.findItem(R.id.disconnect).setVisible(false);
		}
		else {
			menu.findItem(R.id.scan_start).setVisible(false);
			menu.findItem(R.id.disconnect).setVisible(true);
		}
		return true;
	}

	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bleControl.bluetoothAdapter.disable();
	}

	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		bleControl.checkBluetoothEnabled();
	}

	
	
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		bleControl.checkDeviceScanning();
		bleControl.checkGattConnected();
	}

	
	
	@Override
	//switch Start and Stop
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan_start:
			//start scanning
			bleControl.scanBleDevice(true);
			Toast.makeText(this, "scaning, please wait", Toast.LENGTH_LONG).show();
			scaning = true;
			invalidateOptionsMenu();
			break;
		case R.id.disconnect:
			bleControl.checkGattConnected();
			scaning = false;
			invalidateOptionsMenu();
			break;
		}
		return true;
	}

	
	
	//enable the blue-tooth and create the device list dialog��һ���ֻ��������adruit��һ������ģ�飬ֻ����ʾһ������ �ǿ��ܻ���ͬʱ����������ĵ͹�������
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case 0:
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, 1);//������������
				break;
			case 1:
				bleControl.creatDeviceListDialog();
				break;
			default:
				break;
			}
		}
	};

	
	
	public void send(View view)
	{
		bleControl.send(view);
		invalidateOptionsMenu();
	}

	
	
	public void clear(View view)
	{
//		information.setText("");
	}
}

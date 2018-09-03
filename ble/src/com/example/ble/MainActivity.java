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
	protected void onCreate(Bundle savedInstanceState) {//保存当前的状态，不管怎样的方式退出程序，比如内存不足或者用户主动退出程序，下次再次进入时从带有这个方法的activity中进入，一般常见的都是在主页面中写这个方法，显示保存状态
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		input = (EditText) findViewById(R.id.editText);
		send = (Button) findViewById(R.id.send);
		clear = (Button) findViewById(R.id.clear);
		xdata = (TextView) findViewById(R.id.xdata);
		ydata = (TextView) findViewById(R.id.ydata);
		zdata = (TextView) findViewById(R.id.zdata);
		movemode = (TextView) findViewById(R.id.movemode);
		setTitle("数据监测平台");		
		bleControl = new BleControl(this, mHandler);//这个mhandle是显示搜索到的蓝牙设备的
		bleControl.initialize();
		
		MainActivity.movemode.setText("无运动");
		//bleControl.a=(float) Math.sqrt(bleControl.x*bleControl.x+bleControl.y*bleControl.y+bleControl.z*bleControl.z);
			
		 context = getApplicationContext();    
		    //这里获得main界面上的布局，下面会把图表控件画在这个布局里面
		    LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout);    
		    //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		    series1 = buildOneSeries(titles[0]);
		    series2 = buildOneSeries(titles[1]);
		    series3 = buildOneSeries(titles[2]);
		    
		    //创建一个数据集的实例，这个数据集将被用来创建图表
		    mDataset = new XYMultipleSeriesDataset();
		    
		    //将点集添加到这个数据集中
		    mDataset.addSeries(series1);
		    mDataset.addSeries(series2);
		    mDataset.addSeries(series3);
		    
		    //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		    int[] colors = new int[] { Color.BLUE, Color.RED, Color.BLACK};
		    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
		        PointStyle.TRIANGLE};
		    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
		    int length = renderer.getSeriesRendererCount();
		    setChartSettings(renderer, "SensorWave", "Time", "Data", 0.5, 12.5, -30, 30,
		        Color.LTGRAY, Color.LTGRAY);
		    //生成图表
		    chart = ChartFactory.getLineChartView(context, mDataset, renderer);//context?  
		    //将图表添加到布局中去
		    layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));		    
		    //这里的Handler实例将配合下面的Timer实例，完成定时更新图表的功能
		    handler = new Handler() {
		    @Override
		    public void handleMessage(Message msg) 
		    {
		     //刷新图表
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
	 //当结束程序时关掉Timer
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
	 //有关对图表的渲染可参看api文档
	 renderer.setChartTitle(chartTitle);//设置表的大标题
	 renderer.setXTitle(xTitle);//设置x轴的表示
	 renderer.setYTitle(yTitle);//设置y轴的表示
	 renderer.setXAxisMin(xMin);//设置x坐标的初始刻度最小值
	 renderer.setXAxisMax(xMax);//设置X坐标的初始刻度最大值
	 renderer.setYAxisMin(yMin);//设置y坐标的初始刻度坐标最小值
	 renderer.setYAxisMax(yMax);//设置y坐标的初始刻度最大值
	 renderer.setAxesColor(axesColor);//设置坐标轴颜色
	 renderer.setLabelsColor(labelsColor);//设置刻度颜色
	 renderer.setShowGrid(true);//设置网格可见
	 renderer.setGridColor(Color.GREEN);//设置网格是绿色
	 renderer.setXLabels(20);//设置横坐标的刻度数量，那么间隔=（XAxisMax-XAxisMin）/20-1+1
	 renderer.setYLabels(10);//设置纵坐标的刻度数量，那么间隔=（YAxisMax-YAxisMin）/10-1
	 renderer.setYLabelsAlign(Align.RIGHT);//设置y坐标与网格线的对其关系是刻度标注在网格线的右侧，实际上倒过来看就是坐标刻度在网格线的“上方”
	 renderer.setPointSize((float) 2);//设置的采样间隔和这个有莫大的关系，即网格的间隔
	 renderer.setShowLegend(true);//设置图例，这里可以进一步优化
	 renderer.setDisplayChartValues(true);
	}
	
	protected XYSeries buildOneSeries(String title) 
	{ 
	XYSeries series = new XYSeries(title);    //根据每条线的名称创建，每条线有几个点	
	for (int k = 0; k < 1000; k++)        //每条线中的点数依次以类似名数值对的形式存入线中
	{ 
		addX = k;
		addY = 0;
		series.add(addX, addY); //每条线加入到数据集中
	}
	
	return series; 
	}

	
	
	private void updateChart(XYSeries series1,XYSeries series2,XYSeries series3) {
	
	//设置好下一个需要增加的节点
	addX = 0;
	addY1 = (float)bleControl.x;
	addY2 = (float)bleControl.y;
	addY3 = (float)bleControl.z;
	//移除数据集中旧的点集
	mDataset.removeSeries(series1);
	mDataset.removeSeries(series2);
	mDataset.removeSeries(series3);
	//判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
	int length = series1.getItemCount();
	if (length > 20) {
	 length = 20;
	}
	//将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
	for (int i = 0; i < length; i++) {
	xv1[i] = (int) series1.getX(i) + 1;//每波动一次那么平移的尺度为1，这里加的常亮1要配合了坐标刻度属性的设置而设置
	yv1[i] = (float) series1.getY(i);//要实现动态效果，必须先把点集从数据集移除解匹配，然后把平移刷新之前的特定一条线中点集里存储的名值对获取和暂时存储在数组单元，
	xv2[i] = (int) series2.getX(i) + 1;
	yv2[i] = (float) series2.getY(i);
	xv3[i] = (int) series3.getX(i) + 1;
	yv3[i] = (float) series3.getY(i);	
	}
	//点集先清空，为了做成新的点集而准备
	series1.clear();
	series2.clear();
	series3.clear();
	//将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
	//这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
	series1.add(addX, addY1);//在波动线的左边添加新的1个坐标点
	series2.add(addX, addY2);
	series3.add(addX, addY3);
	for (int k = 0; k < length; k++) {
	 series1.add(xv1[k], yv1[k]);
	 series2.add(xv2[k], yv2[k]);
	 series3.add(xv3[k], yv3[k]);//从临时的暂存空间中一次取出旧的坐标点，那么每一个刷新瞬间就会左右两端移动有21个坐标点能显示在图表上
	 }
	//在数据集中添加新的点集
	mDataset.addSeries(series1);//重新把特定一条线的点集加入到数据集中实现来让数据集和渲染器render的绑定
	mDataset.addSeries(series2);
	mDataset.addSeries(series3);
	//视图更新，没有这一步，曲线不会呈现动态
	//如果在非UI主线程中，需要调用postInvalidate()，具体参考api
	chart.invalidate();//原表废弃，新表生成，实现系统层次的刷新
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

	
	
	//enable the blue-tooth and create the device list dialog，一般的只会搜索到adruit的一个蓝牙模块，只会显示一个，但 是可能会有同时多个搜索到的低功耗蓝牙
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case 0:
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, 1);//带参数的启动
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

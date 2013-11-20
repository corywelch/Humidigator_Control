package com.example.thermpellercontrol;

import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.example.thermpellercontrol.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


//I apologize for the awful code, this was a school project. 

public class ControlActivity extends Activity implements OnClickListener, OnSeekBarChangeListener{

	Button Connect;
	ToggleButton OnOff;
	TextView Result;
	private String dataToSend;
	
	boolean powerOn = false;
	
	private static final String TAG = "Ian";

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static String address = "20:13:08:28:03:16";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private InputStream inStream = null;
    Handler handler = new Handler(); 
    byte delimiter = 10;
    boolean stopWorker = false;
    int readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];
	
    private TextView setHum;
    private SeekBar humSeek;
    private LinearLayout setHumLayout;
    
    ImageView powerButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		
		CheckBt();
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		Log.e("Ian", device.toString());
		
		Connect = (Button) findViewById(R.id.connect);
		powerButton = (ImageView) findViewById(R.id.power_button);
		Result = (TextView) findViewById(R.id.current_data);
		setHum = (TextView) findViewById(R.id.humidity);
		humSeek = (SeekBar) findViewById(R.id.humidity_seek);
		setHumLayout = (LinearLayout) findViewById(R.id.setHumLayout);


		Connect.setOnClickListener(this);
		powerButton.setOnClickListener(this);
		humSeek.setOnSeekBarChangeListener(this);
		
		powerButton.setVisibility(View.INVISIBLE);
		setHumLayout.setVisibility(View.INVISIBLE);
		humSeek.setVisibility(View.INVISIBLE);
					
		
	}

	
	private void CheckBt() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
					Toast.LENGTH_SHORT).show();
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth null !", Toast.LENGTH_SHORT)
					.show();
		}
	}
	
		public void Connect() {
			Log.d(TAG, address);
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			Log.d(TAG, "Connecting to ... " + device);
			mBluetoothAdapter.cancelDiscovery();
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
				btSocket.connect();
				Log.d(TAG, "Connection made.");
			} catch (IOException e) {
				try {
					btSocket.close();
				} catch (IOException e2) {
					Log.d(TAG, "Unable to end the connection");
				}
				Log.d(TAG, "Socket creation failed");
			}
			
			beginListenForData();
		}
	
	private void writeData(String data) {
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d(TAG, "Bug BEFORE Sending stuff", e);
		}

		String message = data;
		byte[] msgBuffer = message.getBytes();

		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			Log.d(TAG, "Bug while sending stuff", e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	
			try {
				btSocket.close();
			} catch (IOException e) {
			}
	}
	
	public void beginListenForData()   {
		 try {
				inStream = btSocket.getInputStream();
			} catch (IOException e) {
			}
		 
	        Thread workerThread = new Thread(new Runnable()
	        {
	            public void run()
	            {                
	               while(!Thread.currentThread().isInterrupted() && !stopWorker)
	               {
	                    try 
	                    {
	                        int bytesAvailable = inStream.available();                        
	                        if(bytesAvailable > 0)
	                        {
	                            byte[] packetBytes = new byte[bytesAvailable];
	                            inStream.read(packetBytes);
	                            for(int i=0;i<bytesAvailable;i++)
	                            {
	                                byte b = packetBytes[i];
	                                if(b == delimiter)
	                                {
	                                    byte[] encodedBytes = new byte[readBufferPosition];
	                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
	                                    final String data = new String(encodedBytes, "US-ASCII");
	                                    readBufferPosition = 0;
	                                    handler.post(new Runnable()
	                                    {
	                                        public void run()
	                                        {
	                                        	
	                                        	Result.setText(data);
	                                        	Result.setTextSize(25);
	                                        	
	                                        	/* You also can use Result.setText(data); it won't display multilines
	                                        	*/
	                                        	
	                                        }
	                                    });
	                                }
	                                else
	                                {
	                                    readBuffer[readBufferPosition++] = b;
	                                }
	                            }
	                        }
	                    } 
	                    catch (IOException ex) 
	                    {
	                        stopWorker = true;
	                    }
	               }
	            }
	        });

	        workerThread.start();
	    }

	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.control, menu);
		return true;

	}


	@Override
	public void onClick(View control) {
		// TODO Auto-generated method stub
		switch (control.getId()) {
		case R.id.connect:
				Connect();
				powerButton.setVisibility(View.VISIBLE);
				setHumLayout.setVisibility(View.VISIBLE);
				humSeek.setVisibility(View.VISIBLE);
				
				Connect.setVisibility(View.INVISIBLE);
			break;
		case R.id.power_button:
			if (!powerOn){
				powerOn = true;
				powerButton.setImageResource(R.drawable.humidigator_on);
				dataToSend = "1";
				writeData(dataToSend);
			}
			else if (powerOn){
				powerOn = false;
				powerButton.setImageResource(R.drawable.humidigator_off);
				dataToSend = "0";
				writeData(dataToSend);
			}

			break;
		}
	}


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		setHum.setText(Integer.toString(humSeek.getProgress()));

	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}

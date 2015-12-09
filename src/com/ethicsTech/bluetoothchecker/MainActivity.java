package com.ethicsTech.bluetoothchecker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import com.ethicsTech.bluetoothchecker.Connecting.BluetoothChatService;
import com.ethicsTech.bluetoothchecker.Connecting.Devices_List_For_Connection;
import com.ethicsTech.bluetoothchecker.Location.MyLocation;
import com.ethicsTech.bluetoothchecker.pair_and_Scan.DeviceListActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

//	Intent myintent=null;
	public static final String TAG="BLUETOOTH_KRISHNAN";
	private Button mPairedBtn;
	private Button mScanBtn;
	
	private Button mSelectBtn;
	
	private Button mVerify_device;
	
	private TextView selected_device_text;

	private ProgressDialog mProgressDlg;

	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	private BluetoothAdapter mBluetoothAdapter;

    private BluetoothChatService mChatService = null;
	
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
//	
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
//	
//	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
	
    
    private String mConnectedDeviceName = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVerify_device=(Button)findViewById(R.id.verify_device);
		selected_device_text=(TextView)findViewById(R.id.selected_devicename_text);
		mPairedBtn = (Button) findViewById(R.id.paired_device);
		mScanBtn = (Button) findViewById(R.id.scan_device);

		mSelectBtn=(Button)findViewById(R.id.select_device);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mProgressDlg = new ProgressDialog(this);

		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						mBluetoothAdapter.cancelDiscovery();
					}
				});

		if (mBluetoothAdapter == null) {

		} else {
			mPairedBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					 if (mBluetoothAdapter.isEnabled()) {
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
							.getBondedDevices();

					if (pairedDevices == null || pairedDevices.size() == 0) {
//						showToast("No Paired Devices Found");
					} else {
						ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

						list.addAll(pairedDevices);

						Intent intent = new Intent(MainActivity.this,
								DeviceListActivity.class);

						intent.putParcelableArrayListExtra("device.list", list);

						startActivity(intent);
					}
					}else{
//						showToast("BlueTooth is off");
					}
				}
			});

			mScanBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					 if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.startDiscovery();
					 }
				}
			});
			
			mSelectBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
		             Intent serverIntent = new Intent(MainActivity.this, Devices_List_For_Connection.class);
		            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
					
				}
			});
			mVerify_device.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					 sendMessage(""+android.os.Build.MODEL+"HELLO");
					 
					 // Get current time from Location 
					 String cu_time=getCurrentTime(getApplicationContext());
					 if(cu_time!=null){
						 sendMessage("::TIME::"+cu_time);
					 }
				}

			});
		}

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
	}

	
  
	 @Override
	    public void onStart() {
	        super.onStart();

	            if (mChatService == null) {
	            	setupChat();
	        }
	    }
	  
	 @Override
	    public synchronized void onResume() {
	        super.onResume();
	         Log.e(TAG, "+ ON RESUME +");

	        // Performing this check in onResume() covers the case in which BT was
	        // not enabled during onStart(), so we were paused to enable it...
	        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
	        if (mChatService != null) {
	            // Only if the state is STATE_NONE, do we know that we haven't started already
	            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
	              // Start the Bluetooth chat services
	              mChatService.start();
	            }
	        }
	    }
	
	



	private void setupChat() {

	        mChatService = new BluetoothChatService(this, mHandler);
		
	}

	   // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
           Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
//                	 showToast("STATE_CONNECTED :"+mConnectedDeviceName);
                    break;
                case BluetoothChatService.STATE_CONNECTING:

                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:

                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                
                Log.d(TAG+"BLUETOOTH WRITE", ""+writeMessage);
//                showToast(writeMessage);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG+"BLUETOOTH READ", ""+readMessage);
               
                
               if( readMessage.contains("TIME")){
//            	   showToast(readMessage);
               }
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                               Toast.LENGTH_SHORT).show();
                break;
            }
        }

	
    };

    private void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    
    
    
//    
//	private void showToast(String message) {
//		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
//				.show();
//	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
//					showToast("Enabled");
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				mDeviceList = new ArrayList<BluetoothDevice>();

				mProgressDlg.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				mProgressDlg.dismiss();

				Intent newIntent = new Intent(MainActivity.this,
						DeviceListActivity.class);

				newIntent.putParcelableArrayListExtra("device.list",
						mDeviceList);

				startActivity(newIntent);
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = (BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDeviceList.add(device);
//				showToast("Found device " + device.getName());
			}
		}
	};
	
	
	
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	 connectDevice(data, true);
            	setname_to_textview(data);
                
            }
            break;
        }
    }
    
    
    
    
    
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(Devices_List_For_Connection.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    
    private void setname_to_textview(Intent data) {
    	String address = data.getExtras().getString(Devices_List_For_Connection.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        selected_device_text.setText(""+device.getName());
		
	}

    

	private String getCurrentTime(Context context) {
		String text=null;
		MyLocation my=new MyLocation(getApplicationContext());
		Location l=my.getLocation();
		if(l!=null){
			Log.d("", ""+l.getTime());
			long time = l.getTime();
	    	Date date = new Date(time);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	 text = sdf.format(date);
	    	System.out.println(text); // prints something like 2011-01-08 13:35:48
	    	Log.d("prints something like 2011-01-08 13:35:48", ""+text);
		return text;
	}
		return text;
	}
}
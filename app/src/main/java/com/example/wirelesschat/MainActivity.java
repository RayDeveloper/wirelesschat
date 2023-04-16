package com.example.wirelesschat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("NewApi")
    private static final android.icu.text.SimpleDateFormat sdf3 = new android.icu.text.SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");// time format
    Date parsed_receivedDate= new Date();
    Date currentTime_check = new Date();
    Button btnOnOff, btnDiscover, btnSend;
    ListView listView, listViewDevices, RB0;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;
    View v;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    WifiP2pDevice connectedDevice;



    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;



    static final int MESSAGE_READ=1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initialWork();
        exqListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.onOff:
                if(wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                    //btnOnOff.setText("ON");
                }else {
                    wifiManager.setWifiEnabled(true);
                    //btnOnOff.setText("OFF");
                }
                return true;
            case R.id.discover:


            return true;



            default:
                    return super.onOptionsItemSelected(item);
        }
    }
    Handler handler=new Handler(new Handler.Callback() {
        @Override

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    //String current_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());//dont need
                    System.out.println("The message read:" + tempMsg);//dont need
                    String[] arr = null;
                    arr = tempMsg.split(" ");
                    String received_date = arr[arr.length - 1];
                    System.out.println("Date from array:" + arr[arr.length - 1]);//dont need
                    //Date parsed_receivedDate = null;
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            parsed_receivedDate = sdf3.parse(received_date);
                            currentTime_check = Calendar.getInstance().getTime();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    System.out.println("parsed_receivedDate:" + parsed_receivedDate);//dont need
                    System.out.println("currentTime_check:" + currentTime_check);//dont need
                    String elapsedTime = printDifference(parsed_receivedDate, currentTime_check);
                    read_msg_box.setText(tempMsg);
                    System.out.println("Elapsed Time:" + elapsedTime);//dont need
                    Toast.makeText(MainActivity.this, "Elapsed Time:" + elapsedTime, Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        }
    });

    private void exqListener() {
//        btnOnOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(wifiManager.isWifiEnabled())
//                {
//                    wifiManager.setWifiEnabled(false);
//                    btnOnOff.setText("ON");
//                }else {
//                    wifiManager.setWifiEnabled(true);
//                    btnOnOff.setText("OFF");
//                }
//            }
//        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                        //connectionStatus.setText("Discovery Started");
                        Toast.makeText(MainActivity.this, "Looking for devices", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        //connectionStatus.setText("Discovery Starting Failed");
                        Toast.makeText(MainActivity.this, "Unable to look for peers", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device=deviceArray[i];
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;


                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                        setSubStatus("You are CLIENT");
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_SHORT).show();
                        setSubStatus("Not Connected ");
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg=writeMsg.getText().toString();
                String date_read = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
                String combined_message = msg+ "\n Timestamp:"+ " "+date_read;
                System.out.println("The message sent:"+combined_message);
                sendReceive.write(combined_message.getBytes());
            }
        });


    }

    public String printDifference(Date startDate, Date endDate) { //method to calculate the difference between the sending time and received time.
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : "+ endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        return elapsedDays+"days,"+elapsedHours+"hours," +elapsedMinutes+"minutes,"+elapsedSeconds+"seconds," +different+"milliseconds" ;
    }



    private final void setSubStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private final void settitle(CharSequence Title) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Title);
    }

    private void initialWork() {
        btnOnOff=(Button) findViewById(R.id.onOff);
        btnDiscover=(Button) findViewById(R.id.discover);
        btnSend=(Button) findViewById(R.id.sendButton);
        listView=(ListView) findViewById(R.id.peerListView);
        //listViewDevices=(ListView) findViewById(R.id.lvDevice);
        read_msg_box=(TextView) findViewById(R.id.readMsg);
        connectionStatus=(TextView) findViewById(R.id.connectionStatus);
        writeMsg=(EditText) findViewById(R.id.writeMsg);
        //RB0 = (ListView) findViewById(R.id.lvDevice);
        wifiManager= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager= (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel=mManager.initialize(this,getMainLooper(),null);

        mReceiver=new WiFiDirectBroadcastReceiver(mManager, mChannel,this);

        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
    }

    WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray=new String[peerList.getDeviceList().size()];
                deviceArray=new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;

                for(WifiP2pDevice device : peerList.getDeviceList())
                {
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }

                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }

            if(peers.size()==0)
            {
                Toast.makeText(getApplicationContext(),"No Device Found",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress=wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)

            {
                connectionStatus.setText("Connected");

                setSubStatus("You are HOST ");
                //Toast.makeText(MainActivity.this, connectedDevice, Toast.LENGTH_SHORT).show();
                serverClass=new ServerClass();
                serverClass.start();
            }else if(wifiP2pInfo.groupFormed) {
                connectionStatus.setText("Connected");
                //setSubStatus("You are Client connected to: "+wifiP2pInfo.groupOwnerAddress.getHostName());
                clientClass=new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;


        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        String device = WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;
        Socket socket;
        String hostAdd;

        public  ClientClass(InetAddress hostAddress)
        {
            hostAdd=hostAddress.getHostAddress();
            socket=new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
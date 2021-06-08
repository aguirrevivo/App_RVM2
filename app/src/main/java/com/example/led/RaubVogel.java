package com.example.led;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;


public class RaubVogel extends AppCompatActivity {
    //Widget Variables
    Button btnOn, btnOff, btnDis;
    SeekBar speed;
    String address = null;
    TextView speedVal;
    Switch modo;
    //TextView prueba;

    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //termina tuto var gggg

    //data para empaquetar
    String ps="0",md="0",vvv ="000",strO="0";
    //


    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raub_vogel);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

//view of the RaubVogel layout
        setContentView(R.layout.activity_raub_vogel);
//call the widgtes
        btnOn = (Button)findViewById(R.id.button2);//boton inicio
        btnOff = (Button)findViewById(R.id.button3);//boton parro
        btnDis = (Button)findViewById(R.id.button4);//boton de desconexión
        speed = (SeekBar)findViewById(R.id.seekBar);//speed cambio de brigtness
        speedVal = (TextView) findViewById(R.id.speedText);
        //prueba = (TextView) findViewById(R.id.textPrueba);
        modo= (Switch) findViewById(R.id.switch1);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();//envía datos para inicio de secuencia
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();//paro de emergencia
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();//close connection
            }
        });

        //seekBar speed
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    speedVal.setText(String.valueOf(progress)+" km/h") ;
                    vvv=String.valueOf(progress);

                    switch (vvv.length()){
                        case 0:
                            vvv="000";
                            break;
                        case 1:
                            vvv="00"+vvv;
                            break;
                        case 2:
                            vvv="0"+vvv;
                            break;
                        case 3:
                            vvv= vvv;
                            break;
                    }

                    sendDataInst();

                }

            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        }); //Seekbar speed

        //Switch

        modo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modo.isChecked()){
                    //aquí se empaquetan los datos
                    md="1"; //modo automático
                }
                else{
                    //se empaqueta nuevamente pero con edo distinto
                    md="0";//Modo manual
                }
                sendDataInst();
            }
        });//Switch

    }//On create
    //controles

    private void Disconnect() {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void turnOffLed() {
        //btSocket.getOutputStream().write("TF".toString().getBytes());
        ps="1";//paro de emergencia
        strO="0";//desactiva el inicio
        sendDataInst();
    }

    private void turnOnLed() {
        //btSocket.getOutputStream().write("TO".toString().getBytes());
        ps="0";//quita el estado de paro de emergencia
        strO="1";//inicia op.
        sendDataInst();
    }


    private void sendDataInst() {
        String dataInst= "#"+ps+md+strO+vvv+"!";
        //# + paro de emergencia+modo de operació+inicio+velocidad+!
        //prueba.setText(dataInst);
        //btSocket.getOutputStream().write(dataInst.toString().getBytes());//envía los datos BT


        //El bueno B)
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(dataInst.toString().getBytes());//envía los datos BT
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

    }
    //controles

    //message en Disconnect
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }//message en disconect

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_raub_vogel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Start BT Connection
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(RaubVogel.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }//Start BT Connection



}//RaubVogel
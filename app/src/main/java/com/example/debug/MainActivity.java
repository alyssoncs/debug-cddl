package com.example.debug;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import br.pucrio.inf.lac.mhub.s2pa.technologies.TechnologyID;
import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.message.ObjectFoundMessage;
import br.ufma.lsdi.cddl.network.MicroBroker;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private CDDL cddl;
    private Subscriber subscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPermissions();
    }

    public void startCDDL(View view) {
        init();
    }

    public void stopCDDL(View view) {
        if (cddl != null) {
            subscriber.unsubscribeAll();

            cddl.stopAllCommunicationTechnologies();
            cddl.stopService();
            cddl.getConnection().disconnect();

            MicroBroker.getInstance().stop();
        }
    }

    public void init() {
        initMicroBroker();
        initCDDL();
        initSubscriber();
    }

    private void initMicroBroker() {
        MicroBroker microBroker = MicroBroker.getInstance();
        microBroker.start();
    }

    private void initCDDL() {
        Connection connection = ConnectionFactory.createConnection();
        connection.setClientId("alysson.cirilo@lsdi.ufma.br");
        connection.setHost(Connection.MICRO_BROKER_LOCAL_HOST);
        connection.connect();

        cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);
        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.BLE_TECHNOLOGY_ID); /* start bluetooth scan */
    }

    private void initSubscriber() {
        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());
        subscriber.subscribeObjectFoundTopic();

        subscriber.setSubscriberListener(new ISubscriberListener() {
            /* all the bluetooth rendezvous will arrive here */
            @Override
            public void onMessageArrived(Message message) {
                if (message instanceof ObjectFoundMessage) {
                    String moouid = message.getMouuid();
                    if (moouid != null) {
                        Log.d(TAG, "onMessageArrived: " + moouid);
                    }
                }

            }
        });
    }

    private void setPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}

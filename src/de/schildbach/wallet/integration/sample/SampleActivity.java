/**
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schildbach.wallet.integration.sample;


import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.schildbach.wallet.integration.android.BitcoinIntegration;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import android.os.RemoteException;


import android.view.Gravity;
import android.widget.Toast;

/**
 * @author Andreas Schildbach
 */
public class SampleActivity extends Activity
{
	
	private String TAG = "Mumble";
	
	private static final String[] DONATION_ADDRESSES_MAINNET = { "13pEqnZUMFkDGiBSQyvrnxpdNteXqoBosm" };
	private static final String[] DONATION_ADDRESSES_TESTNET = { "mkCLjaXncyw8eSWJBcBtnTgviU85z5PfwS", "mwEacn7pYszzxfgcNaVUzYvzL6ypRJzB6A" };
	private static final int REQUEST_CODE = 0;

	private Button donateButton;
	private TextView donateMessage;
	
	
		/*
		 * 	Begin the part for the beacons here
		 * 	*/
	
	 private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

	  private BeaconManager beaconManager = new BeaconManager(this);
	  
	  private Beacon beacon;
	  private Region region = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
	  
	  
	  
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{


		Log.d(TAG,"Application Staarted");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sample_activity);

		donateButton = (Button) findViewById(R.id.sample_donate_button);
		donateButton.setEnabled(false);
		
		donateButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(final View v)
			{
				handleDonate();
			}
		});

		donateMessage = (TextView) findViewById(R.id.sample_donate_message);

		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
		   
			@Override
			public void onBeaconsDiscovered(Region arg0, List<Beacon> beacons) {
				// TODO Auto-generated method stub
				//Log.d(TAG, "Ranged beacons: " + beacons);
				for (Beacon rangedBeacon : beacons) {
					//Log.d(TAG, "Ranged Beacon MAC: " + rangedBeacon.getMacAddress());
		              if (rangedBeacon.getMacAddress().equals("D1:94:6D:B1:03:EE")) {
		            	  Log.d(TAG, "Ranged Beacon MAC: " + rangedBeacon.getMacAddress());
		            	  	// Create a Toast box with Information about the beacon
		            	  ShowToast("Ranged Beacon MAC: " + rangedBeacon.getMacAddress());
		            	  donateButton.setEnabled(true);
		            	  
		              } //Here enable and disable that button based on if Beacon is there or not.
		              
		            }
			    //
			}
		  });
	}
	private void ShowToast(String msg )
	{
	Toast toast = Toast.makeText(SampleActivity.this, msg,
	Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, 0, 180);
	toast.show();
	}
	@Override public void onStart(){
		super.onStart();
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
		    @Override public void onServiceReady() {
		      try {
		        beaconManager.startRanging(region);
		      } catch (RemoteException e) {
		        Log.e(TAG, "Cannot start ranging", e);
		      }
		    }
		  });
	}
		
	@Override public void onStop(){
		super.onStop();
		 try {
			    beaconManager.stopRanging(region);
			  } catch (RemoteException e) {
			    Log.e(TAG, "Cannot stop but it does not matter now", e);
			  }
	}
		
	@Override public void onDestroy(){
		super.onDestroy();
			beaconManager.disconnect();
	}
		
	private String[] donationAddresses()
	{
		final boolean isMainnet = true;//((RadioButton) findViewById(R.id.sample_network_mainnet)).isChecked();

		return isMainnet ? DONATION_ADDRESSES_MAINNET : DONATION_ADDRESSES_TESTNET;
	}

	private void handleDonate()
	{
		final String[] addresses = donationAddresses();

		BitcoinIntegration.requestForResult(SampleActivity.this, REQUEST_CODE, addresses[0]);
		Log.d(TAG,"Request Sent");
	}
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (requestCode == REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				final String txHash = BitcoinIntegration.transactionHashFromResult(data);
				if (txHash != null)
				{
					final SpannableStringBuilder messageBuilder = new SpannableStringBuilder("Transaction hash:\n");
					messageBuilder.append(txHash);
					messageBuilder.setSpan(new TypefaceSpan("monospace"), messageBuilder.length() - txHash.length(), messageBuilder.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					if (BitcoinIntegration.paymentFromResult(data) != null)
						messageBuilder.append("\n(also a BIP70 payment message was received)");
					Log.d(TAG,"Data From coins:"+data.toString());
					donateMessage.setText(messageBuilder);
					donateMessage.setVisibility(View.VISIBLE);
				}

				Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
			}
			else if (resultCode == Activity.RESULT_CANCELED)
			{
				Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(this, "Unknown result.", Toast.LENGTH_LONG).show();
			}
		}
	}
}

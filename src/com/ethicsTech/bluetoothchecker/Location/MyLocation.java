package com.ethicsTech.bluetoothchecker.Location;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyLocation implements LocationListener {
    Context mContext;
	Location location = null; 
	public double latitude;
	public double longitude;
	protected LocationManager locationManager;
	public MyLocation(Context context){
		 this.mContext = context;
		 getLocation();
	}
	
	
	public Location getLocation() {
	    try { 
			        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	                if (location == null) {
	                	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1, 1, MyLocation.this);           	
	                    if (locationManager != null) {
	                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                        Log.v(""+locationManager.getAllProviders(),""+locationManager.getAllProviders());
	                        
	                        if (location != null) {
	                        	
	                        	  return location;
	                        
	                        } 
	                    } 
	                } 
	 
	                
	                
	                
	                
	                
	                
	                
	                
	                List<String> matchingProviders = locationManager.getAllProviders();
	                for (String provider: matchingProviders) {
	                  Location location = locationManager.getLastKnownLocation(provider);
	                  if (location != null) {
	                    return location;
	                  }
	                }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	 
	    return location;
	}
	
	

public void stopUsingGPS() { 
    if (locationManager != null) {
        locationManager.removeUpdates(MyLocation.this);
    } 
} 
	
	@Override
	public void onLocationChanged(Location location) {
		 if (location != null) {
//		        latitude = location.getLatitude();
//		        longitude = location.getLongitude();
		    } 
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

}
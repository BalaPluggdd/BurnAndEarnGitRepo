package com.pluggdd.burnandearn.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AlertNotification
{
	public void showAlertPopup(final Activity context, String title,String message, final boolean shouldFinish)
	{
		AlertDialog.Builder alertPopUpBuilder = new AlertDialog.Builder(context);
		alertPopUpBuilder.setTitle(title);
		alertPopUpBuilder.setMessage(message);
		alertPopUpBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialogInterface, int position){
				dialogInterface.dismiss();
				if(shouldFinish)
					context.finish();
			}
		});
		alertPopUpBuilder.show();
	}
}


/*
 * Copyright (c) 2015. - Alexis Lecanu (alexis.lecanu@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.alecanu.samplerssreader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static ProgressDialog mProgressDialog;

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo item : info) {
                    if (item.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void showProgressDialog(Context context, DialogInterface.OnCancelListener listener) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getResources().getString(R.string.waiting));
        mProgressDialog.setOnCancelListener(listener);
        mProgressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

    }

    public static void showSimpleInformationDialogBox(Context context, int messageResId, int okStringId) {
        Utils.showSimpleInformationDialogBox(context, context.getResources().getString(messageResId),
                context.getResources().getString(okStringId));
    }

    public static void showSimpleInformationDialogBox(Context context, String message, String okString) {
        dismissProgressDialog();
        AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(false).setMessage(message)
                .setNeutralButton(okString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public static Date stringToDate(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date simpleDate;
        try {
            simpleDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            simpleDate = new Date();
        }
        return simpleDate;
    }

    public static String dateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy");
        return formatter.format(date);
    }

    public static void deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                deleteDir(new File(dir, child));
            }
        } else if (dir != null) {
            dir.delete();
        }
    }

    public static void deletePicassoCache(Context context) {
        File dir = context.getCacheDir();
        if (dir != null && dir.isDirectory()) {
            File picasso = new File(dir, "picasso-cache");
            deleteDir(picasso);
        }
    }
}

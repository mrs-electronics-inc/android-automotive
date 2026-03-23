package com.mrselectronics.usernotice;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

public final class BasicUserNoticeService extends Service {
    private static final String IUSER_NOTICE_BINDER_DESCRIPTOR = "android.car.user.IUserNotice";
    private static final int IUSER_NOTICE_ON_DIALOG_DISMISSED =
            IBinder.FIRST_CALL_TRANSACTION;
    private static final String IUSER_NOTICE_UI_BINDER_DESCRIPTOR = "android.car.user.IUserNoticeUI";
    private static final int IUSER_NOTICE_UI_SET_CALLBACK =
            IBinder.FIRST_CALL_TRANSACTION;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Object mLock = new Object();

    private AlertDialog mDialog;
    private IBinder mCarServiceCallback;

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            if (code == IBinder.INTERFACE_TRANSACTION) {
                if (reply != null) {
                    reply.writeString(IUSER_NOTICE_UI_BINDER_DESCRIPTOR);
                }
                return true;
            }
            if (code == IUSER_NOTICE_UI_SET_CALLBACK) {
                data.enforceInterface(IUSER_NOTICE_UI_BINDER_DESCRIPTOR);
                onCarServiceCallbackSet(data.readStrongBinder());
                if (reply != null) {
                    reply.writeNoException();
                }
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMainHandler.post(this::dismissDialog);
        return false;
    }

    @Override
    public void onDestroy() {
        mMainHandler.post(this::dismissDialog);
        super.onDestroy();
    }

    private void onCarServiceCallbackSet(IBinder callback) {
        synchronized (mLock) {
            mCarServiceCallback = callback;
        }
        mMainHandler.post(this::showDialog);
    }

    private void showDialog() {
        synchronized (mLock) {
            if (mDialog != null) {
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog_Alert))
                    .setTitle(R.string.user_notice_title)
                    .setMessage(R.string.user_notice_message)
                    .setCancelable(false)
                    .setPositiveButton(
                            R.string.user_notice_continue,
                            (unusedDialog, which) -> notifyDismissedAndClose())
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }
            dialog.show();
            mDialog = dialog;
        }
    }

    private void notifyDismissedAndClose() {
        IBinder callback;
        synchronized (mLock) {
            callback = mCarServiceCallback;
        }

        if (callback != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IUSER_NOTICE_BINDER_DESCRIPTOR);
                callback.transact(IUSER_NOTICE_ON_DIALOG_DISMISSED, data, reply, 0);
                reply.readException();
            } catch (RemoteException ignored) {
                // CarService treats disconnects as the UI going away.
            } finally {
                data.recycle();
                reply.recycle();
            }
        }

        dismissDialog();
        stopSelf();
    }

    private void dismissDialog() {
        synchronized (mLock) {
            if (mDialog == null) {
                return;
            }
            mDialog.dismiss();
            mDialog = null;
        }
    }
}

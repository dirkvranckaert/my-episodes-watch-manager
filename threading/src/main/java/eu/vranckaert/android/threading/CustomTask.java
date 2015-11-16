package eu.vranckaert.android.threading;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import eu.vranckaert.android.general.KeyboardHelper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Date: 14/04/14
 * Time: 14:44
 *
 * @author Dirk Vranckaert
 */
public abstract class CustomTask<T extends Object> {

    private final Context context;

    private CustomThread t;

    private int minTimeForBackgroundThread = 0;
    private boolean showErrorDialog;
    private boolean errorCancellable;
    private String errorTitle;
    private String errorMessage;
    private boolean errorAllowRetry;
    private String retryButton;
    private String customRetryButton;
    private String retryCancelButton;
    private String okButton;

    private boolean mRefreshing;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AlertDialog loadingDialog;
    private AlertDialog alertDialog;
    private Exception exception;

    private boolean stopped = false;

    public CustomTask(Activity activity) {
        this.context = activity;
        setShowErrorDialog(true);
        setErrorCancellable(true);
        setRetryButton(R.string.general_retry_button);
        setRetryCancelButton(R.string.general_retry_cancel_button);
        setOkButton(R.string.general_ok);
    }

    public CustomTask(Context context) {
        this.context = context;
        setShowErrorDialog(false);
        setErrorCancellable(true);
        setRetryButton(R.string.general_retry_button);
        setRetryCancelButton(R.string.general_retry_cancel_button);
        setOkButton(R.string.general_ok);
    }

    public Context getContext() {
        return context;
    }

    public final void execute() {
        exception = null;

        if (stopped) {
            return;
        }

        onPreExecute();

        if (stopped) {
            return;
        }

        t = new CustomThread(new Handler());
        t.start();
    }

    public void onError(Exception exception) {

    }

    public void onTaskCompleted(T result) {
    }

    public ErrorMapping getErrorMapping(Exception e) {
        return null;
    }

    public final void attachSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        mSwipeRefreshLayout = swipeRefreshLayout;
        if (mRefreshing) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    public final void detachSwipeRefreshLayout() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
        mSwipeRefreshLayout = null;
    }

    private void dismissLoadingDialog() {
        mRefreshing = false;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void onPreExecute() {
        mRefreshing = true;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        } else if (isProgressTask() && context != null) {
            LoadingView loadingView = new LoadingView(context);
            loadingView.setMessage(getLoadingMessage());
            if (context instanceof Activity) {
                KeyboardHelper.hideKeyboard(((Activity) context).getWindow().getDecorView(), context);
            }

            loadingDialog = new AlertDialog.Builder(context)
                    .setView(loadingView.getView())
                    .setCancelable(false)
                    .show();
        } else {
            loadingDialog = null;
        }

        preExecute();
    }

    protected boolean isProgressTask() {
        return false;
    }

    public void preExecute() {
    }

    private void retry() {
        if (showErrorDialog) {
            if (stopped) {
                return;
            }

            String retryButtonText = retryButton;
            if (!TextUtils.isEmpty(customRetryButton)) {
                retryButtonText = customRetryButton;
                customRetryButton = null;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(errorMessage);
            builder.setCancelable(errorCancellable);
            if (errorAllowRetry) {
                builder.setPositiveButton(retryButtonText, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveRetry();
                    }
                });
                if (errorCancellable) {
                    builder.setNegativeButton(retryCancelButton, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onNegativeRetry();
                        }
                    });
                }
            } else {
                builder.setPositiveButton(okButton, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveOnly();
                    }
                });
            }
            if (!TextUtils.isEmpty(getNeutralText())) {
                builder.setNeutralButton(getNeutralText(), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onNeutral();
                    }
                });
            }

            if (!TextUtils.isEmpty(errorTitle)) {
                builder.setTitle(errorTitle);
            }
            alertDialog = builder.show();
            if (errorCancellable) {
                alertDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onNegativeRetry();
                    }
                });
            }
        } else {
            exception = null;
        }
    }

    private void onNegativeRetry() {
        dismissAlertDialog();
        onNegativeRetryClicked();
        exception = null;
    }

    private void onPositiveRetry() {
        stopped = false;
        dismissAlertDialog();
        if (onPositiveRetryClicked()) {
            execute();
        }
        exception = null;
    }

    private void onPositiveOnly() {
        dismissAlertDialog();
        onPositiveClicked();
        exception = null;
    }

    private void onNeutral() {
        dismissAlertDialog();
        onNeutralClicked();
        exception = null;
    }

    public final Exception getException() {
        return exception;
    }

    public abstract T doInBackground() throws Exception;

    public void onNegativeRetryClicked() {

    }

    public boolean onPositiveRetryClicked() {
        return true;
    }

    public void onPositiveClicked() {
    }

    public void onNeutralClicked() {
    }

    public final void cancel() {
        if (stopped) {
            return;
        }

        Log.d(CustomTask.class.getSimpleName(), "Cancelling task");

        onCancel();
        stopped = true;
        if (t != null) {
            t.cancel();
        }

        dismissLoadingDialog();
        dismissAlertDialog();
        //t = null;
    }

    public void onCancel() {

    }

    public String getLoadingMessage() {
        return context.getString(getLoadingMessageResId());
    }

    @ColorRes
    public int getProgressColor() {
        return 0;
    }

    public int getLoadingMessageResId() {
        return R.string.general_loading;
    }

    public String getNeutralText() {
        return null;
    }

    public void setMinTimeForBackgroundThread(int minTimeForBackgroundThread) {
        this.minTimeForBackgroundThread = minTimeForBackgroundThread;
    }

    public final String getString(@StringRes int stringResId) {
        return getContext().getString(stringResId);
    }

    public final void setShowErrorDialog(boolean showErrorDialog) {
        this.showErrorDialog = showErrorDialog;
    }

    public void setErrorCancellable(boolean errorCancellable) {
        this.errorCancellable = errorCancellable;
    }

    public final void setErrorTitle(int errorTitleResId) {
        setErrorTitle(context.getString(errorTitleResId));
    }

    public final void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public final void setErrorMessage(int errorMessageResId) {
        setErrorMessage(context.getString(errorMessageResId));
    }

    public final void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isErrorAllowRetry() {
        return errorAllowRetry;
    }

    public void setErrorAllowRetry(boolean errorAllowRetry) {
        this.errorAllowRetry = errorAllowRetry;
    }

    public final void setRetryButton(int retryButtonResId) {
        setRetryButton(context.getString(retryButtonResId));
    }

    public final void setRetryButton(String retryButton) {
        this.retryButton = retryButton;
    }

    public final void setRetryCancelButton(int retryCancelButtonResId) {
        setRetryCancelButton(context.getString(retryCancelButtonResId));
    }

    public final void setRetryCancelButton(String retryCancelButton) {
        this.retryCancelButton = retryCancelButton;
    }

    public final void setOkButton(int okButtonResId) {
        setOkButton(context.getString(okButtonResId));
    }

    public final void setOkButton(String okButton) {
        this.okButton = okButton;
    }

    public final boolean isTaskStopped() {
        return stopped;
    }

    private class CustomThread extends Thread {
        private final Handler handler;
        private boolean stopped = false;
        private long start;
        private long end;

        public CustomThread(Handler handler) {
            this.handler = handler;
        }

        public void cancel() {
            stopped = true;
        }

        @Override
        public void run() {
            if (stopped) {
                return;
            }

            ErrorMapping errorMapping = null;
            try {
                start = new Date().getTime();
                final T result = doInBackground();

                if (stopped) {
                    return;
                }

                end = new Date().getTime();
                long duration = end - start;
                if (minTimeForBackgroundThread > 0 && duration < minTimeForBackgroundThread) {
                    Thread.sleep(minTimeForBackgroundThread - duration);
                }

                if (stopped) {
                    return;
                }

                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            onTaskCompleted(result);
                        }
                    });
                }
                return;
            } catch (UnknownHostException e) {
                end = new Date().getTime();
                errorMapping =
                        new ErrorMapping.Builder().setTitle(context.getString(R.string.general_error_offline_title))
                                .setMessage(context.getString(R.string.general_error_offline_message))
                                .setTryAgain(true)
                                .build();
                exception = e;
            } catch (IOException e) {
                end = new Date().getTime();
                errorMapping =
                        new ErrorMapping.Builder().setTitle(context.getString(R.string.general_error_web_title))
                                .setMessage(context.getString(R.string.general_error_web_message))
                                .setTryAgain(true)
                                .build();
                exception = e;
            } catch (Exception e) {
                end = new Date().getTime();
                errorMapping = getErrorMapping(e);
                exception = e;
            } finally {
                if (exception != null) {
                    if (errorMapping == null) {
                        String message = exception.getClass().getSimpleName() + " (" + end + ")";
                        setErrorMessage(message);
                        setErrorAllowRetry(true);
                    } else {
                        setErrorTitle(errorMapping.getTitle());
                        setErrorMessage(errorMapping.getMessage());
                        setErrorAllowRetry(errorMapping.isTryAgain());
                        customRetryButton = errorMapping.getRetryButton();
                    }
                }
            }

            Log.e(CustomTask.class.getSimpleName(), "Error during custom task", exception);

            if (stopped) {
                return;
            }

            long duration = end - start;
            if (minTimeForBackgroundThread > 0 && duration < minTimeForBackgroundThread) {
                try {
                    Thread.sleep(minTimeForBackgroundThread - duration);
                } catch (InterruptedException e) {
                }
            }

            if (stopped) {
                return;
            }

            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingDialog();
                        onError(exception);
                        retry();
                    }
                });
            }
        }
    }
}

package com.icfi.cordova.geofence;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cowbell.cordova.geofence.GeofencePlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RetryFailedRequestsService extends Service {

    private FailedRequestsStorage failedRequestsStorage;
    private ExecutorService executor;
    private BlockingQueue<Runnable> mTaskQueue;

    @Override
    public void onCreate() {
        Log.d(GeofencePlugin.TAG, "onCreate() of RetryFailedRequestsService has been called!");
        super.onCreate();
        failedRequestsStorage = new FailedRequestsStorage(this);

       /* executor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, null) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    Log.e(GeofencePlugin.TAG, "Error occurred during retry of failed request.", t);
                    return;
                }

                RetryFailedRequestTask task = (RetryFailedRequestTask) r;
                failedRequestsStorage.removeFailedRequest(task.getRequestId());
            }
        };*/

        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(GeofencePlugin.TAG, "onStartCommand() of RetryFailedRequestsService has been called!");
        try {

            List<RetryFailedRequestTask> allFailedRequestTasks = getAllFailedRequestTasks();

            Log.d(GeofencePlugin.TAG, "about to invokeAll() on our executor: " + executor);
            List<Future<Boolean>> futures = executor.invokeAll(allFailedRequestTasks);

            Log.d(GeofencePlugin.TAG, "number of futures created: " + futures.size());
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Boolean resultSuccess = futures.get(i).get();

                    Log.d(GeofencePlugin.TAG, "the result success: " + resultSuccess);
                    if (resultSuccess) {
                        failedRequestsStorage.removeFailedRequest(allFailedRequestTasks.get(i).getRequestId());
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    private List<RetryFailedRequestTask> getAllFailedRequestTasks() {
        Log.d(GeofencePlugin.TAG, "getAllFailedRequestTasks() has been called.");
        List<RetryFailedRequestTask> retryFailedRequestTasks = new ArrayList<RetryFailedRequestTask>();
        List<FailedRequest> allFailedRequests = failedRequestsStorage.getAllFailedRequests();

        Log.d(GeofencePlugin.TAG, "num failed requests: " + allFailedRequests.size());
        for (FailedRequest failedRequest : allFailedRequests) {
            retryFailedRequestTasks.add(new RetryFailedRequestTask(failedRequest));
        }

        return retryFailedRequestTasks;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class RetryFailedRequestTask implements Callable<Boolean> {
        private FailedRequest failedRequest;

        public RetryFailedRequestTask(FailedRequest failedRequest) {
            this.failedRequest = failedRequest;
        }

        @Override
        public Boolean call() throws Exception {
            Log.d(GeofencePlugin.TAG, "call() of " + this + " has been called!");
            return HttpRequests.postGeofenceEvent(failedRequest.getCircuitGeofenceEvent(), failedRequest.getDeviceId())
                    .created();
        }

        public String getRequestId() {
            return failedRequest.getRequestId();
        }
    }
}

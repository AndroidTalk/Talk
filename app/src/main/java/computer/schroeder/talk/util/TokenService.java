package computer.schroeder.talk.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class TokenService extends FirebaseMessagingService
{
    /**
     * Requests a new firebase instance id token and sends it to the backend
     * @param context used to request the token and access the backend
     */
    public void updateToken(final Context context)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                FirebaseApp.initializeApp(context);
                FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task)
                        {
                            if (!task.isSuccessful()) return;

                            // Get new Instance ID token
                            final String token = task.getResult().getToken();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    new RestService(context).updateFCMToken(token);
                                }
                            }).start();
                        }
                    });
            }
        }).start();
    }

    /**
     * Sends a new token to the backend
     * @param token the new token
     */
    @Override
    public void onNewToken(final String token)
    {
        super.onNewToken(token);
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RestService(TokenService.this).updateFCMToken(token);
            }
        }).start();
    }
}

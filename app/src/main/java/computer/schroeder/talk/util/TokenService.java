package computer.schroeder.talk.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class TokenService extends FirebaseMessagingService
{
    public void updateToken(final Context context)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
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
                                    new ServerConnection(context).updateFCMToken(token);
                                }
                            }).start();
                        }
                    });
            }
        }).start();
    }

    @Override
    public void onNewToken(final String s)
    {
        super.onNewToken(s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                new ServerConnection(TokenService.this).updateFCMToken(s);
            }
        }).start();
    }
}

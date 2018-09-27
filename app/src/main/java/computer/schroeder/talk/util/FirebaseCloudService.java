package computer.schroeder.talk.util;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCloudService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    Util.sync(FirebaseCloudService.this);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

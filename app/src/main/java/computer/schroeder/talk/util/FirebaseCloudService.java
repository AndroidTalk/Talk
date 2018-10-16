package computer.schroeder.talk.util;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCloudService extends FirebaseMessagingService
{
    /**
     * Called whenever firebase received a new message from the backend
     * the function will then update the internal storage
     * @param remoteMessage the received messages (NOT the actual message, this object is not needed)
     */
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

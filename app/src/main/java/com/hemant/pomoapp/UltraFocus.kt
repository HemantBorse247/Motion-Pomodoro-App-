package com.hemant.pomoapp

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class UltraFocus : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }
}
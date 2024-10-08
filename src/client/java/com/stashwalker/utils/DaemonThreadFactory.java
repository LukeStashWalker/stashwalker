package com.stashwalker.utils;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread (Runnable r) {

            Thread t = new Thread(r);
            t.setDaemon(true); // Mark thread as daemon

            return t;
        }
    }

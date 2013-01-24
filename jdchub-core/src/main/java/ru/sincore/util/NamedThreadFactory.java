/*
* NamedThreadFactory.java
*
* Created on 24 01 2013, 11:48
*
* Copyright (C) 2013 Alexey 'lh' Antonov
*
* For more info read COPYRIGHT file in project root directory.
*
*/

package ru.sincore.util;

/**
 * @author Alexey 'lh' Antonov
 * @since 2013-01-24
 */

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The named thread factory based on default thread factory
 */
public class NamedThreadFactory implements ThreadFactory
{
    static final AtomicInteger poolNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;


    public NamedThreadFactory(String name)
    {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = name + "-" +
                     poolNumber.getAndIncrement() +
                     "-thread-";
    }


    public Thread newThread(Runnable r)
    {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}

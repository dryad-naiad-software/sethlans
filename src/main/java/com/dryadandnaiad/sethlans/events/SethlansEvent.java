package com.dryadandnaiad.sethlans.events;

import org.springframework.context.ApplicationEvent;

/**
 * Created Mario Estrella on 10/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansEvent extends ApplicationEvent{

    private static final long serialVersionUID = 1L;

    private String message;
    private boolean newNotification;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SethlansEvent(Object source, String message, boolean newNotification) {
        super(source);
        this.message = message;
        this.newNotification = newNotification;
    }

    public SethlansEvent(Object source, boolean newNotification) {
        super(source);
        this.message = message;
        this.newNotification = newNotification;
    }

    public String getMessage() {
        return message;
    }

    public boolean isNewNotification() {
        return newNotification;
    }
}

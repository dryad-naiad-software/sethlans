export class NotificationSetttings {
  systemEmailNotifications: boolean;
  nodeEmailNotifications: boolean;
  projectEmailNotifications: boolean;
  videoEncodingEmailNotifications: boolean;

  constructor() {
    this.systemEmailNotifications = false;
    this.nodeEmailNotifications = false;
    this.projectEmailNotifications = false;
    this.videoEncodingEmailNotifications = false;
  }
}

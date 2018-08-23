export class MailSettings {
  mailHost: string;
  mailPort: string;
  username: string;
  password: string;
  smtpAuth: boolean;
  startTLSEnable: boolean;
  startTLSRequired: boolean;

  constructor() {
    this.mailHost = '';
    this.mailPort = '';
    this.username = '';
    this.smtpAuth = true;
    this.startTLSEnable = false;
    this.startTLSRequired = false;
  }
}

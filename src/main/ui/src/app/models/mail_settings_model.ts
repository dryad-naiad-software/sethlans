export class MailSettings {
  mailHost: string;
  mailPort: number;
  username: string;
  password: string;
  smtpAuth: boolean;
  replyToAddress: string;
  startTLSEnable: boolean;
  startTLSRequired: boolean;

  constructor() {
    this.mailHost = '';
    this.mailPort = 25;
    this.username = '';
    this.replyToAddress = '';
    this.smtpAuth = true;
    this.startTLSEnable = false;
    this.startTLSRequired = false;
  }
}

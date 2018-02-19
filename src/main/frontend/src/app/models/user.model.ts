export class User {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  private active: boolean;

  setUserName(username: string) {
    this.username = username;
  }

  getUserName(): string {
    return this.username;
  }

  getPassword(): string {
    return this.password;
  }

  setPassword(password: string) {
    this.password = password;
  }

  setEmail(email: string) {
    this.email = email;
  }

  getEmail(): string {
    return this.email;
  }

  setActive(active: boolean) {
    this.active = active;
  }

  isActive(): boolean {
    return this.active;
  }

}

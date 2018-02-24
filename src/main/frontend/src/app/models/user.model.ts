import {Role} from "../enums/role.enum";

export class User {
  private username: string;
  private password: string;
  private passwordConfirm: string;
  private email: string;
  private roles: Role[];
  private active: boolean;
  private passwordUpdated: boolean;


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

  setPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirm = passwordConfirm;

  }

  getRoles(): Role[] {
    return this.roles;
  }

  setRoles(roles: Role[]) {
    this.roles = roles;
  }

  isPasswordUpdated(): boolean {
    return this.passwordUpdated;
  }

  setPasswordUpdated(passwordUpdated: boolean) {
    this.passwordUpdated = passwordUpdated;
  }
}

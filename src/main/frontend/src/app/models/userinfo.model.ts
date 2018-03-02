import {Role} from "../enums/role.enum";

export class UserInfo {
  username: string;
  email: string;
  roles: Role[];
  active: boolean;
  password: string;
  dateCreated: any;
  lastUpdated: any;

}

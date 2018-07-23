import {Component, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {UserListService} from "../../../services/userlist.service";

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['username', 'email', 'status', 'role', 'created', 'lastUpdated', 'actions'];

  constructor(private http: HttpClient, private userListService: UserListService) {
  }

  ngOnInit() {
    this.loadTable();
  }

  loadTable() {
    this.userListService.getUserList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }

  addUser() {
    window.location.href = "/admin/user_management/add";
  }

  editUser(id) {
    window.location.href = "/admin/user_management/edit/" + id;
  }

  activateUser(id) {
    this.http.get('/api/management/activate_user/' + id + "/").subscribe(() => {
      this.loadTable();
    });
  }

  deleteUser(id) {
    this.http.get('/api/management/delete_user/' + id + "/").subscribe(() => {
      this.loadTable();
    });
  }

  deactivateUser(id) {
    this.http.get('/api/management/deactivate_user/' + id + "/").subscribe(() => {
      this.loadTable();
    });
  }

}

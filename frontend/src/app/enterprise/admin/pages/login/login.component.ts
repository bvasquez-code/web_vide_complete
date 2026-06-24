import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AdminVideoService } from '../../../video/service/AdminVideoService';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  UserName = 'admin';
  Password = 'admin123';
  errorMessage = '';

  constructor(private adminVideoService: AdminVideoService, private router: Router) {}

  async Login(): Promise<void> {
    this.errorMessage = '';
    const rpt = await this.adminVideoService.login(this.UserName, this.Password);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    sessionStorage.setItem('AdminToken', rpt.Data.Token);
    sessionStorage.setItem('AdminUserCod', rpt.Data.UserCod);
    sessionStorage.setItem('AdminUserName', rpt.Data.UserName);
    await this.router.navigate(['/admin']);
  }
}

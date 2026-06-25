import { Injectable } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';
import { ApiService } from '../../shared/service/ApiService';
import { ResponseWsDto } from '../../shared/model/dto/ResponseWsDto';
import { AuthStorageService } from '../../shared/service/AuthStorageService';

@Injectable({ providedIn: 'root' })
export class PublicAuthService {
  constructor(private apiService: ApiService, private authStorageService: AuthStorageService) {}

  isLogged(): boolean {
    return !!this.authStorageService.getItem('ViewerToken');
  }

  viewerName(): string {
    return this.authStorageService.getItem('ViewerName') || this.authStorageService.getItem('ViewerUserName') || 'Usuario';
  }

  async login(UserName: string, Password: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/auth/login`, { UserName, Password });
  }

  async register(Email: string, UserName: string, Password: string, Names: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/auth/register`, { Email, UserName, Password, Names });
  }

  saveSession(data: any): void {
    this.authStorageService.setItem('ViewerToken', data.Token);
    this.authStorageService.setItem('ViewerUserCod', data.UserCod);
    this.authStorageService.setItem('ViewerUserName', data.UserName);
    this.authStorageService.setItem('ViewerName', data.Names || data.UserName);
  }

  logout(): void {
    this.authStorageService.removeItems(['ViewerToken', 'ViewerUserCod', 'ViewerUserName', 'ViewerName']);
  }
}

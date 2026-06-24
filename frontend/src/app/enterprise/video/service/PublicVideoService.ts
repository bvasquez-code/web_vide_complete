import { Injectable } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';
import { ApiService } from '../../shared/service/ApiService';
import { ResponseWsDto } from '../../shared/model/dto/ResponseWsDto';

@Injectable({ providedIn: 'root' })
export class PublicVideoService {
  constructor(private apiService: ApiService) {}

  async findCategories(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/categories`);
  }

  async findTopCategories(limit: number = 8): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/categories/top`, { Limit: limit });
  }

  async findRecent(limit: number = 12): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/videos/recent`, { Limit: limit });
  }

  async findMostViewed(limit: number = 12): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/videos/mostViewed`, { Limit: limit });
  }

  async search(Query: string, Page: number = 1, Limit: number = 30, Sort: string = 'recent'): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/videos/search`, { Query, Sort, Page, Limit });
  }

  async findByCategory(categoryCod: string, sort: string = 'recent', Page: number = 1, Limit: number = 24): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/categories/${categoryCod}/videos`, { Sort: sort, Page, Limit });
  }

  async findByActor(actorCod: string, sort: string = 'recent', Page: number = 1, Limit: number = 24): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/actors/${actorCod}/videos`, { Sort: sort, Page, Limit });
  }

  async findDetail(videoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/videos/${videoCod}`);
  }

  async findRelated(videoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/videos/${videoCod}/related`, { Limit: 8 });
  }

  async registerView(videoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/videos/${videoCod}/view`, {});
  }
}

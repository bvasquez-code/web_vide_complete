import { Injectable } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';
import { ResponseWsDto } from '../../shared/model/dto/ResponseWsDto';
import { ApiService } from '../../shared/service/ApiService';
import { ActorEntity } from '../model/entity/ActorEntity';
import { TagEntity } from '../model/entity/TagEntity';
import { VideoCategoryEntity } from '../model/entity/VideoCategoryEntity';
import { VideoRegisterDto } from '../model/dto/VideoRegisterDto';

@Injectable({ providedIn: 'root' })
export class AdminVideoService {
  constructor(private apiService: ApiService) {}

  async login(UserName: string, Password: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/auth/login`, { UserName, Password });
  }

  async findVideos(filters: any): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/videos/findAll`, filters);
  }

  async findVideo(VideoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/videos/findById`, { VideoCod });
  }

  async findDataForm(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/videos/findDataForm`);
  }

  async saveVideo(dto: VideoRegisterDto): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/save`, dto);
  }

  async enableVideo(VideoCod: string): Promise<ResponseWsDto> {
    const dto = new VideoRegisterDto();
    dto.Video.VideoCod = VideoCod;
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/enable`, dto);
  }

  async disableVideo(VideoCod: string): Promise<ResponseWsDto> {
    const dto = new VideoRegisterDto();
    dto.Video.VideoCod = VideoCod;
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/disable`, dto);
  }

  async uploadThumbnail(VideoCod: string, file: Blob): Promise<ResponseWsDto> {
    const formData = new FormData();
    formData.append('VideoCod', VideoCod);
    formData.append('File', file, `${VideoCod}.jpg`);
    return await this.apiService.ExecutePostFormDataService(`${AppSetting.API}/api/v1/admin/videos/uploadThumbnail`, formData);
  }

  async findCategories(Query: string = '', Status: string = '', Page: number = 1): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/categories/findAll`, { Query, Status, Page, Limit: 20 });
  }

  async saveCategory(entity: VideoCategoryEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/categories/save`, entity);
  }

  async enableCategory(entity: VideoCategoryEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/categories/enable`, entity);
  }

  async disableCategory(entity: VideoCategoryEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/categories/disable`, entity);
  }

  async findActors(Query: string = '', Status: string = '', Page: number = 1): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/actors/findAll`, { Query, Status, Page, Limit: 20 });
  }

  async saveActor(entity: ActorEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/actors/save`, entity);
  }

  async enableActor(entity: ActorEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/actors/enable`, entity);
  }

  async disableActor(entity: ActorEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/actors/disable`, entity);
  }

  async findTags(Query: string = '', Status: string = '', Page: number = 1): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/tags/findAll`, { Query, Status, Page, Limit: 20 });
  }

  async saveTag(entity: TagEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/tags/save`, entity);
  }

  async enableTag(entity: TagEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/tags/enable`, entity);
  }

  async disableTag(entity: TagEntity): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/tags/disable`, entity);
  }
}

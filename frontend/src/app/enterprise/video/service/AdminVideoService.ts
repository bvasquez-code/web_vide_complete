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

  async findCaptureSuggestions(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/videos/captureSuggestions`);
  }

  async approveCaptureSuggestion(suggestionId: number, reviewComment: string = ''): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/captureSuggestions/${suggestionId}/approve`, { ReviewComment: reviewComment });
  }

  async rejectCaptureSuggestion(suggestionId: number, reviewComment: string = ''): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/captureSuggestions/${suggestionId}/reject`, { ReviewComment: reviewComment });
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

  async uploadCapture(VideoCod: string, file: Blob): Promise<ResponseWsDto> {
    const formData = new FormData();
    formData.append('VideoCod', VideoCod);
    formData.append('File', file, `${VideoCod}-capture.jpg`);
    return await this.apiService.ExecutePostFormDataService(`${AppSetting.API}/api/v1/admin/videos/uploadCapture`, formData);
  }

  async captureAtSecond(VideoCod: string, CaptureSecond: number): Promise<ResponseWsDto> {
    const url = `${AppSetting.API}/api/v1/admin/videos/captureAtSecond?VideoCod=${encodeURIComponent(VideoCod)}&CaptureSecond=${encodeURIComponent(CaptureSecond)}`;
    return await this.apiService.ExecutePostService(url, {});
  }

  async generateCaptures(VideoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/generateCaptures?VideoCod=${encodeURIComponent(VideoCod)}`, {});
  }

  async registerWatchProgress(VideoCod: string, dto: any): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/admin/videos/${encodeURIComponent(VideoCod)}/watchProgress`, dto);
  }

  async findCategories(Query: string = '', Status: string = '', Page: number = 1, Limit: number = 20): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/categories/findAll`, { Query, Status, Page, Limit });
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

  async findActors(Query: string = '', Status: string = '', Page: number = 1, Limit: number = 20): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/actors/findAll`, { Query, Status, Page, Limit });
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

  async findTags(Query: string = '', Status: string = '', Page: number = 1, Limit: number = 20): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/admin/tags/findAll`, { Query, Status, Page, Limit });
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

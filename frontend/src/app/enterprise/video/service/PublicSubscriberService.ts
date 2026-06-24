import { Injectable } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';
import { ApiService } from '../../shared/service/ApiService';
import { ResponseWsDto } from '../../shared/model/dto/ResponseWsDto';

@Injectable({ providedIn: 'root' })
export class PublicSubscriberService {
  constructor(private apiService: ApiService) {}

  async state(videoCod: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/me/videos/${videoCod}/state`);
  }

  async reaction(videoCod: string, reactionType: 'LIKE' | 'DISLIKE', enabled: boolean = true): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/me/videos/reaction`, { VideoCod: videoCod, ReactionType: reactionType, Enabled: enabled });
  }

  async rating(videoCod: string, ratingValue: number): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/me/videos/rating`, { VideoCod: videoCod, RatingValue: ratingValue });
  }

  async watchLater(videoCod: string, enabled: boolean): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/me/videos/watchLater`, { VideoCod: videoCod, Enabled: enabled });
  }

  async suggestCapture(videoCod: string, captureSecond: number, comment: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/me/videos/captureSuggestion`, { VideoCod: videoCod, CaptureSecond: captureSecond, Comment: comment });
  }

  async watchLaterList(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/me/watchLater`);
  }

  async history(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/me/history`);
  }

  async liked(): Promise<ResponseWsDto> {
    return await this.apiService.ExecuteGetService(`${AppSetting.API}/api/v1/public/me/liked`);
  }
}

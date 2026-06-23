import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AppSetting } from '../../../../config/AppSetting';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { VideoEntity } from '../../../video/model/entity/VideoEntity';

@Component({
  selector: 'app-listvideos',
  templateUrl: './listvideos.component.html'
})
export class ListvideosComponent implements OnInit {
  Query = '';
  Status = '';
  SourceType = '';
  videos: VideoEntity[] = [];
  message = '';
  Page = 1;
  Limit = 20;
  TotalRows = 0;
  previewVideo: VideoEntity | null = null;
  previewSource = '';
  previewEmbedHtml: SafeHtml | null = null;
  previewError = '';

  constructor(private adminVideoService: AdminVideoService, private sanitizer: DomSanitizer) {}

  async ngOnInit(): Promise<void> {
    await this.findAll(1);
  }

  async findAll(page: number = this.Page): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    const rpt = await this.adminVideoService.findVideos({ Query: this.Query, Status: this.Status, SourceType: this.SourceType, Page: this.Page, Limit: this.Limit });
    this.videos = rpt.Data?.Data || [];
    this.TotalRows = rpt.Data?.TotalRows || 0;
    this.Page = rpt.Data?.Page || this.Page;
    this.Limit = rpt.Data?.Limit || this.Limit;
  }

  async search(): Promise<void> {
    await this.findAll(1);
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.TotalRows / this.Limit));
  }

  canPreviousPage(): boolean {
    return this.Page > 1;
  }

  canNextPage(): boolean {
    return this.Page < this.totalPages();
  }

  async enable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.enableVideo(video.VideoCod);
    await this.findAll(this.Page);
  }

  async disable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.disableVideo(video.VideoCod);
    await this.findAll(this.Page);
  }

  openPreview(video: VideoEntity): void {
    this.previewVideo = video;
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.previewError = '';

    if (!video.SourceType || !video.SourceValue) {
      this.previewError = 'Este video no tiene una referencia valida para previsualizar.';
      return;
    }
    if (video.SourceType === 'EMBED') {
      this.previewEmbedHtml = this.sanitizer.bypassSecurityTrustHtml(video.SourceValue);
      return;
    }
    if (video.SourceType === 'PATH') {
      this.previewSource = `${AppSetting.API}/api/v1/public/videos/${video.VideoCod}/stream`;
      return;
    }
    this.previewSource = video.SourceValue;
  }

  closePreview(): void {
    this.previewVideo = null;
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.previewError = '';
  }

  onPreviewError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    const code = video.error?.code;
    this.previewError = `No se pudo cargar la previsualizacion. Codigo: ${code || 'desconocido'}.`;
  }
}

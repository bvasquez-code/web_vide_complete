import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { VideoEntity } from '../../../video/model/entity/VideoEntity';
import { PaginationDto } from '../../../shared/model/dto/PaginationDto';

@Component({
  selector: 'app-listvideos',
  templateUrl: './listvideos.component.html'
})
export class ListvideosComponent implements OnInit, OnDestroy {
  Query = '';
  Status = '';
  SourceType = '';
  videos: VideoEntity[] = [];
  message = '';
  Page = 1;
  Limit = 20;
  TotalRows = 0;
  pagination = new PaginationDto({ Limit: this.Limit, ItemLabel: 'videos' });
  previewVideo: VideoEntity | null = null;
  previewSource = '';
  previewEmbedHtml: SafeHtml | null = null;
  previewError = '';
  previewWidth = 420;
  private resizingPreview = false;
  private resizeStartX = 0;
  private resizeStartWidth = 420;
  private previewViewLogId: number | null = null;
  private previewPendingWatchSeconds = 0;
  private previewLastWatchPosition: number | null = null;
  private previewLastWatchFlushAt = 0;

  constructor(private adminVideoService: AdminVideoService, private sanitizer: DomSanitizer, private route: ActivatedRoute, private router: Router) {}

  async ngOnInit(): Promise<void> {
    const params = this.route.snapshot.queryParamMap;
    this.Query = params.get('Query') || '';
    this.Status = params.get('Status') || '';
    this.SourceType = params.get('SourceType') || '';
    this.Page = Number(params.get('Page') || '1');
    this.Limit = Number(params.get('Limit') || '20');
    await this.findAll(this.Page, false);
  }

  ngOnDestroy(): void {
    this.flushPreviewWatchProgress();
  }

  async findAll(page: number = this.Page, syncUrl: boolean = true): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    const rpt = await this.adminVideoService.findVideos({ Query: this.Query, Status: this.Status, SourceType: this.SourceType, Page: this.Page, Limit: this.Limit });
    this.videos = rpt.Data?.Data || [];
    this.TotalRows = rpt.Data?.TotalRows || 0;
    this.Page = rpt.Data?.Page || this.Page;
    this.Limit = rpt.Data?.Limit || this.Limit;
    this.pagination = new PaginationDto({ Page: this.Page, Limit: this.Limit, TotalRows: this.TotalRows, ItemLabel: 'videos' });
    if (syncUrl) {
      await this.syncQueryParams();
    }
  }

  async search(): Promise<void> {
    await this.findAll(1);
  }

  private async syncQueryParams(): Promise<void> {
    await this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        Page: this.Page,
        Limit: this.Limit,
        Query: this.Query || null,
        Status: this.Status || null,
        SourceType: this.SourceType || null
      },
      queryParamsHandling: 'merge'
    });
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
    this.flushPreviewWatchProgress();
    this.previewVideo = video;
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.previewError = '';
    this.resetPreviewWatchTracking();

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
    this.flushPreviewWatchProgress();
    this.previewVideo = null;
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.previewError = '';
    this.resetPreviewWatchTracking();
  }

  onPreviewError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    const code = video.error?.code;
    this.previewError = `No se pudo cargar la previsualizacion. Codigo: ${code || 'desconocido'}.`;
  }

  onPreviewPlay(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.previewLastWatchPosition = video.currentTime;
  }

  onPreviewTimeUpdate(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    if (this.previewPendingWatchSeconds >= 10 || Date.now() - this.previewLastWatchFlushAt >= 10000) {
      this.flushPreviewWatchProgress();
    }
  }

  onPreviewPause(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    this.flushPreviewWatchProgress();
  }

  onPreviewSeeked(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.previewLastWatchPosition = video.currentTime;
    this.flushPreviewWatchProgress();
  }

  onPreviewEnded(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    this.flushPreviewWatchProgress(true);
  }

  startPreviewResize(event: MouseEvent): void {
    event.preventDefault();
    this.resizingPreview = true;
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = this.previewWidth;
  }

  @HostListener('document:mousemove', ['$event'])
  onPreviewResize(event: MouseEvent): void {
    if (!this.resizingPreview) {
      return;
    }
    const maxWidth = Math.max(280, window.innerWidth - 36);
    const nextWidth = this.resizeStartWidth + (this.resizeStartX - event.clientX);
    this.previewWidth = Math.min(maxWidth, Math.max(280, nextWidth));
  }

  @HostListener('document:mouseup')
  stopPreviewResize(): void {
    this.resizingPreview = false;
  }

  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    this.flushPreviewWatchProgress();
  }

  private resetPreviewWatchTracking(): void {
    this.previewViewLogId = null;
    this.previewPendingWatchSeconds = 0;
    this.previewLastWatchPosition = null;
    this.previewLastWatchFlushAt = 0;
  }

  private addPreviewWatchDelta(video: HTMLVideoElement): void {
    if (video.paused || video.seeking) {
      this.previewLastWatchPosition = video.currentTime;
      return;
    }
    if (this.previewLastWatchPosition !== null) {
      const delta = video.currentTime - this.previewLastWatchPosition;
      if (delta > 0 && delta <= 2.5) {
        this.previewPendingWatchSeconds += delta;
      }
    }
    this.previewLastWatchPosition = video.currentTime;
  }

  private async flushPreviewWatchProgress(completed: boolean = false): Promise<void> {
    const videoCod = this.previewVideo?.VideoCod || '';
    if (!videoCod || (!this.previewViewLogId && this.previewPendingWatchSeconds <= 0 && !completed)) {
      return;
    }
    const viewLogId = this.previewViewLogId;
    const playedSeconds = Number(this.previewPendingWatchSeconds.toFixed(3));
    this.previewPendingWatchSeconds = 0;
    this.previewLastWatchFlushAt = Date.now();
    const rpt = await this.adminVideoService.registerWatchProgress(videoCod, {
      ViewLogId: viewLogId,
      VideoCod: videoCod,
      PlayerContext: 'ADMIN_LIST_PREVIEW',
      PlayedSeconds: playedSeconds,
      CurrentSecond: this.previewLastWatchPosition !== null ? Number(this.previewLastWatchPosition.toFixed(3)) : null,
      DurationSeconds: null,
      Completed: completed
    });
    if (!rpt.ErrorStatus && this.previewVideo?.VideoCod === videoCod && this.previewViewLogId === viewLogId) {
      this.previewViewLogId = rpt.Data?.ViewLogId || this.previewViewLogId;
    }
  }
}

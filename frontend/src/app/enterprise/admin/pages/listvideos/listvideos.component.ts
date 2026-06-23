import { Component, HostListener, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
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
  pageInput = 1;
  Limit = 20;
  TotalRows = 0;
  previewVideo: VideoEntity | null = null;
  previewSource = '';
  previewEmbedHtml: SafeHtml | null = null;
  previewError = '';
  previewWidth = 420;
  private resizingPreview = false;
  private resizeStartX = 0;
  private resizeStartWidth = 420;

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

  async findAll(page: number = this.Page, syncUrl: boolean = true): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    const rpt = await this.adminVideoService.findVideos({ Query: this.Query, Status: this.Status, SourceType: this.SourceType, Page: this.Page, Limit: this.Limit });
    this.videos = rpt.Data?.Data || [];
    this.TotalRows = rpt.Data?.TotalRows || 0;
    this.Page = rpt.Data?.Page || this.Page;
    this.Limit = rpt.Data?.Limit || this.Limit;
    this.pageInput = this.Page;
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

  totalPages(): number {
    return Math.max(1, Math.ceil(this.TotalRows / this.Limit));
  }

  canPreviousPage(): boolean {
    return this.Page > 1;
  }

  canNextPage(): boolean {
    return this.Page < this.totalPages();
  }

  async goToFirstPage(): Promise<void> {
    await this.findAll(1);
  }

  async goToLastPage(): Promise<void> {
    await this.findAll(this.totalPages());
  }

  async goToPageInput(): Promise<void> {
    const requestedPage = Number(this.pageInput);
    if (!Number.isFinite(requestedPage)) {
      this.pageInput = this.Page;
      return;
    }
    const targetPage = Math.trunc(requestedPage);
    if (targetPage < 1 || targetPage > this.totalPages()) {
      this.pageInput = this.Page;
      return;
    }
    await this.findAll(targetPage);
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
}
